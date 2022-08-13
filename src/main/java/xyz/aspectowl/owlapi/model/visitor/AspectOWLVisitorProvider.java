package xyz.aspectowl.owlapi.model.visitor;

import xyz.aspectowl.owlapi.model.AspectOWLAxiomVisitor;
import xyz.aspectowl.owlapi.model.AspectOWLAxiomVisitorEx;
import org.semanticweb.owlapi.model.OWLAxiomVisitor;
import org.semanticweb.owlapi.model.OWLAxiomVisitorEx;

import java.util.Optional;

/**
 * @author ralph
 */
public interface AspectOWLVisitorProvider {
    public Optional<AspectOWLAxiomVisitor> getAspectAxiomVisitor(OWLAxiomVisitor orig);
    public Optional<AspectOWLAxiomVisitorEx> getAspectAxiomVisitor(OWLAxiomVisitorEx orig);
}
