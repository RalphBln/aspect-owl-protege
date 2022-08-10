package xyz.aspectowl.owlapi.model;

import org.semanticweb.owlapi.model.OWLClassExpression;

public interface OWLAnonymousAspect extends OWLAspect, OWLClassExpression {
    public OWLClassExpression getClassExpression();
}
