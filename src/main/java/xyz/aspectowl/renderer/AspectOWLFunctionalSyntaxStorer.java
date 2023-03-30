package xyz.aspectowl.renderer;

import xyz.aspectowl.owlapi.model.OWLAspectManager;
import xyz.aspectowl.parser.AspectOWLFunctionalSyntaxDocumentFormat;
import org.semanticweb.owlapi.model.OWLDocumentFormat;
import org.semanticweb.owlapi.model.OWLOntology;
import org.semanticweb.owlapi.model.OWLOntologyStorageException;
import org.semanticweb.owlapi.util.AbstractOWLStorer;

import javax.annotation.Nonnull;
import java.io.IOException;
import java.io.Writer;

public class AspectOWLFunctionalSyntaxStorer extends AbstractAspectOWLStorer {

    private static final long serialVersionUID = -865604218123629582L;

    public AspectOWLFunctionalSyntaxStorer(OWLAspectManager am) {
        super(am);
    }

    @Override
    protected void storeOntology(@Nonnull OWLOntology ontology, @Nonnull Writer writer, @Nonnull OWLDocumentFormat format) throws OWLOntologyStorageException {
        try {
            ontology.accept(new AspectOWLFunctionalSyntaxObjectRenderer(ontology, writer, getAspectManager()));
            writer.flush();
        } catch (IOException e) {
            throw new OWLOntologyStorageException(e);
        }
    }

    @Override
    public boolean canStoreOntology(@Nonnull OWLDocumentFormat ontologyFormat) {
        return ontologyFormat instanceof AspectOWLFunctionalSyntaxDocumentFormat;
    }
}
