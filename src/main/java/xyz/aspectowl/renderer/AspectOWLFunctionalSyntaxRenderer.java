package xyz.aspectowl.renderer;

import xyz.aspectowl.owlapi.model.OWLAspectManager;
import org.semanticweb.owlapi.io.AbstractOWLRenderer;
import org.semanticweb.owlapi.io.OWLRendererException;
import org.semanticweb.owlapi.io.OWLRendererIOException;
import org.semanticweb.owlapi.model.OWLOntology;

import javax.annotation.Nonnull;
import java.io.IOException;
import java.io.Writer;

public class AspectOWLFunctionalSyntaxRenderer extends AbstractOWLRenderer {

    private OWLAspectManager am;

    public AspectOWLFunctionalSyntaxRenderer(OWLAspectManager am) {
        this.am = am;
    }

    @Override
    public void render(@Nonnull OWLOntology ontology, @Nonnull Writer writer) throws OWLRendererException {
        try {
            AspectOWLFunctionalSyntaxObjectRenderer ren = new AspectOWLFunctionalSyntaxObjectRenderer(ontology, writer, am);
            ontology.accept(ren);
            writer.flush();
        } catch (IOException e) {
            throw new OWLRendererIOException(e);
        }

    }
}
