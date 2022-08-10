package xyz.aspectowl.owlapi.model;

import java.util.Set;

/**
 * An interface to an object that contains aspects.
 * @author ralph
 */
public interface HasAspects {
	
	public Set<OWLAspect> getAspects();
}
