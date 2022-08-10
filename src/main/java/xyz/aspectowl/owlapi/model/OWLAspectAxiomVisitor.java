package xyz.aspectowl.owlapi.model;

import org.semanticweb.owlapi.model.OWLAxiomVisitor;

public interface OWLAspectAxiomVisitor extends OWLAxiomVisitor {
    void visit(OWLAspectAssertionAxiom axiom);
}
