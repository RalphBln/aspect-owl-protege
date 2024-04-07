package xyz.aspectowl.renderer;

import org.semanticweb.owlapi.model.OWLStorer;
import org.semanticweb.owlapi.util.AbstractOWLStorer;
import xyz.aspectowl.owlapi.model.OWLAspectManager;

/**
 * @author ralph
 */
public abstract interface AspectOWLStorer extends OWLStorer {
    public OWLAspectManager getAspectManager();
}
