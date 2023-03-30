package xyz.aspectowl.rdf.renderer;

import org.semanticweb.owlapi.model.OWLDocumentFormat;
import org.semanticweb.owlapi.model.OWLOntology;
import org.semanticweb.owlapi.model.OWLOntologyStorageException;
import xyz.aspectowl.owlapi.model.OWLAspectManager;
import xyz.aspectowl.rdf.AspectOWLRDFDocumentFormat;
import xyz.aspectowl.renderer.AbstractAspectOWLStorer;

import javax.annotation.Nonnull;
import java.io.Writer;

/**
 * @author ralph
 */
public class AspectOWLRDFStorer extends AbstractAspectOWLStorer {
    protected AspectOWLRDFStorer(OWLAspectManager aspectManager) {
        super(aspectManager);
    }

    @Override
    protected void storeOntology(@Nonnull OWLOntology ontology, @Nonnull Writer writer, @Nonnull OWLDocumentFormat format) throws OWLOntologyStorageException {

    }

    @Override
    public boolean canStoreOntology(@Nonnull OWLDocumentFormat ontologyFormat) {
        return ontologyFormat instanceof AspectOWLRDFDocumentFormat;
    }
}
