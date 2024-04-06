package xyz.aspectowl.rdf.renderer;

import org.semanticweb.owlapi.model.OWLDocumentFormat;
import org.semanticweb.owlapi.model.OWLOntology;
import org.semanticweb.owlapi.model.OWLOntologyStorageException;
import org.semanticweb.owlapi.rio.RioStorer;
import org.semanticweb.owlapi.rio.RioTrigStorerFactory;
import xyz.aspectowl.owlapi.model.OWLAspectManager;
import xyz.aspectowl.rdf.AspectOWLTrigDocumentFormat;
import xyz.aspectowl.renderer.AbstractAspectOWLStorer;

import javax.annotation.Nonnull;
import java.io.Writer;

/**
 * @author ralph
 */
public class AspectOWLTrigStorer extends AbstractAspectOWLStorer {
    protected AspectOWLTrigStorer(OWLAspectManager aspectManager) {
        super(aspectManager);
    }

    @Override
    protected void storeOntology(@Nonnull OWLOntology ontology, @Nonnull Writer writer, @Nonnull OWLDocumentFormat format) throws OWLOntologyStorageException {
        System.out.println("Storing AspectOWL -> Trig");
    }

    @Override
    public boolean canStoreOntology(@Nonnull OWLDocumentFormat ontologyFormat) {
        return ontologyFormat instanceof AspectOWLTrigDocumentFormat;
    }
}
