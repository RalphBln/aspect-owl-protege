package xyz.aspectowl.reasoner;

import org.semanticweb.owlapi.model.OWLAxiom;
import org.semanticweb.owlapi.model.OWLOntology;
import org.semanticweb.owlapi.model.parameters.Imports;
import org.tweetyproject.logics.fol.syntax.FolFormula;
import xyz.aspectowl.owlapi.model.AspectOWLAxiomVisitorEx;
import xyz.aspectowl.owlapi.model.OWLAspectAssertionAxiom;
import xyz.aspectowl.owlapi.model.OWLAspectManager;
import xyz.aspectowl.tptp.renderer.AspectOWL2TPTPObjectRenderer;

import java.io.Writer;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.stream.Stream;

/** Renders native AspectOWL assertions, including aspects on aspect assertions. */
public class ConcreteAspectOWL2TPTPObjectRenderer extends AspectOWL2TPTPObjectRenderer
        implements AspectOWLAxiomVisitorEx<Stream<FolFormula>> {

    private final OWLAspectManager aspectManager;

    public ConcreteAspectOWL2TPTPObjectRenderer(OWLAspectManager aspectManager, OWLOntology ontology,
                                               Writer writer, Imports includeImportsClosure) {
        super(ontology, writer, includeImportsClosure);
        this.aspectManager = aspectManager;
    }

    @Override
    protected List<AspectContext> getAspectContexts(OWLAxiom axiom) {
        return getAspectContexts(axiom, new HashSet<>());
    }

    private List<AspectContext> getAspectContexts(OWLAxiom axiom, Set<OWLAxiom> path) {
        if (!path.add(axiom))
            throw new IllegalArgumentException("Cyclic aspect nesting at " + axiom);
        try {
            OWLOntology source = currentOntology != null ? currentOntology : onto.orElseThrow();
            return aspectManager.getAspectAssertionAxioms(source, axiom).stream()
                    .map(assertion -> new AspectContext(assertion.getAspect().asClassExpression(),
                            getAspectContexts(assertion, path))).toList();
        } finally {
            path.remove(axiom);
        }
    }

    @Override
    public Stream<FolFormula> visit(OWLAspectAssertionAxiom axiom) {
        // Rendered together with its join point, never as an independent OWL axiom.
        return Stream.empty();
    }
}
