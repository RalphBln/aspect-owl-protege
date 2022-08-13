package xyz.aspectowl.owlapi.model;

import org.semanticweb.owlapi.model.OWLAxiomVisitorEx;

public interface AspectOWLAxiomVisitorEx<O> extends OWLAxiomVisitorEx<O> {
    O visit(OWLAspectAssertionAxiom axiom);
}
