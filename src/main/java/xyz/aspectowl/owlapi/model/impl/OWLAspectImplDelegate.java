/**
 * 
 */
package xyz.aspectowl.owlapi.model.impl;

import java.util.*;

import com.google.common.collect.Sets;
import org.semanticweb.owlapi.model.*;

import xyz.aspectowl.owlapi.model.OWLAspect;
import org.semanticweb.owlapi.util.CollectionFactory;

import javax.annotation.Nonnull;

/**
 *
 * @author ralph
 */
public class OWLAspectImplDelegate {

	private OWLAspect aspect;

	private HashSet<OWLObjectProperty> accessibilityRelations = new HashSet<>();
	
	@Nonnull
	private final List<OWLAnnotation> annotations;

	@Nonnull
	private final List<OWLAspect> aspects;


	/**
	 * @param aspect
	 * @param annotations
	 * @param aspects Nested aspects
	 */
	public OWLAspectImplDelegate(OWLAspect aspect, Set<OWLAnnotation> annotations, Set<OWLAspect> aspects) {
		this.aspect = aspect;
		this.annotations = CollectionFactory.sortOptionally((Set<OWLAnnotation>) annotations);
		this.aspects = CollectionFactory.sortOptionally((Set<OWLAspect>) aspects);
	}

	private static final long serialVersionUID = 4829969668138075822L;

	public Set<OWLObjectProperty> getAccessibilityRelations() {
		return Sets.union(accessibilityRelations, aspect.getObjectPropertiesInSignature());
	}

	@Nonnull
	public Set<OWLAnnotation> getAnnotations() {
		return CollectionFactory
				.getCopyOnRequestSetFromImmutableCollection(annotations);
	}
	
	/**
	 * Returns the nested aspects of this aspect.
	 * @return the nested aspects of this aspect
	 */
	@Nonnull
	public Set<OWLAspect> getAspects() {
		return CollectionFactory
		.getCopyOnRequestSetFromImmutableCollection(aspects);
	}

    /**
     *
     * @return This aspect's pure OWLClassExpression
     */
	public OWLClassExpression asClassExpression() {
	    return aspect.asClassExpression();
    }

}
