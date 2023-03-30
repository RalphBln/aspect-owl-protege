package xyz.aspectowl.renderer;

import org.semanticweb.owlapi.model.OWLDocumentFormatFactory;
import org.semanticweb.owlapi.util.OWLStorerFactoryImpl;
import xyz.aspectowl.owlapi.model.OWLAspectManager;

import javax.annotation.Nonnull;

/**
 * @author ralph
 */
public abstract class AspectOWLStorerFactoryImpl extends OWLStorerFactoryImpl {

    private OWLAspectManager aspectManager;

    protected AspectOWLStorerFactoryImpl(@Nonnull OWLDocumentFormatFactory format, @Nonnull OWLAspectManager aspectManager) {
        super(format);
        this.aspectManager = aspectManager;
    }

    protected OWLAspectManager getAspectManager() {
        return aspectManager;
    }
}
