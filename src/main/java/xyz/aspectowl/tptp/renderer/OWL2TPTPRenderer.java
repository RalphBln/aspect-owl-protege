package xyz.aspectowl.tptp.renderer;

import org.semanticweb.owlapi.io.AbstractOWLRenderer;
import org.semanticweb.owlapi.io.OWLRendererException;
import org.semanticweb.owlapi.model.OWLOntology;
import org.semanticweb.owlapi.model.OWLRuntimeException;
import xyz.aspectowl.owlapi.model.OWLAspectManager;
import xyz.aspectowl.reasoner.ConcreteAspectOWL2TPTPObjectRenderer;
import org.semanticweb.owlapi.model.parameters.Imports;

import java.io.IOException;
import java.io.Writer;

/**
 * @author ralph
 */
public class OWL2TPTPRenderer extends AbstractOWLRenderer {

    private final Imports includeImportsClosure;
    private final OWLAspectManager aspectManager;

    public OWL2TPTPRenderer(Imports includeImportsClosure) {
        this(null, includeImportsClosure);
    }

    /** Supplies the manager that owns native AspectOWL assertions and their nesting. */
    public OWL2TPTPRenderer(OWLAspectManager aspectManager, Imports includeImportsClosure) {
        this.aspectManager = aspectManager;
        this.includeImportsClosure = includeImportsClosure;
    }

    @Override
    public void render(OWLOntology ontology, Writer writer) throws OWLRendererException {
        try {
            OWL2TPTPObjectRenderer ren = aspectManager == null
                    ? new AspectAnnotationOWL2TPTPObjectRenderer(ontology, writer, includeImportsClosure)
                    : new ConcreteAspectOWL2TPTPObjectRenderer(aspectManager, ontology, writer, includeImportsClosure);
            ontology.accept(ren);
            writer.flush();
        } catch (OWLRuntimeException | IOException e) {
            throw new OWLRendererException(e);
        }
    }
}
