package xyz.aspectowl.reasoner;

import org.semanticweb.owlapi.reasoner.OWLReasoner;
import xyz.aspectowl.owlapi.model.OWLAspect;

/**
 * {@link org.semanticweb.owlapi.reasoner.OWLReasoner}s implementing this interface must obey to the
 * @author ralph
 */
public interface AspectOWLReasoner extends OWLReasoner {

    /**
     * Sets the aspect under which all entailments are supposed to be made.
     * @param aspect
     */
    public void setAspect(OWLAspect aspect);

    public OWLAspect getAspect();
}
