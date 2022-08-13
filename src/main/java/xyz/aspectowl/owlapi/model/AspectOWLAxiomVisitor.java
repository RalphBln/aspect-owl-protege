package xyz.aspectowl.owlapi.model;

import org.semanticweb.owlapi.model.OWLAxiomVisitor;

public interface AspectOWLAxiomVisitor extends OWLAxiomVisitor {
    void visit(OWLAspectAssertionAxiom axiom);
}
