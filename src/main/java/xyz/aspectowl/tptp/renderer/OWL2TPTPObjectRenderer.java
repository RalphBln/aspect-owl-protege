package xyz.aspectowl.tptp.renderer;

import com.google.common.collect.HashBiMap;
import org.tweetyproject.commons.ParserException;
import org.tweetyproject.commons.util.Pair;
import org.tweetyproject.logics.commons.syntax.Constant;
import org.tweetyproject.logics.commons.syntax.Predicate;
import org.tweetyproject.logics.fol.parser.FolParser;
import org.tweetyproject.logics.fol.syntax.FolBeliefSet;
import org.tweetyproject.logics.fol.syntax.FolFormula;
import org.tweetyproject.logics.fol.syntax.FolSignature;
import org.semanticweb.owlapi.formats.PrefixDocumentFormat;
import org.semanticweb.owlapi.io.XMLUtils;
import org.semanticweb.owlapi.model.*;
import org.semanticweb.owlapi.model.parameters.Imports;
import org.semanticweb.owlapi.util.AnnotationValueShortFormProvider;
import org.semanticweb.owlapi.util.DefaultPrefixManager;
import org.semanticweb.owlapi.util.OWLObjectVisitorExAdapter;
import org.semanticweb.owlapi.util.ShortFormProvider;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import xyz.aspectowl.owlapi.model.OWLAspect;
import xyz.aspectowl.tptp.reasoner.util.UnsortedTPTPWriter;
import xyz.aspectowl.tptp.util.Counter;

import javax.annotation.Nonnull;
import java.io.IOException;
import java.io.Writer;
import java.nio.charset.StandardCharsets;
import java.util.*;
import java.util.stream.Collectors;
import java.util.stream.Stream;

/**
 * @author ralph
 *
 * Prefixes:
 *
 * nc: named class
 * ni: named individual
 * no: named object property
 * nd: named data property
 *
 * tc: temporary (anonymous) class (such as "r some A")
 * to: temporary (anonymous) object property (such as "inverse of r")
 *
 * owl: owl built-in entity names (owlThing and owlNothing)
 */
public class OWL2TPTPObjectRenderer extends OWLObjectVisitorExAdapter<Stream<FolFormula>> {

    private static final Logger log = LoggerFactory.getLogger(OWL2TPTPObjectRenderer.class);

    // TPTP only allows for ascii characters in names, so we have to convert iris accordingly.
    // We normally do this by creating an ascii-only UUID for each IRI.
    // This flag makes the OWL2TPTPObjectRenderer convert iris to human-readable names.
    // This is for debugging purposes; colliding short forms receive a UUID suffix.
    private boolean humanReadableTptpNames = false;

    protected final Imports includeImportsClosure;

    protected final Optional<OWLOntology> onto;
    private DefaultPrefixManager defaultPrefixManager;
    protected Optional<ShortFormProvider> labelMaker = Optional.empty();
    private Optional<PrefixManager> prefixManager = Optional.empty();
    private boolean writeEntitiesAsURIs = true;
    private boolean addMissingDeclarations = true;

    // Since TPTP only allows letters, numbers, and the underscore character in names we have to translate the OWL IRIs
    // to names containing only these characters and keep the mappings for later back translation.

    private UnsortedTPTPWriter out;

    public FolParser getFolParser() {
        return folp;
    }

    private FolParser folp;

    public FolBeliefSet getBeliefSet() {
        return bs;
    }

    private HashBiMap<HasIRI, String> nameMap = HashBiMap.create();

    // There is a DlBeliefSet and I considered using it (somehow shove the OWL2 KB into a DLBeliefSet and then maybe
    // transform the latter to a FolBeliedSet) but it turned out DLBeliefSet only implements ALC while we need full
    // SROIQ(D).
    protected FolBeliefSet bs;
    protected FolSignature sig;

    protected OWLOntology currentOntology;
    private final Set<OWLObject> definedExpressions = new HashSet<>();

    public OWL2TPTPObjectRenderer(OWLOntology ontology, Writer writer, Imports includeImportsClosure) {

        super(Stream.empty());

        this.includeImportsClosure = includeImportsClosure;

        onto = Optional.ofNullable(ontology);
        out = new UnsortedTPTPWriter(writer);
        defaultPrefixManager = new DefaultPrefixManager();

        onto.ifPresent(o -> {
            OWLOntologyManager manager = o.getOWLOntologyManager();
            OWLDocumentFormat ontologyFormat = manager.getOntologyFormat(o);
            // reuse the setting on the existing format
            addMissingDeclarations = ontologyFormat != null && ontologyFormat.isAddMissingTypes();
            if (ontologyFormat instanceof PrefixDocumentFormat) {
                prefixManager = Optional.of(new DefaultPrefixManager());
                prefixManager.get().copyPrefixesFrom((PrefixDocumentFormat) ontologyFormat);
                prefixManager.get().setPrefixComparator(
                        ((PrefixDocumentFormat) ontologyFormat).getPrefixComparator());
                if (!o.isAnonymous() && prefixManager.get().getDefaultPrefix() == null) {
                    prefixManager.get().setDefaultPrefix(XMLUtils.iriWithTerminatingHash(
                            o.getOntologyID().getOntologyIRI().get().toString()));
                }
            }
            OWLDataFactory df = manager.getOWLDataFactory();
            labelMaker = Optional.of(
                    new AnnotationValueShortFormProvider(Collections.singletonList(df.getRDFSLabel()),
                            Collections.emptyMap(), manager, defaultPrefixManager));
        });
    }

    /**
     * Sets a flag that makes {@link OWL2TPTPObjectRenderer} create human-readable names for debugging.
     * Defaults to false.
     * @param humanReadableTptpNames if true, makes {@link OWL2TPTPObjectRenderer} create human-readable names for debugging.
     */
    public void setHumanReadableTptpNames(boolean humanReadableTptpNames) {
        this.humanReadableTptpNames = humanReadableTptpNames;
    }

    /**
     * Translates an IRI to a FOL constant name
     * @param iri
     * @return
     */
    private String translateIRI(HasIRI iri) {

        if (iri instanceof OWLClass && iri.getIRI().isNothing())
            return "owlNothing";
        if (iri instanceof OWLClass && iri.getIRI().isThing())
            return "owlThing";

        String cachedName = nameMap.get(iri);
        if (cachedName != null)
            return cachedName;

        String prefix;
        if (iri instanceof OWLClass)
            prefix = "nc"; // named class
        else if (iri instanceof OWLIndividual)
            prefix = "ni"; // named individual
        else if (iri instanceof OWLObjectProperty)
            prefix = "no"; // named object property
        else if (iri instanceof OWLDataProperty)
            prefix = "nd"; // named data property
        else
            prefix = "Error"; // should not happen

        String localName = humanReadableTptpNames ?
            iri.getIRI().getShortForm().replaceFirst("^.*#(.+?)$", "$1")  // regex is a workaround for https://github.com/owlcs/owlapi/issues/699
            : UUID.nameUUIDFromBytes(iri.getIRI().toString().getBytes(StandardCharsets.UTF_8)).toString().replaceAll("-", "");

        String tptpName = String.format("%s_%s", prefix, localName.replaceAll("[^a-zA-Z0-9_]", "_"));
        if (nameMap.containsValue(tptpName))
            tptpName += "_" + UUID.nameUUIDFromBytes(iri.getIRI().toString().getBytes(StandardCharsets.UTF_8))
                    .toString().replace("-", "");
        nameMap.put(iri, tptpName);
        return tptpName;
    }

    @Override
    public Stream<FolFormula> visit(OWLNegativeObjectPropertyAssertionAxiom axiom) {
        return makeFormula("!%s(%s,%s)", translate(axiom.getProperty()), translate(axiom.getSubject()), translate(axiom.getObject()));
    }

    @Override
    public Stream<FolFormula> visit(OWLAsymmetricObjectPropertyAxiom axiom) {
        return makeFormula("forall X: (forall Y: (%1$s(X,Y) => !%1$s(Y,X)))", translate(axiom.getProperty()));
    }

    @Override
    public Stream<FolFormula> visit(OWLDisjointClassesAxiom axiom) {
        return disjointClasses(axiom.getClassExpressions());
    }

    private Stream<FolFormula> disjointClasses(Collection<OWLClassExpression> classes) {
        List<String> predicateNames = classes.stream().map(this::translate).collect(Collectors.toList());
        return combinations(predicateNames, predicateNames).stream().flatMap(pair -> makeFormula("forall X: (!(%s(X) && %s(X)))", pair.getFirst(), pair.getSecond()));
    }

    @Override
    public Stream<FolFormula> visit(OWLObjectPropertyDomainAxiom axiom) {
        return makeFormula("forall X: (forall Y: (%s(X,Y) => %s(X)))", translate(axiom.getProperty()), translate(axiom.getDomain()));
    }

    @Override
    public Stream<FolFormula> visit(OWLEquivalentObjectPropertiesAxiom axiom) {
        List<OWLObjectPropertyExpression> properties = new ArrayList<>(axiom.getProperties());
        return properties.stream().skip(1).flatMap(property -> makeFormula(
                "forall X: (forall Y: (%s(X,Y) <=> %s(X,Y)))",
                translate(properties.get(0)), translate(property)));
    }

    @Override
    public Stream<FolFormula> visit(OWLDisjointObjectPropertiesAxiom axiom) {
        List<String> predicateNames = axiom.getProperties().stream().map(ce -> translate(ce)).collect(Collectors.toList());
        return combinations(predicateNames, predicateNames).stream().flatMap(pair -> makeFormula("forall X: (forall Y: (!(%s(X,Y) && %s(X,Y))))", pair.getFirst(), pair.getSecond()));
    }

    @Override
    public Stream<FolFormula> visit(OWLObjectPropertyRangeAxiom axiom) {
        return makeFormula("forall X: (forall Y: (%s(X,Y) => %s(Y)))", translate(axiom.getProperty()), translate(axiom.getRange()));
    }

    @Override
    public Stream<FolFormula> visit(OWLFunctionalObjectPropertyAxiom axiom) {
        return makeFormula("forall X: (forall Y1: (forall Y2: (%1$s(X,Y1) && %1$s(X,Y2) => Y1 == Y2)))", translate(axiom.getProperty()));
    }

    @Override
    public Stream<FolFormula> visit(OWLSubObjectPropertyOfAxiom axiom) {
        return makeFormula("forall X: (forall Y: (%s(X,Y) => %s(X,Y)))", translate(axiom.getSubProperty()), translate(axiom.getSuperProperty()));
    }

    @Override
    public Stream<FolFormula> visit(OWLDisjointUnionAxiom axiom) {
        var disjointnessFormulae = disjointClasses(axiom.getClassExpressions());
        StringBuilder buf = new StringBuilder();
        buf.append(String.format("forall X: ( %s(X) <=> ", translate(axiom.getOWLClass())));
        axiom.getClassExpressions().stream().findFirst().ifPresent(ce -> buf.append(String.format("%s(X)", translate(ce))));
        axiom.getClassExpressions().stream().skip(1).forEach(ce -> buf.append(String.format(" || %s(X)", translate(ce))));
        buf.append(")");
        return Stream.concat(makeFormula(buf.toString()), disjointnessFormulae);
    }

    @Override
    public Stream<FolFormula> visit(OWLSymmetricObjectPropertyAxiom axiom) {
        return makeFormula("forall X: (forall Y: (%1$s(X,Y) <=> %1$s(Y,X)))", translate(axiom.getProperty()));
    }

    @Override
    public Stream<FolFormula> visit(OWLAnnotationAssertionAxiom axiom) {
        // There is no translation of annotation assertion axioms to FOL since annotations have no semantics.
        return Stream.empty();
    }

    @Override
    public Stream<FolFormula> visit(OWLIrreflexiveObjectPropertyAxiom axiom) {
        return makeFormula(String.format("forall X: (forall Y: (%s(X,Y) => X /== Y))", translate(axiom.getProperty())));
    }

    @Override
    public Stream<FolFormula> visit(OWLInverseFunctionalObjectPropertyAxiom axiom) {
        return makeFormula("forall X1: (forall X2: (forall Y: (%1$s(X1,Y) && %1$s(X2,Y) => X1 == X2)))", translate(axiom.getProperty()));
    }

    @Override
    public Stream<FolFormula> visit(OWLSameIndividualAxiom axiom) {
        List<OWLIndividual> individuals = axiom.getIndividualsAsList();
        return individuals.stream().skip(1).flatMap(individual -> makeFormula(
                "%s == %s", translate(individuals.get(0)), translate(individual)));
    }

    @Override
    public Stream<FolFormula> visit(OWLSubPropertyChainOfAxiom axiom) {
        //  Θ(r1 o r2 o ... rn ⊑ s) = r1(X1,X2) && r2(X2,X3) && ... && rn(Xn, Xn+1) <=> s(X1,Xn)

        StringBuilder quantBuf = new StringBuilder();
        StringBuilder closingBuf  = new StringBuilder();
        StringBuilder innerBuf    = new StringBuilder();

        final Counter count = new Counter(1);
        axiom.getPropertyChain().stream().findFirst().ifPresent(ope -> handleSubPropertyChainAxiomPart(ope, count, quantBuf, closingBuf, innerBuf));
        axiom.getPropertyChain().stream().skip(1).forEach(ope -> {
            innerBuf.append(" && ");
            handleSubPropertyChainAxiomPart(ope, count, quantBuf, closingBuf, innerBuf);
        });

        quantBuf.append(String.format("forall X%d :(", count.value));
        closingBuf.append(")");
        innerBuf.append(String.format(" => %s(X1, X%d)", translate(axiom.getSuperProperty()), count.value));

        return makeFormula(quantBuf.append(innerBuf).append(closingBuf).toString());
    }

    private void handleSubPropertyChainAxiomPart(OWLObjectPropertyExpression ope, Counter<Integer> count, StringBuilder quantBuf, StringBuilder closingBuf, StringBuilder innerBuf) {
        quantBuf.append(String.format("forall X%d: (", count.value));
        closingBuf.append(")");
        innerBuf.append(String.format("%s(X%d,X%d)", translate(ope), count.value, ++count.value));
    }

    @Override
    public Stream<FolFormula> visit(OWLInverseObjectPropertiesAxiom axiom) {
        return makeFormula("forall X: (forall Y: (%s(X,Y) <=> %s(Y,X)))", translate(axiom.getFirstProperty()), translate(axiom.getSecondProperty()));
    }

    @Override
    public Stream<FolFormula> visit(OWLHasKeyAxiom axiom) {
        throw new UnsupportedOperationException("OWL keys are not supported: " + axiom);
    }

    public String translate(OWLIndividual individual) {
        if (individual.isAnonymous()) {
            throw new IllegalArgumentException(String.format("Cannot translate anonymous individuals to FOL: %s", individual));
        }
        return translateIRI(individual.asOWLNamedIndividual());
    }

    /**
     * Translates the given OWL class expression (CE) to a String representation of its corresponding FOL predicate.
     * Has side effects: Translates all anonymous CEs that the given CE is composed of to temporary named predicates
     * (the names of which correspond to their respective type 3 (name based) UUID generated from the respective String
     * representation of the CE, prefixed by "tc" and minus the "-" characters) and adds formulas that define the
     * complex predicate by using these temporarily named predicates.
     * @param ce
     * @return
     */
    public String translate(OWLClassExpression ce) {
        if (ce instanceof OWLAspect)
            ce = ((OWLAspect) ce).asClassExpression();
        ClassExpressionType type = ce.getClassExpressionType();
        if (type == ClassExpressionType.OWL_CLASS)
            return translateIRI(ce.asOWLClass());

        String tempName = temporaryPredicate(ce);
        if (!definedExpressions.add(ce))
            return tempName;

        StringBuilder buf = new StringBuilder();
        buf.append("forall X: (%s(X) <=> ");

        switch (type) {
            case OBJECT_SOME_VALUES_FROM: {
                OWLObjectSomeValuesFrom svf = (OWLObjectSomeValuesFrom)ce;
                String opeTrans = translate(svf.getProperty());
                String fillerTrans = translate(svf.getFiller());

                buf.append(String.format("exists Y: (%s(X,Y) && %s(Y))", opeTrans, fillerTrans));
                break;
            }
            case OBJECT_ALL_VALUES_FROM: {
                OWLObjectAllValuesFrom avf = (OWLObjectAllValuesFrom) ce;
                String opeTrans = translate(avf.getProperty());
                String fillerTrans = translate(avf.getFiller());

                buf.append(String.format("forall Y: (%s(X,Y) => %s(Y))", opeTrans, fillerTrans));
                break;
            }
            case OBJECT_MIN_CARDINALITY:
            case OBJECT_MAX_CARDINALITY:
            case OBJECT_EXACT_CARDINALITY: {
                OWLObjectCardinalityRestriction restriction = (OWLObjectCardinalityRestriction) ce;
                String property = translate(restriction.getProperty());
                String filler = translate(restriction.getFiller());
                int n = restriction.getCardinality();
                String minimum = minimumCardinality(property, filler, n);
                String maximum = "!(" + minimumCardinality(property, filler, n + 1) + ")";
                buf.append(type == ClassExpressionType.OBJECT_MIN_CARDINALITY ? minimum
                        : type == ClassExpressionType.OBJECT_MAX_CARDINALITY ? maximum
                        : "(" + minimum + " && " + maximum + ")");
                break;
            }
            case OBJECT_HAS_VALUE: {
                OWLObjectHasValue ohv = (OWLObjectHasValue) ce;
                OWLObjectPropertyExpression ope = ohv.getProperty();
                OWLIndividual filler = ohv.getFiller();

                buf.append(String.format("%s(X,%s)", translate(ope), translate(filler)));
                break;
            }
            case OBJECT_HAS_SELF: {
                OWLObjectHasSelf ohs = (OWLObjectHasSelf)ce;
                buf.append(String.format("%s(X,X)", translate(ohs.getProperty())));
                break;
            }
            case OBJECT_INTERSECTION_OF: {
                OWLObjectIntersectionOf oio = (OWLObjectIntersectionOf) ce;
                oio.getOperands().stream().findFirst().ifPresent(operand -> buf.append(String.format("%s(X)", translate(operand))));
                oio.getOperands().stream().skip(1).forEach(operand -> buf.append(String.format(" && %s(X)", translate(operand))));
                break;
            }
            case OBJECT_UNION_OF: {
                OWLObjectUnionOf ouo = (OWLObjectUnionOf) ce;
                ouo.getOperands().stream().findFirst().ifPresent(operand -> buf.append(String.format("%s(X)", translate(operand))));
                ouo.getOperands().stream().skip(1).forEach(operand -> buf.append(String.format(" || %s(X)", translate(operand))));
                break;
            }
            case OBJECT_COMPLEMENT_OF: {
                OWLObjectComplementOf oco = (OWLObjectComplementOf) ce;
                buf.append(String.format("!%s(X)", translate(oco.getOperand())));
                break;
            }
            case OBJECT_ONE_OF: {
                Set<OWLIndividual> individuals = ((OWLObjectOneOf) ce).getIndividuals();
                individuals.stream().findFirst().ifPresent(individual -> buf.append(String.format("X == %s", translate(individual))));
                individuals.stream().skip(1).forEach(individual -> buf.append(String.format(" || X == %s", translate(individual))));
                break;
            }
            case DATA_SOME_VALUES_FROM:
                throw new RuntimeException("Not implemented " + type);
            case DATA_ALL_VALUES_FROM:
                throw new RuntimeException("Not implemented " + type);
            case DATA_MIN_CARDINALITY:
                throw new RuntimeException("Not implemented " + type);
            case DATA_MAX_CARDINALITY:
                throw new RuntimeException("Not implemented " + type);
            case DATA_EXACT_CARDINALITY:
                throw new RuntimeException("Not implemented " + type);
            case DATA_HAS_VALUE:
                throw new RuntimeException("Not implemented " + type);

            default:
                log.error("Unhandled class expression type: {}", type);
                throw new RuntimeException("Unknown class expression type " + type);
        }
        buf.append(')');
        makeFormula(buf.toString(), tempName).forEach(this::addDefinition);
        return tempName;
    }

    @Override
    public Stream<FolFormula> visit(OWLDifferentIndividualsAxiom axiom) {
        List<OWLIndividual> individuals = axiom.getIndividualsAsList();
        return combinations(individuals, individuals).stream().flatMap(pair -> makeFormula("%s /== %s", translate(pair.getFirst()), translate(pair.getSecond())));
    }

    private <T extends Comparable<? super T>> HashSet<Pair<T, T>> combinations(Collection<T> s1, Collection<T> s2) {
        HashSet<Pair<T, T>> pairs = new HashSet<>();

        s1.forEach(i ->
                s2.stream().filter(j -> !(j.equals(i))).forEach(j ->
                        pairs.add(ordered(new Pair<>(i, j)))
                )
        );
        return pairs;
    }

    public static <T extends Comparable<? super T>> Pair<T, T> ordered(Pair<T, T> pair) {
        T a = pair.getFirst();
        T b = pair.getSecond();
        return a.compareTo(b) < 0 ? pair : new Pair<>(b, a);
    }

    private String minimumCardinality(String property, String filler, int n) {
        if (n == 0)
            return "+"; // Tweety's truth constant
        StringBuilder formula = new StringBuilder();
        for (int i = 0; i < n; i++)
            formula.append(String.format("exists Y%d: (", i));
        List<String> conjuncts = new ArrayList<>();
        for (int i = 0; i < n; i++) {
            conjuncts.add(String.format("%s(X,Y%d)", property, i));
            conjuncts.add(String.format("%s(Y%d)", filler, i));
            for (int j = 0; j < i; j++)
                conjuncts.add(String.format("Y%d /== Y%d", i, j));
        }
        return formula.append(String.join(" && ", conjuncts)).append(")".repeat(n)).toString();
    }

    public String temporaryPredicate(OWLObject o) {
        String prefix;
        int arity;
        if (o instanceof OWLClassExpression) {
            prefix = "tc_";
            arity = 1;
        } else if (o instanceof OWLObjectPropertyExpression) {
            prefix = "to_";
            arity = 2;
        } else {
            throw new IllegalArgumentException("Can only make temporary predicates out of classes or object properties.");
        }
        String name = prefix + UUID.nameUUIDFromBytes(o.toString().getBytes(StandardCharsets.UTF_8))
                .toString().replace("-", "");
        sig.add(new Predicate(name, arity));
        return name;
    }

    public String translate(OWLObjectPropertyExpression ope) {
        if (ope.isNamed())
            return translateIRI(ope.asOWLObjectProperty());
        String tempName = temporaryPredicate(ope);
        if (definedExpressions.add(ope)) {
            makeFormula("forall X: (forall Y: (%s(X,Y) <=> %s(Y,X)))",
                    tempName, translate(ope.getNamedProperty())).forEach(this::addDefinition);
        }
        return tempName;
    }

    /**
     * Always returns a stream containing *exactly one* formula.
     * @param format a formatted TPTP string
     * @param args the arguments for the formatted string
     * @return A stream containing *exactly one* formula.
     */
    public Stream<FolFormula> makeFormula(String format, Object... args) {
        try {
            return Stream.of((FolFormula)folp.parseFormula(String.format(format, args)));
        } catch (IOException e) {
            throw new ParserException(e);
        }
    }

    @Override
    public Stream<FolFormula> visit(OWLOntology ontology) {
        nameMap.clear();
        definedExpressions.clear();
        // signature
        sig = new FolSignature(true);
        sig.add(new Predicate("owlThing", 1));
        sig.add(new Predicate("owlNothing", 1));
        ontology.getSignature(includeImportsClosure).forEach(entity -> entity.accept(this));

        folp = new FolParser(false);
        folp.setSignature(sig);

        // axioms
        bs = new FolBeliefSet();
        bs.setSignature(sig);
        try {
            bs.add(folp.parseFormula("forall X: (owlThing(X))"));
            bs.add(folp.parseFormula("forall X: (!owlNothing(X))"));
        } catch (IOException e) {
            throw new OWL2TPTPRendererError(String.format("Error configuring %s. Could not parse trivial formula. This should not have happened.", OWL2TPTPObjectRenderer.class.getSimpleName()), e);
        }

        Stream<OWLOntology> ontologies = includeImportsClosure == Imports.INCLUDED
                ? ontology.getImportsClosure().stream() : Stream.of(ontology);
        try {
            ontologies.forEach(source -> {
                currentOntology = source;
                source.getAxioms().forEach(axiom -> Optional.ofNullable(axiom.accept(this))
                        .ifPresent(formulae -> formulae.forEach(bs::add)));
            });
        } finally {
            currentOntology = null;
        }
        finishRendering();

        try {
            out.printBase(bs);
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
        return bs.stream();
    }

    protected void addDefinition(FolFormula formula) {
        bs.add(formula);
    }

    /** Called after axioms and auxiliary definitions have been collected, before serialization. */
    protected void finishRendering() {
    }

    @Override
    public Stream<FolFormula> visit(OWLClass ce) {
        log.debug("Class: {}\n", ce);
        sig.add(new Predicate(translate(ce), 1));
        return Stream.empty();
    }

    @Override
    public Stream<FolFormula> visit(OWLNamedIndividual individual) {
        log.debug("Individual: {}\n", individual);
        sig.add(new Constant(translateIRI(individual)));
        return Stream.empty();
    }

    @Override
    public Stream<FolFormula> visit(OWLObjectProperty property) {
        log.debug("ObjectProperty: {}\n", property);
        sig.add(new Predicate(translateIRI(property), 2));
        return Stream.empty();
    }

    @Override
    public Stream<FolFormula> visit(OWLClassAssertionAxiom axiom) {
        String individualTrans = translate(axiom.getIndividual());
        String ceTrans = translate(axiom.getClassExpression());
        return makeFormula("%s(%s)", ceTrans, individualTrans);
    }

    @Override
    public Stream<FolFormula> visit(OWLObjectPropertyAssertionAxiom axiom) {
        OWLIndividual subject = axiom.getSubject();
        OWLObjectPropertyExpression predicate = axiom.getProperty();
        OWLIndividual object = axiom.getObject();
        return makeFormula("%s(%s,%s)", translate(predicate), translate(subject), translate(object));
    }

    @Override
    public Stream<FolFormula> visit(OWLTransitiveObjectPropertyAxiom axiom) {
        OWLObjectPropertyExpression ope = axiom.getProperty();
        String predicateName = translate(ope);
        return makeFormula("forall X: (forall Y: (forall Z: (%s(X,Y) && %s(Y,Z) => %s(X,Z))))", predicateName, predicateName, predicateName);
    }

    @Override
    public Stream<FolFormula> visit(OWLReflexiveObjectPropertyAxiom axiom) {
        OWLObjectPropertyExpression ope = axiom.getProperty();
        String predicateName = translate(ope);
        return makeFormula("forall X: (%s(X,X))", predicateName);
    }

    @Override
    public Stream<FolFormula> visit(OWLSubClassOfAxiom axiom) {
        OWLClassExpression superCE = axiom.getSuperClass();
        OWLClassExpression subCE = axiom.getSubClass();
        return makeFormula("forall X: (%s(X) => %s(X))", translate(subCE), translate(superCE));
    }

    @Override
    public Stream<FolFormula> visit(OWLEquivalentClassesAxiom axiom) {
        List<OWLClassExpression> classes = new ArrayList<>(axiom.getClassExpressions());
        return classes.stream().skip(1).flatMap(expression -> makeFormula(
                "forall X: (%s(X) <=> %s(X))", translate(classes.get(0)), translate(expression)));
    }

    @Nonnull
    @Override
    protected Stream<FolFormula> doDefault(@Nonnull OWLObject object) {
        if (object instanceof OWLAxiom && ((OWLAxiom) object).isLogicalAxiom())
            throw new UnsupportedOperationException("Unsupported logical axiom: " + object);
        log.debug("No FOL axiom for metadata: {}", object);
        return Stream.empty();
    }
}
