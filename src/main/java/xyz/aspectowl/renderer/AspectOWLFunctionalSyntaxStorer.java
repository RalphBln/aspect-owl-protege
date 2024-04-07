package xyz.aspectowl.renderer;

import org.semanticweb.owlapi.io.OWLOntologyDocumentTarget;
import org.semanticweb.owlapi.model.IRI;
import org.semanticweb.owlapi.util.AbstractOWLStorer;
import xyz.aspectowl.owlapi.model.OWLAspectManager;
import xyz.aspectowl.parser.AspectOWLFunctionalSyntaxDocumentFormat;
import org.semanticweb.owlapi.model.OWLDocumentFormat;
import org.semanticweb.owlapi.model.OWLOntology;
import org.semanticweb.owlapi.model.OWLOntologyStorageException;
import xyz.aspectowl.rdf.AspectOWLTrigDocumentFormat;

import javax.annotation.Nonnull;
import java.io.IOException;
import java.io.Writer;

public class AspectOWLFunctionalSyntaxStorer extends AbstractOWLStorer implements AspectOWLStorer {

    private static final long serialVersionUID = -865604218123629582L;
    
    private OWLAspectManager aspectManager;

    public AspectOWLFunctionalSyntaxStorer(OWLAspectManager aspectManager) {
        this.aspectManager = aspectManager;
    }

    @Override
    protected void storeOntology(@Nonnull OWLOntology ontology, @Nonnull Writer writer, @Nonnull OWLDocumentFormat format) throws OWLOntologyStorageException {
        try {
            ontology.accept(new AspectOWLFunctionalSyntaxObjectRenderer(ontology, writer, aspectManager));
            writer.flush();
        } catch (IOException e) {
            throw new OWLOntologyStorageException(e);
        }
    }
    
    @Override
    public OWLAspectManager getAspectManager() {
        return aspectManager;
    }
    
    @Override
    public boolean canStoreOntology(@Nonnull OWLDocumentFormat ontologyFormat) {
        return ontologyFormat instanceof AspectOWLFunctionalSyntaxDocumentFormat;
    }
}
