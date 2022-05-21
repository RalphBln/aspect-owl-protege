package de.fuberlin.csw.aspectowl.owlapi.model.visitor;

import de.fuberlin.csw.aspectowl.owlapi.model.OWLAspectAxiomVisitor;
import de.fuberlin.csw.aspectowl.owlapi.model.OWLAspectAxiomVisitorEx;
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
