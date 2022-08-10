package xyz.aspectowl.owlapi.model.visitor;

import xyz.aspectowl.owlapi.model.OWLAspectAxiomVisitor;
import xyz.aspectowl.owlapi.model.OWLAspectAxiomVisitorEx;
import org.semanticweb.owlapi.model.OWLAxiomVisitor;
import org.semanticweb.owlapi.model.OWLAxiomVisitorEx;

import java.util.Optional;

/**
 * @author ralph
 */
public interface AspectOWLVisitorProvider {
    public Optional<OWLAspectAxiomVisitor> getAspectAxiomVisitor(OWLAxiomVisitor orig);
    public Optional<OWLAspectAxiomVisitorEx> getAspectAxiomVisitor(OWLAxiomVisitorEx orig);
}
