package xyz.aspectowl.renderer;

import org.semanticweb.owlapi.util.AbstractOWLStorer;
import xyz.aspectowl.owlapi.model.OWLAspectManager;

/**
 * @author ralph
 */
public abstract class AbstractAspectOWLStorer extends AbstractOWLStorer {

    private OWLAspectManager aspectManager;

    protected AbstractAspectOWLStorer(OWLAspectManager aspectManager) {
        this.aspectManager = aspectManager;
    }

    protected OWLAspectManager getAspectManager() {
        return aspectManager;
    }

}
