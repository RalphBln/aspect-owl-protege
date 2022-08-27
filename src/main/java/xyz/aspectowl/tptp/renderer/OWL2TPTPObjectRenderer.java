package xyz.aspectowl.tptp.renderer;

import net.sf.tweety.commons.ParserException;
import net.sf.tweety.commons.util.Pair;
import net.sf.tweety.logics.commons.syntax.Constant;
import net.sf.tweety.logics.commons.syntax.Predicate;
import net.sf.tweety.logics.fol.parser.FolParser;
import net.sf.tweety.logics.fol.syntax.FolBeliefSet;
import net.sf.tweety.logics.fol.syntax.FolFormula;
import net.sf.tweety.logics.fol.syntax.FolSignature;
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
import xyz.aspectowl.tptp.reasoner.util.UnsortedTPTPWriter;
import xyz.aspectowl.tptp.util.Counter;

import javax.annotation.Nonnull;
import java.io.IOException;
import java.io.Writer;
import java.nio.charset.StandardCharsets;
import java.util.*;
import java.util.stream.Collectors;
import java.util.stream.IntStream;
import java.util.stream.Stream;

// TODO include ontology import closure (priority HIGH)
// TODO prevent name clashes (since we only use local names)
// TODO handle punnings (make unique names)  (priority MEDIUM) DONE
// TODO prevent name clashes between existing and generated predicate names (unlikely but who knows)  (priority LOW)


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
 * owl: owl built-entity names (owlThing and owlNothing)
 */
public class OWL2TPTPObjectRenderer extends OWLObjectVisitorExAdapter<Stream<FolFormula>> {

    private static final Logger log = LoggerFactory.getLogger(OWL2TPTPObjectRenderer.class);

    protected final Optional<OWLOntology> onto;
    private DefaultPrefixManager defaultPrefixManager;
    protected Optional<ShortFormProvider> labelMaker = Optional.empty();
    private Optional<PrefixManager> prefixManager = Optional.empty();
    private boolean writeEntitiesAsURIs = true;
    private boolean addMissingDeclarations = true;

    // Since TPTP only allows letters, numbers, and the underscore character in names we have to translate the OWL IRIs
    // to names containing only these characters and keep the mappings for later back translation.
    private HashMap<String, OWLEntity> nameMappings = new HashMap<>();

    private UnsortedTPTPWriter out;

    public FolParser getFolParser() {
        return folp;
    }

    private FolParser folp;

    public FolBeliefSet getBeliefSet() {
        return bs;
    }

    // There is a DlBeliefSet and I considered using it (somehow shove the OWL2 KB into a DLBeliefSet and then maybe
    // transform the latter to a FolBeliedSet) but it turned out DLBeliefSet only implements ALC while we need full
    // SROIQ(D).
    private FolBeliefSet bs;
    private FolSignature sig;

    public OWL2TPTPObjectRenderer(OWLOntology ontology, Writer writer) {
        super(Stream.empty());
        onto = Optional.ofNullable(ontology);
        out = new UnsortedTPTPWriter(writer);
        defaultPrefixManager = new DefaultPrefixManager();
        onto.ifPresent(o -> {
            OWLOntologyManager manager = o.getOWLOntologyManager();
            OWLDocumentFormat ontologyFormat = manager.getOntologyFormat(o);
            // reuse the setting on the existing format
            addMissingDeclarations = ontologyFormat.isAddMissingTypes();
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
     * Translates an IRI to a FOL constant name
     * @param iri
     * @return
     */
    private String translateIRI(HasIRI iri) {
        if (iri.getIRI().isThing())
            return "owlThing";
        if (iri.getIRI().isNothing())
            return "owlNothing";
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
        String localName = iri.getIRI().getShortForm().replaceFirst("^.*#(.+?)$", "$1"); // regex is a workaround for https://github.com/owlcs/owlapi/issues/699
        return String.format("%s%s", prefix, localName);
//        return iri.getIRI().toString();
    }

    @Override
    public Stream<FolFormula> visit(OWLNegativeObjectPropertyAssertionAxiom axiom) {
        return makeFormula("!%s(%s,%s)", translate(axiom.getProperty()), translate(axiom.getSubject()), translate(axiom.getObject()));
    }

    @Override
    public Stream<FolFormula> visit(OWLAsymmetricObjectPropertyAxiom axiom) {
        return makeFormula("forall X: (forall Y: (%1$s(X,Y) <=> !%1$s(Y,X)))", translate(axiom.getProperty()));
    }

    @Override
    public Stream<FolFormula> visit(OWLDisjointClassesAxiom axiom) {
        List<String> predicateNames = axiom.getClassExpressions().stream().map(ce -> translate(ce)).collect(Collectors.toList());
        return combinations(predicateNames, predicateNames).stream().flatMap(pair -> makeFormula("forall X: (!(%s(X) && %s(X)))", pair.getFirst(), pair.getSecond()));
    }

    @Override
    public Stream<FolFormula> visit(OWLObjectPropertyDomainAxiom axiom) {
        return makeFormula("forall X: (forall Y: (%s(X,Y) => %s(X)))", translate(axiom.getProperty()), translate(axiom.getDomain()));
    }

    @Override
    public Stream<FolFormula> visit(OWLEquivalentObjectPropertiesAxiom axiom) {
        StringBuilder buf = new StringBuilder("forall X: (forall Y: (");
        axiom.getProperties().stream().findFirst().ifPresent(ope -> buf.append(String.format("%s(X,Y)", translate(ope))));
        axiom.getProperties().stream().skip(1).forEach(ope -> buf.append(String.format(" <=> %s(X,Y)", translate(ope))));
        buf.append("))");
        return makeFormula(buf.toString());
    }

    @Override
    public Stream<FolFormula> visit(OWLDisjointObjectPropertiesAxiom axiom) {
        List<String> predicateNames = axiom.getProperties().stream().map(ce -> translate(ce)).collect(Collectors.toList());
        return combinations(predicateNames, predicateNames).stream().flatMap(pair -> makeFormula("forall X: (forall Y: (!(%s(X,Y) && %s(X,Y)))", pair.getFirst(), pair.getSecond()));
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
        var disjointnessFormulae = visit(axiom.getOWLDisjointClassesAxiom());
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
        StringBuilder buf = new StringBuilder();
        axiom.getIndividuals().stream().findFirst().ifPresent(ind -> buf.append(translate(ind)));
        axiom.getIndividuals().stream().skip(1).forEach(ind -> buf.append(String.format(" == %s", translate(ind))));
        return makeFormula(buf.toString());
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
        // TODO
        return null;
    }

    private String translate(OWLIndividual individual) {
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
    protected String translate(OWLClassExpression ce) {
        ClassExpressionType type = ce.getClassExpressionType();
        if (type == ClassExpressionType.OWL_CLASS)
            return translateIRI(ce.asOWLClass());

        String tempName = temporaryPredicate(ce);

        StringBuilder buf = new StringBuilder();
        buf.append("forall X: (%s(X) <=> ");

        switch (type) {
            case OBJECT_SOME_VALUES_FROM: {
                OWLObjectSomeValuesFrom svf = (OWLObjectSomeValuesFrom)ce;
                String opeTrans = translate(svf.getProperty());
                String fillerTrans = translate(svf.getFiller());

                buf.append(String.format("exists Y: (%s(X,Y) && %(Y))", opeTrans, fillerTrans));
                break;
            }
            case OBJECT_ALL_VALUES_FROM: {
                OWLObjectAllValuesFrom avf = (OWLObjectAllValuesFrom) ce;
                String opeTrans = translate(avf.getProperty());
                String fillerTrans = translate(avf.getFiller());

                buf.append(String.format("forall Y: (%s(X,Y) => %s(Y))", opeTrans, fillerTrans));
                break;
            }
            case OBJECT_MIN_CARDINALITY: {
                // forall X: (Temp(X) <=> exists Y1: (r(X,Y1) && Filler(Y1) &&
                //                        exists Y2: (r(X,Y2) && Filler(Y2) &&
                //                        .. &&
                //                        exists Yn: (r(X,Yn) && Filler(Yn) &&
                //                        Y1 /== Y2 /== ... /== Yn
                //                        ))...) [1..n]
                // )
                OWLObjectMinCardinality omc = (OWLObjectMinCardinality) ce;
                String opeTrans = translate(omc.getProperty());
                String fillerTrans = translate(omc.getFiller());
                int cardinality = omc.getCardinality();

                String fragment = "exists Y%d: (%s(X,Y%d) && %s(Y%d)";
                buf.append(String.format(fragment, 0, opeTrans, 0, fillerTrans, 0));
                IntStream.range(1, cardinality).forEach(i -> buf.append(" && ").append(String.format(fragment, i, opeTrans, i, fillerTrans, i)));

                List<Integer> indices = IntStream.range(0, cardinality).boxed().collect(Collectors.toList());
                HashSet<Pair<Integer, Integer>> combinations = combinations(indices, indices);
                combinations.stream().forEach(pair -> buf.append(String.format(" &&  Y%d /== Y%d", pair.getFirst(), pair.getSecond())));

                IntStream.range(0, cardinality).forEach(i -> buf.append(')')); // closing inner existential quantifications
                break;
            }
            case OBJECT_MAX_CARDINALITY: {
                // forall X: (Temp(X) <=> exists Y1: (r(X,Y1) && Filler(Y1) &&
                //                        exists Y2: (r(X,Y2) && Filler(Y2) &&
                //                        .. &&
                //                        exists Yn: (r(X,Yn) && Filler(Yn) &&
                //                        forall Z: (Z /== Y1 /== Y2 /== .. /== Yn => !r(X,Z))
                //                        ))...) [1..n]
                // )
                OWLObjectMaxCardinality omc = (OWLObjectMaxCardinality) ce;
                String opeTrans = translate(omc.getProperty());
                String fillerTrans = translate(omc.getFiller());
                int cardinality = omc.getCardinality();

                String fragment = "exists Y%d: (%s(X,Y%d) && %s(Y%d)";
                buf.append(String.format(fragment, 0, opeTrans, 0, fillerTrans, 0));
                IntStream.range(1, cardinality).forEach(i -> buf.append(" && ").append(String.format(fragment, i, opeTrans, i, fillerTrans, i)));

                buf.append(" && forall Z: ((Z /== Y0 ");
                IntStream.range(1, cardinality).forEach(i -> buf.append(String.format(" && Z /== Y%d", i)));
                buf.append(String.format(") => !%s(X,Z))", opeTrans));

                IntStream.range(0, cardinality).forEach(i -> buf.append(')')); // closing inner existential quantifications
                break;
            }
            case OBJECT_EXACT_CARDINALITY: {
                //
                // forall X: (Temp(X) <=> exists Y1: (r(X,Y1) && Filler(Y1) &&
                //                        exists Y2: (r(X,Y2) && Filler(Y2) &&
                //                        .. &&
                //                        exists Yn: (r(X,Yn) && Filler(Yn) &&
                //                        Y1 /== Y2 /== ... /== Yn &&
                //                        forall Z: (Z /== Y1 /== Y2 /== .. /== Yn => !r(X,Z))
                //                        ))...) [1..n]
                // )
                OWLObjectExactCardinality oec = (OWLObjectExactCardinality) ce;
                String opeTrans = translate(oec.getProperty());
                String fillerTrans = translate(oec.getFiller());
                int cardinality = oec.getCardinality();

                String fragment = "exists Y%d: (%s(X,Y%d) && %s(Y%d)";
                buf.append(String.format(fragment, 0, opeTrans, 0, fillerTrans, 0));
                IntStream.range(1, cardinality).forEach(i -> buf.append(" && ").append(String.format(fragment, i, opeTrans, i, fillerTrans, i)));

                List<Integer> indices = IntStream.range(0, cardinality).boxed().collect(Collectors.toList());
                HashSet<Pair<Integer, Integer>> combinations = combinations(indices, indices);
                combinations.stream().forEach(pair -> buf.append(String.format(" &&  Y%d /== Y%d", pair.getFirst(), pair.getSecond())));

                buf.append(" && forall Z: ((Z /== Y0 ");
                IntStream.range(1, cardinality).forEach(i -> buf.append(String.format(" && Z /== Y%d", i)));
                buf.append(String.format(") => !%s(X,Z))", opeTrans));

                IntStream.range(0, cardinality).forEach(i -> buf.append(')')); // closing inner existential quantifications
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
        makeFormula(buf.toString(), tempName).forEach(bs::add);
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

    private String temporaryPredicate(OWLObject o) {
        String tempName = UUID.nameUUIDFromBytes(o.toString().getBytes(StandardCharsets.UTF_8)).toString()
                .replaceAll("-", "");
        if(!sig.containsPredicate(tempName)) {
            if (o instanceof OWLClassExpression) {
                tempName = String.format("tc%s", tempName);
                sig.add(new Predicate(tempName, 1));
            } else if (o instanceof OWLObjectPropertyExpression) {
                tempName = String.format("to%s", tempName);
                sig.add(new Predicate(tempName, 2));
            } else {
                throw new IllegalArgumentException("Can only make temporary predicated out of classes or object properties.");
            }
            bs.setSignature(sig);
        }
        return tempName;
    }

    private String translate(OWLObjectPropertyExpression ope) {
        if (ope.isNamed()) {
            return translateIRI(ope.asOWLObjectProperty());
        }

        // the only possible anonymous object property expression is an inverse property expression
        OWLObjectProperty op = ((OWLObjectInverseOf)ope).getInverse().asOWLObjectProperty();
        String tempName = temporaryPredicate(op);
        makeFormula("forall X: (forall Y: (%s(X,Y) <=> %s(Y,X)))", translateIRI(op), tempName).forEach(formula -> bs.add(formula));

        return tempName;
    }

    /**
     * Always returns a stream containing *exactly one* formula.
     * @param format a formatted TPTP string
     * @param args the arguments for the formatted string
     * @return A stream containing *exactly one* formula.
     */
    protected Stream<FolFormula> makeFormula(String format, Object... args) {
        try {
            return Stream.of((FolFormula)folp.parseFormula(String.format(format, args)));
        } catch (IOException e) {
            throw new ParserException(e);
        }
    }

    @Override
    public Stream<FolFormula> visit(OWLOntology ontology) {
        // signature
        sig = new FolSignature(true);
        sig.add(new Predicate("owlThing", 1));
        sig.add(new Predicate("owlNothing", 1));
        ontology.getSignature(Imports.INCLUDED).forEach(entity -> entity.accept(this));

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
        ontology.getAxioms().stream().forEach(
                axiom -> axiom.accept(this).forEach(
                        folFormula -> bs.add(folFormula)
                )
        );

        try {
            out.printBase(bs);
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
        return bs.stream();
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
        String predicateName = translateIRI(axiom.getProperty().asOWLObjectProperty());
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
        List<OWLClassExpression> ces = axiom.getClassExpressions().stream().collect(Collectors.toList());
        StringBuilder buf = new StringBuilder();
        Optional.of(ces.get(0)).ifPresent(ce -> buf.append(String.format("forall X: (%s(X)", translate(ce))));
        ces.stream().skip(1).forEach(ce -> buf.append(String.format(" <=> %s(X)", translate(ce))));
        buf.append(')');
        return makeFormula(buf.toString());
    }

    @Nonnull
    @Override
    protected Stream<FolFormula> doDefault(@Nonnull OWLObject object) {
        log.warn("Not (yet) implemented: {}\n", object);
        return Stream.empty();
    }
}
