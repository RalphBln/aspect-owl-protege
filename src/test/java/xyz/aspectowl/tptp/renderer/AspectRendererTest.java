package xyz.aspectowl.tptp.renderer;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.dataformat.yaml.YAMLFactory;
import org.junit.jupiter.api.Test;
import org.semanticweb.owlapi.apibinding.OWLManager;
import org.semanticweb.owlapi.model.*;
import org.semanticweb.owlapi.model.parameters.Imports;
import org.tweetyproject.logics.commons.syntax.Variable;
import org.tweetyproject.logics.fol.parser.TPTPParser;
import org.tweetyproject.logics.fol.syntax.*;
import xyz.aspectowl.owlapi.model.OWLAspectManager;
import xyz.aspectowl.owlapi.model.AspectOWLJoinPointAxiomPointcut;
import xyz.aspectowl.parser.AspectOWLFunctionalSyntaxParser;
import xyz.aspectowl.parser.StreamProvider;
import xyz.aspectowl.reasoner.ConcreteAspectOWL2TPTPObjectRenderer;
import xyz.aspectowl.tptp.decidability.TriguardedFragmentChecker;

import java.io.InputStreamReader;
import java.io.StringWriter;
import java.nio.charset.StandardCharsets;
import java.util.*;
import java.util.stream.Collectors;

import static org.junit.jupiter.api.Assertions.*;

class AspectRendererTest {
    private record Rendered(ConcreteAspectOWL2TPTPObjectRenderer renderer, String text) {}

    private Rendered renderNative(String resource) throws Exception {
        OWLOntologyManager manager = OWLManager.createOWLOntologyManager();
        manager.loadOntologyFromOntologyDocument(getClass().getResourceAsStream("/reasoner/ontologies/aspect-base.ofn"));
        manager.loadOntologyFromOntologyDocument(getClass().getResourceAsStream("/reasoner/ontologies/test-base.ofn"));
        OWLOntology ontology = manager.createOntology();
        OWLAspectManager aspects = new OWLAspectManager();
        try (var reader = new InputStreamReader(getClass().getResourceAsStream(resource), StandardCharsets.UTF_8)) {
            var parser = new AspectOWLFunctionalSyntaxParser(new StreamProvider(reader));
            parser.setUp(ontology, new OWLOntologyLoaderConfiguration(), aspects);
            manager.setOntologyFormat(ontology, parser.parse());
        }
        StringWriter text = new StringWriter();
        var renderer = new ConcreteAspectOWL2TPTPObjectRenderer(aspects, ontology, text, Imports.INCLUDED);
        renderer.setHumanReadableTptpNames(true);
        ontology.accept(renderer);
        return new Rendered(renderer, text.toString());
    }

    @Test
    void plutoMatchesReferenceContextStructure() throws Exception {
        Rendered rendered = renderNative("/reasoner/ontologies/test-pluto.aofn");
        FolBeliefSet beliefSet = rendered.renderer().getBeliefSet();
        assertFalse(beliefSet.isEmpty());
        assertTrue(beliefSet.stream().allMatch(FolFormula::isClosed));
        assertEquals(beliefSet.size(), parseOutput(rendered.text(), beliefSet).size());

        // Discover the names and context identifiers rather than depending on generated UUIDs.
        Map<String, String> names = new HashMap<>(Map.of(
                "nc_Planet", "planet", "nc_DwarfPlanet", "dwarfplanet", "ni_Pluto", "pluto"));
        Map<String, String> variables = new HashMap<>();
        FolAtom planet = scopedAssertion(beliefSet, "nc_Planet");
        FolAtom dwarf = scopedAssertion(beliefSet, "nc_DwarfPlanet");
        variables.put(planet.getArguments().getLast().toString(), "C1");
        variables.put(dwarf.getArguments().getLast().toString(), "C4");
        Implication nesting = beliefSet.stream().map(this::matrix)
                .filter(f -> f instanceof Implication i && i.getFormulas().getSecond() instanceof Implication)
                .map(f -> (Implication) ((Implication) f).getFormulas().getSecond()).findFirst().orElseThrow();
        FolAtom composite = (FolAtom) nesting.getFormulas().getFirst();
        var parts = ((Conjunction) nesting.getFormulas().getSecond()).getFormulas();
        FolAtom base = (FolAtom) parts.getFirst();
        FolAtom qualifier = (FolAtom) parts.getLast();
        names.put(composite.getPredicate().getName(), "ctx4");
        names.put(base.getPredicate().getName(), "ctx2");
        names.put(qualifier.getPredicate().getName(), "ctx3");
        variables.put(base.getArguments().getLast().toString(), "C2");
        variables.put(qualifier.getArguments().getLast().toString(), "C3");
        FolFormula binding = beliefSet.stream().map(this::matrix)
                .filter(f -> f instanceof Implication i && i.getFormulas().getSecond() instanceof Conjunction)
                .findFirst().orElseThrow();
        for (var member : ((Conjunction) ((Implication) binding).getFormulas().getSecond()).getFormulas()) {
            FolAtom atom = (FolAtom) member;
            if (atom.getArguments().getFirst().equals(planet.getArguments().getLast()))
                names.put(atom.getPredicate().getName(), "ctx1");
        }
        Set<String> actual = beliefSet.stream().map(f -> canonical(f, names, variables)).collect(Collectors.toSet());
        JsonNode reference = new ObjectMapper(new YAMLFactory()).readTree(
                getClass().getResourceAsStream("/fol/pluto-dwarf-planet.yaml"));
        int checked = 0;
        for (JsonNode item : reference.get("data")) {
            // This extra assumption is present only in the illustrative FOL fixture,
            // not in test-pluto.aofn (before and after are not declared inverses there).
            if (item.get("label").asText().equals("Ctx1 and Ctx2 are disjoint"))
                continue;
            FolFormula expected = new TPTPParser().parseBeliefBase(
                    "fof(reference,axiom,(" + item.get("formula").asText() + ")).").iterator().next();
            assertTrue(actual.contains(canonical(expected, Map.of(), Map.of())), item.get("label").asText());
            checked++;
        }
        assertEquals(7, checked);
        // The added context binding and nesting formulae are guarded. OWL transitivity
        // itself is outside TGF and is preserved rather than silently weakened.
        var checker = new TriguardedFragmentChecker();
        beliefSet.stream().filter(f -> f.getPredicates().stream().anyMatch(p -> p.getName().equals("ctxBnd")))
                .forEach(f -> assertTrue(checker.isInFragment(f), f::toString));
    }

    @Test
    void handlesDeepNativeAspects() throws Exception {
        Rendered rendered = renderNative("/syntax/deeply-nested.aofn");
        FolBeliefSet beliefSet = rendered.renderer().getBeliefSet();
        long nestings = beliefSet.stream().map(this::matrix)
                .filter(f -> f instanceof Implication i && i.getFormulas().getSecond() instanceof Implication nesting
                        && nesting.getFormulas().getFirst() instanceof FolAtom atom
                        && atom.getPredicate().getName().startsWith("ctx_")).count();
        assertEquals(5, nestings);
        assertTrue(beliefSet.stream().allMatch(FolFormula::isClosed));
        assertEquals(beliefSet.size(), parseOutput(rendered.text(), beliefSet).size());
    }

    @Test
    void handlesMultipleAndNestedAnnotationsWithoutReusingStreams() throws Exception {
        OWLOntologyManager manager = OWLManager.createOWLOntologyManager();
        var df = manager.getOWLDataFactory();
        OWLOntology ontology = manager.createOntology();
        var a = df.getOWLClass(IRI.create("urn:test:A"));
        var b = df.getOWLClass(IRI.create("urn:test:B"));
        var c = df.getOWLClass(IRI.create("urn:test:C"));
        var aspect = df.getOWLAnnotationProperty(IRI.create("http://ontology.aspectowl.xyz#hasAspect"));
        manager.addAxiom(ontology, df.getOWLDeclarationAxiom(a));
        manager.addAxiom(ontology, df.getOWLDeclarationAxiom(b));
        manager.addAxiom(ontology, df.getOWLDeclarationAxiom(c));
        var first = df.getOWLAnnotation(aspect, df.getOWLLiteral("A"));
        var second = df.getOWLAnnotation(aspect, b.getIRI(), Set.of(df.getOWLAnnotation(aspect, c.getIRI())));
        OWLAxiom qualified = df.getOWLDisjointClassesAxiom(Set.of(a, b, c), Set.of(first, second));
        manager.addAxiom(ontology, qualified);
        manager.addAxiom(ontology, df.getOWLSubClassOfAxiom(a, b, Set.of(
                df.getOWLAnnotation(df.getRDFSLabel(), df.getOWLLiteral("unrelated annotation")))));
        StringWriter text = new StringWriter();
        var renderer = new AspectAnnotationOWL2TPTPObjectRenderer(ontology, text, Imports.EXCLUDED);
        renderer.setHumanReadableTptpNames(true);
        assertFalse(renderer.hasAspect(df.getOWLSubClassOfAxiom(a, b)));
        ontology.accept(renderer);
        assertTrue(renderer.getBeliefSet().stream().allMatch(FolFormula::isClosed));
        assertEquals(6, renderer.getBeliefSet().stream().map(this::matrix)
                .filter(f -> f instanceof Implication i && i.getFormulas().getSecond() instanceof Negation).count());
        assertDoesNotThrow(() -> parseOutput(text.toString(), renderer.getBeliefSet()));
        Set<String> firstRender = renderer.getBeliefSet().stream().map(Object::toString).collect(Collectors.toSet());
        ontology.accept(renderer);
        assertEquals(firstRender, renderer.getBeliefSet().stream().map(Object::toString).collect(Collectors.toSet()));
        List<FolFormula> queries = qualified.accept(renderer).toList();
        assertEquals(6, queries.size());
        assertTrue(queries.stream().allMatch(FolFormula::isClosed));
        assertTrue(queries.stream().allMatch(f -> f.getPredicates().stream().anyMatch(p -> p.getName().equals("ctxBnd"))));
        FolFormula freshExpressionQuery = df.getOWLSubClassOfAxiom(a, df.getOWLObjectComplementOf(b))
                .accept(renderer).findFirst().orElseThrow();
        assertTrue(freshExpressionQuery.isClosed());
        assertTrue(freshExpressionQuery.getPredicates().stream().allMatch(p -> p.getArity() == 2));
        assertTrue(renderer.getBeliefSet().stream().flatMap(f -> f.getPredicates().stream())
                .allMatch(p -> p.getName().equals("ctxT") || p.getName().equals("ctxBnd") || p.getArity() == 2));
    }

    @Test
    void importsRetainTheirOwnNativeAspectsAndWrapperCanRenderThem() throws Exception {
        OWLOntologyManager manager = OWLManager.createOWLOntologyManager();
        var df = manager.getOWLDataFactory();
        OWLOntology root = manager.createOntology(IRI.create("urn:test:root"));
        OWLOntology imported = manager.createOntology(IRI.create("urn:test:imported"));
        manager.applyChange(new AddImport(root, df.getOWLImportsDeclaration(IRI.create("urn:test:imported"))));
        OWLAxiom axiom = df.getOWLClassAssertionAxiom(df.getOWLClass(IRI.create("urn:test:A")),
                df.getOWLNamedIndividual(IRI.create("urn:test:i")));
        manager.addAxiom(root, axiom);
        manager.addAxiom(imported, axiom);
        OWLAspectManager aspects = new OWLAspectManager();
        for (OWLOntology source : List.of(root, imported)) {
            var aspect = aspects.getAspect(df.getOWLClass(IRI.create(source == root ? "urn:test:Local" : "urn:test:Imported")), Set.of());
            aspects.addAspect(source, aspects.getAspectAssertionAxiom(source, new AspectOWLJoinPointAxiomPointcut(axiom), aspect));
        }
        var renderer = new ConcreteAspectOWL2TPTPObjectRenderer(aspects, root, new StringWriter(), Imports.INCLUDED);
        renderer.setHumanReadableTptpNames(true);
        root.accept(renderer);
        assertEquals(2, renderer.getBeliefSet().stream().map(this::matrix)
                .filter(f -> f instanceof Implication i && i.getFormulas().getSecond() instanceof FolAtom atom
                        && atom.getPredicate().getName().equals("nc_A")).count());
        assertTrue(renderer.getBeliefSet().getMinimalSignature().containsPredicate("nc_Imported"));
        assertTrue(renderer.getBeliefSet().getMinimalSignature().containsPredicate("nc_Local"));
        StringWriter output = new StringWriter();
        new OWL2TPTPRenderer(aspects, Imports.INCLUDED).render(root, output);
        assertTrue(output.toString().contains("ctxBnd"));
        var excluded = new ConcreteAspectOWL2TPTPObjectRenderer(aspects, root, new StringWriter(), Imports.EXCLUDED);
        excluded.setHumanReadableTptpNames(true);
        root.accept(excluded);
        assertFalse(excluded.getBeliefSet().getMinimalSignature().containsPredicate("nc_Imported"));
    }

    private FolBeliefSet parseOutput(String text, FolBeliefSet beliefSet) throws Exception {
        TPTPParser parser = new TPTPParser();
        // Tweety 1.30 does not trim whitespace before matching the formula role.
        parser.setFormulaRoles(".*");
        // Constants occurring only in equality are missed by signature inference.
        parser.getSignature().addSignature(beliefSet.getMinimalSignature());
        return parser.parseBeliefBase(text);
    }

    private FolAtom scopedAssertion(FolBeliefSet beliefSet, String predicate) {
        return beliefSet.stream().map(this::matrix).filter(f -> f instanceof Implication)
                .map(f -> ((Implication) f).getFormulas().getSecond()).filter(f -> f instanceof FolAtom)
                .map(f -> (FolAtom) f).filter(a -> a.getPredicate().getName().equals(predicate)).findFirst().orElseThrow();
    }

    private FolFormula matrix(FolFormula formula) {
        while (formula instanceof ForallQuantifiedFormula)
            formula = (FolFormula) formula.getFormula();
        return formula;
    }

    private String canonical(FolFormula formula, Map<String, String> names, Map<String, String> variables) {
        if (formula instanceof FolAtom atom) {
            List<String> terms = atom.getArguments().stream().map(t -> t instanceof Variable
                    ? variables.getOrDefault(t.toString(), t.toString()) : names.getOrDefault(t.toString(), t.toString())).toList();
            // Argument order of the invented guard has no significance across renderers.
            if (atom.getPredicate().getName().equals("ctxBnd"))
                terms = terms.stream().sorted().toList();
            return names.getOrDefault(atom.getPredicate().getName(), atom.getPredicate().getName()) + terms;
        }
        if (formula instanceof ForallQuantifiedFormula || formula instanceof ExistsQuantifiedFormula) {
            boolean universal = formula instanceof ForallQuantifiedFormula;
            Set<String> bound = new TreeSet<>();
            do {
                formula.getQuantifierVariables().forEach(v -> bound.add(variables.getOrDefault(v.toString(), v.toString())));
                formula = (FolFormula) formula.getFormula();
            } while (universal ? formula instanceof ForallQuantifiedFormula : formula instanceof ExistsQuantifiedFormula);
            return (universal ? "forall" : "exists") + bound + canonical(formula, names, variables);
        }
        if (formula instanceof Negation negation)
            return "not(" + canonical(negation.getFormula(), names, variables) + ")";
        if (formula instanceof AssociativeFolFormula associative)
            return formula.getClass().getSimpleName() + associative.getFormulas().stream()
                    .map(f -> canonical((FolFormula) f, names, variables)).sorted().toList();
        if (formula instanceof Implication implication)
            return "implies(" + canonical((FolFormula) implication.getFormulas().getFirst(), names, variables) + ","
                    + canonical((FolFormula) implication.getFormulas().getSecond(), names, variables) + ")";
        if (formula instanceof Equivalence equivalence)
            return "iff(" + canonical((FolFormula) equivalence.getFormulas().getFirst(), names, variables) + ","
                    + canonical((FolFormula) equivalence.getFormulas().getSecond(), names, variables) + ")";
        return formula.toString();
    }
}
