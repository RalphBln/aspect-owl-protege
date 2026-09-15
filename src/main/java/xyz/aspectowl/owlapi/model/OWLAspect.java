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
   * @return This OWLAspect as an OWLClassExpression.
   */
  public OWLClassExpression asClassExpression();
}
