/**
 * 
 */
package xyz.aspectowl.owlapi.model;

import java.util.Set;

import org.semanticweb.owlapi.model.*;

/**
 * An OWL aspect (actually the advice which will potentially be applied to its pointcut).
 * @author ralph
 *
 */
public interface OWLAspect extends OWLClassExpression, HasAnnotations {

	public Set<OWLObjectProperty> getAccessibilityRelations();
	
	public OWLAspect getAspectWithoutAnnotations();
	
	/**
	 * Aspects can have aspects as well (see
	 * <a href="http://www-formal.stanford.edu/jmc/context3/node3.html">
	 * McCarthy's "Notes on Formalizing Context", section 3,
	 * "Entering and Leaving Contexts".
	 * </a>
	 * @return The nested aspects of this aspect.
	 */
	public Set<OWLAspect> getAspects();

    /**
     *
     * @return This OWLAspect as an OWLClassExpression.
     */
	public OWLClassExpression asClassExpression();
}
