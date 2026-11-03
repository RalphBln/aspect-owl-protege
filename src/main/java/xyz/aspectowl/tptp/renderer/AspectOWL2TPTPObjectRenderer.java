package xyz.aspectowl.tptp.renderer;

import org.tweetyproject.logics.fol.syntax.*;
import org.tweetyproject.logics.fol.parser.FolParser;
import org.tweetyproject.logics.commons.syntax.Predicate;
import org.tweetyproject.logics.commons.syntax.Variable;
import org.tweetyproject.logics.commons.syntax.interfaces.Term;
import org.semanticweb.owlapi.model.*;
import org.semanticweb.owlapi.model.parameters.Imports;

import java.nio.charset.StandardCharsets;
import java.util.*;
import java.io.Writer;
import java.util.stream.Stream;

/**
 * @author ralph
 */
public abstract class AspectOWL2TPTPObjectRenderer extends OWL2TPTPObjectRenderer implements AspectOWLObjectRenderer<Stream<FolFormula>> {

    public AspectOWL2TPTPObjectRenderer(OWLOntology ontology, Writer writer, Imports includeImportsClosure) {
        super(ontology, writer, includeImportsClosure);
    }

    /** A context may itself be qualified by contexts, at any nesting depth. */
    protected record AspectContext(OWLClassExpression expression, List<AspectContext> nested) {
        public AspectContext {
            nested = nested.stream().sorted(Comparator.comparing(Object::toString)).toList();
        }
    }

    private record ContextPredicate(String name, Variable variable,
                                    List<ContextPredicate> nested) {}
    private record ScopedFormula(FolFormula formula, ContextPredicate context) {}

    private final Map<AspectContext, ContextPredicate> contexts = new LinkedHashMap<>();
    private final List<ScopedFormula> scopedFormulae = new ArrayList<>();
    private boolean collecting;
    private FolParser contextualParser;
    private FolAtom contextGuard;
    private Set<Variable> guardVariables;

    @Override
    public FolParser getFolParser() {
        return contextualParser == null ? super.getFolParser() : contextualParser;
    }

    /** Returns contexts in the ontology currently being visited (including imported ontologies). */
    protected abstract List<AspectContext> getAspectContexts(OWLAxiom axiom);

    @Override
    public Stream<FolFormula> visit(OWLOntology ontology) {
        contexts.clear();
        scopedFormulae.clear();
        contextualParser = null;
        contextGuard = null;
        guardVariables = Set.of();
        collecting = true;
        try {
            return super.visit(ontology);
        } finally {
            collecting = false;
        }
    }

    @Override
    public boolean hasAspect(OWLAxiom axiom) {
        return !getAspectContexts(axiom).isEmpty();
    }

    @Override
    public Stream<FolFormula> handleAspects(OWLAxiom axiom, Stream<FolFormula> formulae) {
        List<AspectContext> aspects = getAspectContexts(axiom);
        if (aspects.isEmpty())
            return collecting ? formulae : formulae.map(this::inEveryContext);
        if (!collecting) {
            List<FolFormula> translated = formulae.toList();
            return aspects.stream().flatMap(aspect -> {
                ContextPredicate context = contexts.get(aspect);
                if (context == null)
                    throw new IllegalArgumentException("Render the ontology before querying a new aspect context: " + aspect);
                return translated.stream().map(formula -> inAspectContext(formula, context));
            });
        }
        // A stream cannot be traversed once for each aspect or each translated axiom part.
        List<FolFormula> translated = formulae.toList();
        for (AspectContext aspect : aspects) {
            ContextPredicate context = registerContext(aspect);
            translated.forEach(formula -> scopedFormulae.add(new ScopedFormula(formula, context)));
        }
        return Stream.empty();
    }

    private ContextPredicate registerContext(AspectContext aspect) {
        ContextPredicate existing = contexts.get(aspect);
        if (existing != null)
            return existing;
        OWLClassExpression expression = aspect.expression();
        // Advice entities need not occur in the join point's OWL signature.
        expression.getSignature().forEach(entity -> entity.accept(this));
        String base = translate(expression);
        List<ContextPredicate> nested = aspect.nested().stream().map(this::registerContext).toList();
        if (!nested.isEmpty())
            registerContext(new AspectContext(expression, List.of()));
        String name = nested.isEmpty() ? base : "ctx_" + UUID.nameUUIDFromBytes(
                aspect.toString().getBytes(StandardCharsets.UTF_8)).toString().replace("-", "");
        ContextPredicate context = new ContextPredicate(name, new Variable("C" + (contexts.size() + 1)), nested);
        contexts.put(aspect, context);
        return context;
    }

    @Override
    protected void finishRendering() {
        FolBeliefSet contextualized = new FolBeliefSet();
        for (FolFormula formula : bs) {
            contextualized.add(inEveryContext(formula));
        }
        Variable top = new Variable("CT");
        contextualized.add(forall(atom("ctxT", top), Set.of(top)));
        if (!contexts.isEmpty()) {
            // One atomic guard binds all context identifiers, rather than an unguarded
            // conjunction or a universally true guard (which would undo the restriction).
            Set<Variable> variables = new LinkedHashSet<>();
            variables.add(new Variable("X"));
            variables.add(new Variable("Y"));
            for (ScopedFormula scoped : scopedFormulae)
                variables.addAll(outerVariables(scoped.formula()));
            variables.add(top);
            contexts.values().forEach(context -> variables.add(context.variable()));
            FolAtom guard = atom("ctxBnd", variables.toArray(Term<?>[]::new));
            contextGuard = guard;
            guardVariables = variables;
            List<FolFormula> memberships = new ArrayList<>();
            memberships.add(atom("ctxT", top));
            contexts.values().forEach(context -> memberships.add(atom(context.name(), context.variable(), top)));
            contextualized.add(forall(new Implication(guard, new Conjunction(memberships)), variables));
            contextualized.add(new ExistsQuantifiedFormula(guard, variables));
            for (var entry : contexts.entrySet()) {
                ContextPredicate context = entry.getValue();
                if (!context.nested().isEmpty()) {
                    Variable x = new Variable("X");
                    ContextPredicate base = contexts.get(new AspectContext(
                            entry.getKey().expression(), List.of()));
                    List<FolFormula> parts = new ArrayList<>();
                    parts.add(atom(base.name(), x, base.variable()));
                    context.nested().forEach(nested -> parts.add(atom(nested.name(), base.variable(), nested.variable())));
                    contextualized.add(forall(new Implication(guard, new Implication(
                            atom(context.name(), x, context.variable()), new Conjunction(parts))), variables));
                }
            }
            for (ScopedFormula scoped : scopedFormulae) {
                contextualized.add(inAspectContext(scoped.formula(), scoped.context()));
            }
        }
        FolSignature signature = new FolSignature(true);
        for (Predicate predicate : sig.getPredicates()) {
            if (!(predicate instanceof EqualityPredicate || predicate instanceof InequalityPredicate))
                signature.add(new Predicate(predicate.getName(), predicate.getArity() + 1));
        }
        signature.addAll(sig.getConstants());
        signature.addSignature(contextualized.getMinimalSignature());
        bs = contextualized;
        bs.setSignature(signature);
        // Keep the raw OWL translation parser/signature separate so that subsequent
        // axiom visits (e.g. entailment queries) can still build contextual formulae.
        contextualParser = new FolParser(false);
        contextualParser.setSignature(signature);
    }

    @Override
    protected void addDefinition(FolFormula formula) {
        if (collecting) {
            super.addDefinition(formula);
        } else {
            FolFormula contextualized = inEveryContext(formula);
            bs.add(contextualized);
            contextualParser.getSignature().addSignature(contextualized.getSignature());
        }
    }

    private FolFormula inEveryContext(FolFormula formula) {
        Variable context = freshVariable("C", formula);
        return forall(contextualize(formula, context), Set.of(context));
    }

    private FolFormula inAspectContext(FolFormula formula, ContextPredicate context) {
        if (!guardVariables.containsAll(outerVariables(formula)))
            throw new IllegalArgumentException("Axiom needs variables absent from the ontology's context guard: " + formula);
        while (formula instanceof ForallQuantifiedFormula)
            formula = (FolFormula) formula.getFormula();
        return forall(new Implication(contextGuard, contextualize(formula, context.variable())), guardVariables);
    }

    private Set<Variable> outerVariables(FolFormula formula) {
        Set<Variable> variables = new LinkedHashSet<>();
        while (formula instanceof ForallQuantifiedFormula) {
            variables.addAll(formula.getQuantifierVariables());
            formula = (FolFormula) formula.getFormula();
        }
        variables.addAll(formula.getUnboundVariables());
        return variables;
    }

    private Variable freshVariable(String name, FolFormula formula) {
        Set<Variable> variables = formula.getTerms(Variable.class);
        while (variables.contains(new Variable(name)))
            name += "C";
        return new Variable(name);
    }

    private FolAtom atom(String name, Term<?>... terms) {
        return new FolAtom(new Predicate(name, terms.length), terms);
    }

    private FolFormula forall(FolFormula formula, Set<Variable> variables) {
        return variables.isEmpty() ? formula : new ForallQuantifiedFormula(formula, variables);
    }

    /** Adds a context to relational atoms, preserving equality and quantifier scope. */
    private FolFormula contextualize(FolFormula formula, Variable context) {
        if (formula instanceof FolAtom atom) {
            if (atom.getPredicate() instanceof EqualityPredicate || atom.getPredicate() instanceof InequalityPredicate)
                return atom;
            List<Term<?>> arguments = new ArrayList<>(atom.getArguments());
            arguments.add(context);
            return new FolAtom(new Predicate(atom.getPredicate().getName(), arguments.size()), arguments);
        }
        if (formula instanceof Negation negation)
            return new Negation(contextualize(negation.getFormula(), context));
        if (formula instanceof Implication implication)
            return new Implication(contextualize((FolFormula) implication.getFormulas().getFirst(), context),
                    contextualize((FolFormula) implication.getFormulas().getSecond(), context));
        if (formula instanceof Equivalence equivalence)
            return new Equivalence(contextualize((FolFormula) equivalence.getFormulas().getFirst(), context),
                    contextualize((FolFormula) equivalence.getFormulas().getSecond(), context));
        if (formula instanceof AssociativeFolFormula associative) {
            List<FolFormula> parts = associative.getFormulas().stream()
                    .map(part -> contextualize((FolFormula) part, context)).toList();
            return formula instanceof Conjunction ? new Conjunction(parts) : new Disjunction(parts);
        }
        if (formula instanceof ForallQuantifiedFormula)
            return new ForallQuantifiedFormula(contextualize((FolFormula) formula.getFormula(), context), formula.getQuantifierVariables());
        if (formula instanceof ExistsQuantifiedFormula)
            return new ExistsQuantifiedFormula(contextualize((FolFormula) formula.getFormula(), context), formula.getQuantifierVariables());
        if (formula instanceof Tautology || formula instanceof Contradiction)
            return formula;
        throw new IllegalArgumentException("Cannot contextualize formula: " + formula);
    }

    @Override
    public Stream<FolFormula> visit(OWLNegativeObjectPropertyAssertionAxiom axiom) {
        return handleAspects(axiom, super.visit(axiom));
    }

    @Override
    public Stream<FolFormula> visit(OWLAsymmetricObjectPropertyAxiom axiom) {
        return handleAspects(axiom, super.visit(axiom));
    }

    @Override
    public Stream<FolFormula> visit(OWLDisjointClassesAxiom axiom) {
        return handleAspects(axiom, super.visit(axiom));
    }

    @Override
    public Stream<FolFormula> visit(OWLObjectPropertyDomainAxiom axiom) {
        return handleAspects(axiom, super.visit(axiom));
    }

    @Override
    public Stream<FolFormula> visit(OWLEquivalentObjectPropertiesAxiom axiom) {
        return handleAspects(axiom, super.visit(axiom));
    }

    @Override
    public Stream<FolFormula> visit(OWLDisjointObjectPropertiesAxiom axiom) {
        return handleAspects(axiom, super.visit(axiom));
    }

    @Override
    public Stream<FolFormula> visit(OWLObjectPropertyRangeAxiom axiom) {
        return handleAspects(axiom, super.visit(axiom));
    }

    @Override
    public Stream<FolFormula> visit(OWLFunctionalObjectPropertyAxiom axiom) {
        return handleAspects(axiom, super.visit(axiom));
    }

    @Override
    public Stream<FolFormula> visit(OWLSubObjectPropertyOfAxiom axiom) {
        return handleAspects(axiom, super.visit(axiom));
    }

    @Override
    public Stream<FolFormula> visit(OWLDisjointUnionAxiom axiom) {
        return handleAspects(axiom, super.visit(axiom));
    }

    @Override
    public Stream<FolFormula> visit(OWLSymmetricObjectPropertyAxiom axiom) {
        return handleAspects(axiom, super.visit(axiom));
    }

    @Override
    public Stream<FolFormula> visit(OWLAnnotationAssertionAxiom axiom) {
        return handleAspects(axiom, super.visit(axiom));
    }

    @Override
    public Stream<FolFormula> visit(OWLIrreflexiveObjectPropertyAxiom axiom) {
        return handleAspects(axiom, super.visit(axiom));
    }

    @Override
    public Stream<FolFormula> visit(OWLInverseFunctionalObjectPropertyAxiom axiom) {
        return handleAspects(axiom, super.visit(axiom));
    }

    @Override
    public Stream<FolFormula> visit(OWLSameIndividualAxiom axiom) {
        return handleAspects(axiom, super.visit(axiom));
    }

    @Override
    public Stream<FolFormula> visit(OWLSubPropertyChainOfAxiom axiom) {
        return handleAspects(axiom, super.visit(axiom));
    }

    @Override
    public Stream<FolFormula> visit(OWLInverseObjectPropertiesAxiom axiom) {
        return handleAspects(axiom, super.visit(axiom));
    }

    @Override
    public Stream<FolFormula> visit(OWLHasKeyAxiom axiom) {
        return handleAspects(axiom, super.visit(axiom));
    }

    @Override
    public Stream<FolFormula> visit(OWLDifferentIndividualsAxiom axiom) {
        return handleAspects(axiom, super.visit(axiom));
    }

    @Override
    public Stream<FolFormula> visit(OWLClassAssertionAxiom axiom) {
        return handleAspects(axiom, super.visit(axiom));
    }

    @Override
    public Stream<FolFormula> visit(OWLObjectPropertyAssertionAxiom axiom) {
        return handleAspects(axiom, super.visit(axiom));
    }

    @Override
    public Stream<FolFormula> visit(OWLTransitiveObjectPropertyAxiom axiom) {
        return handleAspects(axiom, super.visit(axiom));
    }

    @Override
    public Stream<FolFormula> visit(OWLReflexiveObjectPropertyAxiom axiom) {
        return handleAspects(axiom, super.visit(axiom));
    }

    @Override
    public Stream<FolFormula> visit(OWLSubClassOfAxiom axiom) {
        return handleAspects(axiom, super.visit(axiom));
    }

    @Override
    public Stream<FolFormula> visit(OWLEquivalentClassesAxiom axiom) {
        return handleAspects(axiom, super.visit(axiom));
    }
}
