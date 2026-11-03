package xyz.aspectowl.tptp.renderer;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.MethodSource;
import org.semanticweb.owlapi.apibinding.OWLManager;
import org.semanticweb.owlapi.model.*;
import org.semanticweb.owlapi.model.parameters.Imports;
import org.tweetyproject.logics.commons.syntax.Variable;
import org.tweetyproject.logics.fol.parser.TPTPParser;
import org.tweetyproject.logics.fol.syntax.*;

import java.io.StringWriter;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.*;
import java.util.function.Predicate;
import java.util.stream.Stream;

import static org.junit.jupiter.api.Assertions.*;

class OWLRendererTest {
    private static Stream<String> ontologyFiles() throws Exception {
        try (var paths = Files.list(Path.of(OWLRendererTest.class.getResource("/reasoner/ontologies").toURI()))) {
            return paths.map(p -> p.getFileName().toString()).filter(p -> p.endsWith(".ofn")).sorted().toList().stream();
        }
    }

    @ParameterizedTest
    @MethodSource("ontologyFiles")
    void rendersExistingOwlFixtures(String file) throws Exception {
        OWLOntologyManager manager = OWLManager.createOWLOntologyManager();
        manager.loadOntologyFromOntologyDocument(getClass().getResourceAsStream("/reasoner/ontologies/test-base.ofn"));
        OWLOntology ontology = file.equals("test-base.ofn") ? manager.getOntologies().iterator().next()
                : manager.loadOntologyFromOntologyDocument(getClass().getResourceAsStream("/reasoner/ontologies/" + file));
        StringWriter output = new StringWriter();
        var renderer = new OWL2TPTPObjectRenderer(ontology, output, Imports.INCLUDED);
        ontology.accept(renderer);
        assertTrue(renderer.getBeliefSet().stream().allMatch(FolFormula::isClosed));
        TPTPParser parser = new TPTPParser();
        parser.setFormulaRoles(".*");
        parser.getSignature().addSignature(renderer.getBeliefSet().getMinimalSignature());
        assertEquals(renderer.getBeliefSet().size(), parser.parseBeliefBase(output.toString()).size());
    }

    @Test
    void cardinalitiesCountOnlyDistinctMatchingSuccessorsAndAllowZero() throws Exception {
        OWLOntologyManager manager = OWLManager.createOWLOntologyManager();
        var df = manager.getOWLDataFactory();
        var r = df.getOWLObjectProperty(IRI.create("urn:test:r"));
        var a = df.getOWLClass(IRI.create("urn:test:A"));
        for (int n = 0; n <= 2; n++) {
            for (String kind : List.of("min", "max", "exact")) {
                OWLClassExpression restriction = switch (kind) {
                    case "min" -> df.getOWLObjectMinCardinality(n, r, a);
                    case "max" -> df.getOWLObjectMaxCardinality(n, r, a);
                    default -> df.getOWLObjectExactCardinality(n, r, a);
                };
                OWLOntology ontology = manager.createOntology();
                manager.addAxiom(ontology, df.getOWLSubClassOfAxiom(a, restriction));
                var renderer = render(ontology, Imports.EXCLUDED);
                FolFormula condition = renderer.getBeliefSet().stream().map(this::matrix)
                        .filter(f -> f instanceof Equivalence).map(f -> (FolFormula) ((Equivalence) f).getFormulas().getSecond())
                        .findFirst().orElseThrow();
                for (int matching = 0; matching <= 3; matching++) {
                    int count = matching;
                    // r has four successors, only 'matching' of which satisfy A.
                    Predicate<FolAtom> interpretation = atom -> switch (atom.getPredicate().getName()) {
                        case "no_r" -> true;
                        case "nc_A" -> Integer.parseInt(atom.getArguments().getFirst().toString().substring(1)) < count;
                        default -> throw new AssertionError(atom);
                    };
                    boolean expected = switch (kind) {
                        case "min" -> matching >= n;
                        case "max" -> matching <= n;
                        default -> matching == n;
                    };
                    assertEquals(expected, evaluate(condition, Map.of(new Variable("X"), 0), interpretation),
                            kind + " " + n + " with " + matching + " matching successors");
                }
                manager.removeOntology(ontology);
            }
        }
    }

    @Test
    void rendersAsymmetryDisjointPropertiesAndNaryEquivalences() throws Exception {
        OWLOntologyManager manager = OWLManager.createOWLOntologyManager();
        var df = manager.getOWLDataFactory();
        OWLOntology ontology = manager.createOntology();
        var p = df.getOWLObjectProperty(IRI.create("urn:test:p"));
        var q = df.getOWLObjectProperty(IRI.create("urn:test:q"));
        var r = df.getOWLObjectProperty(IRI.create("urn:test:r"));
        manager.addAxiom(ontology, df.getOWLAsymmetricObjectPropertyAxiom(p));
        manager.addAxiom(ontology, df.getOWLDisjointObjectPropertiesAxiom(p, q, r));
        manager.addAxiom(ontology, df.getOWLEquivalentObjectPropertiesAxiom(p, q, r));
        var a = df.getOWLClass(IRI.create("urn:test:A"));
        var b = df.getOWLClass(IRI.create("urn:test:B"));
        var c = df.getOWLClass(IRI.create("urn:test:C"));
        manager.addAxiom(ontology, df.getOWLEquivalentClassesAxiom(a, b, c));
        var i = df.getOWLNamedIndividual(IRI.create("urn:test:i"));
        var j = df.getOWLNamedIndividual(IRI.create("urn:test:j"));
        var k = df.getOWLNamedIndividual(IRI.create("urn:test:k"));
        manager.addAxiom(ontology, df.getOWLSameIndividualAxiom(i, j, k));
        var renderer = render(ontology, Imports.EXCLUDED);
        FolBeliefSet beliefs = renderer.getBeliefSet();
        assertTrue(containsFormula(beliefs, renderer.getFolParser().parseFormula("forall X: (forall Y: (no_p(X,Y) => !no_p(Y,X)))")), beliefs.toString());
        assertEquals(3, beliefs.stream().map(this::matrix).filter(f -> f instanceof Negation negation
                && negation.getFormula() instanceof Conjunction).count());
        assertEquals(4, beliefs.stream().map(this::matrix).filter(f -> f instanceof Equivalence).count());
        assertEquals(2, beliefs.stream().filter(f -> f instanceof FolAtom atom && atom.getPredicate() instanceof EqualityPredicate).count());
    }

    @Test
    void inversePropertiesUseTheInverseExpressionAndDefinitionsAreNotDuplicated() throws Exception {
        OWLOntologyManager manager = OWLManager.createOWLOntologyManager();
        var df = manager.getOWLDataFactory();
        OWLOntology ontology = manager.createOntology();
        var p = df.getOWLObjectProperty(IRI.create("urn:test:p"));
        var a = df.getOWLClass(IRI.create("urn:test:A"));
        var b = df.getOWLClass(IRI.create("urn:test:B"));
        var restriction = df.getOWLObjectSomeValuesFrom(p.getInverseProperty(), a);
        manager.addAxiom(ontology, df.getOWLSubClassOfAxiom(a, restriction));
        manager.addAxiom(ontology, df.getOWLSubClassOfAxiom(b, restriction));
        manager.addAxiom(ontology, df.getOWLTransitiveObjectPropertyAxiom(p.getInverseProperty()));
        var renderer = render(ontology, Imports.EXCLUDED);
        assertEquals(2, renderer.getBeliefSet().stream().map(this::matrix).filter(f -> f instanceof Equivalence).count());
        String inverse = renderer.translate(p.getInverseProperty());
        assertTrue(containsFormula(renderer.getBeliefSet(), renderer.getFolParser().parseFormula(
                "forall X: (forall Y: (" + inverse + "(X,Y) <=> no_p(Y,X)))")), renderer.getBeliefSet().toString());
    }

    @Test
    void respectsImportsInBothSignatureAndAxioms() throws Exception {
        OWLOntologyManager manager = OWLManager.createOWLOntologyManager();
        var df = manager.getOWLDataFactory();
        OWLOntology root = manager.createOntology(IRI.create("urn:test:root"));
        OWLOntology imported = manager.createOntology(IRI.create("urn:test:imported"));
        manager.applyChange(new AddImport(root, df.getOWLImportsDeclaration(IRI.create("urn:test:imported"))));
        manager.addAxiom(imported, df.getOWLClassAssertionAxiom(df.getOWLClass(IRI.create("urn:test:A")),
                df.getOWLNamedIndividual(IRI.create("urn:test:i"))));
        var excluded = render(root, Imports.EXCLUDED);
        var included = render(root, Imports.INCLUDED);
        assertFalse(excluded.getBeliefSet().getMinimalSignature().containsPredicate("nc_A"));
        assertEquals(2, excluded.getBeliefSet().size());
        assertTrue(containsFormula(included.getBeliefSet(), included.getFolParser().parseFormula("nc_A(ni_i)")));
    }

    @Test
    void rejectsUnsupportedLogicalAxiomsInsteadOfDroppingThem() throws Exception {
        OWLOntologyManager manager = OWLManager.createOWLOntologyManager();
        var df = manager.getOWLDataFactory();
        OWLOntology ontology = manager.createOntology();
        manager.addAxiom(ontology, df.getOWLFunctionalDataPropertyAxiom(df.getOWLDataProperty(IRI.create("urn:test:p"))));
        assertThrows(UnsupportedOperationException.class, () -> render(ontology, Imports.EXCLUDED));
    }

    @Test
    void readableSymbolsRemainValidAndDistinctWithPunningAndClashingShortForms() throws Exception {
        OWLOntologyManager manager = OWLManager.createOWLOntologyManager();
        var df = manager.getOWLDataFactory();
        OWLOntology ontology = manager.createOntology();
        var first = df.getOWLClass(IRI.create("http://first.example/#A-B"));
        var second = df.getOWLClass(IRI.create("http://second.example/#A-B"));
        var individual = df.getOWLNamedIndividual(first.getIRI());
        manager.addAxiom(ontology, df.getOWLDisjointClassesAxiom(first, second));
        manager.addAxiom(ontology, df.getOWLClassAssertionAxiom(first, individual));
        var renderer = render(ontology, Imports.EXCLUDED);
        var signature = renderer.getBeliefSet().getMinimalSignature();
        assertEquals(4, signature.getPredicates().size());
        assertEquals(1, signature.getConstants().size());
        signature.getPredicates().forEach(p -> assertTrue(p.getName().matches("[a-z][a-zA-Z0-9_]*")));
    }

    private boolean containsFormula(FolBeliefSet beliefSet, FolFormula expected) {
        // Tweety 1.30 quantified formulae override equals without hashCode.
        return beliefSet.stream().anyMatch(expected::equals);
    }

    private OWL2TPTPObjectRenderer render(OWLOntology ontology, Imports imports) {
        var renderer = new OWL2TPTPObjectRenderer(ontology, new StringWriter(), imports);
        renderer.setHumanReadableTptpNames(true);
        ontology.accept(renderer);
        return renderer;
    }

    private FolFormula matrix(FolFormula formula) {
        while (formula instanceof ForallQuantifiedFormula)
            formula = (FolFormula) formula.getFormula();
        return formula;
    }

    /** Small finite interpretation used to verify counting semantics independently of a prover. */
    private boolean evaluate(FolFormula formula, Map<Variable, Integer> assignment, Predicate<FolAtom> interpretation) {
        if (formula instanceof Tautology) return true;
        if (formula instanceof Contradiction) return false;
        if (formula instanceof Negation negation) return !evaluate(negation.getFormula(), assignment, interpretation);
        if (formula instanceof Conjunction conjunction)
            return conjunction.getFormulas().stream().allMatch(f -> evaluate((FolFormula) f, assignment, interpretation));
        if (formula instanceof Disjunction disjunction)
            return disjunction.getFormulas().stream().anyMatch(f -> evaluate((FolFormula) f, assignment, interpretation));
        if (formula instanceof ExistsQuantifiedFormula) {
            Variable variable = formula.getQuantifierVariables().iterator().next();
            for (int value = 0; value < 4; value++) {
                Map<Variable, Integer> next = new HashMap<>(assignment);
                next.put(variable, value);
                if (evaluate((FolFormula) formula.getFormula(), next, interpretation)) return true;
            }
            return false;
        }
        FolAtom atom = (FolAtom) formula;
        if (atom.getPredicate() instanceof EqualityPredicate || atom.getPredicate() instanceof InequalityPredicate) {
            boolean equal = Objects.equals(assignment.get(atom.getArguments().getFirst()), assignment.get(atom.getArguments().getLast()));
            return atom.getPredicate() instanceof EqualityPredicate ? equal : !equal;
        }
        FolAtom grounded = atom;
        for (var entry : assignment.entrySet())
            grounded = grounded.substitute(entry.getKey(), new org.tweetyproject.logics.commons.syntax.Constant("d" + entry.getValue()));
        return interpretation.test(grounded);
    }
}
