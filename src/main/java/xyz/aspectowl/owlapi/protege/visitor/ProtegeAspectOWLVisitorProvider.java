package xyz.aspectowl.owlapi.protege.visitor;

import xyz.aspectowl.owlapi.model.OWLAspectAxiomVisitor;
import xyz.aspectowl.owlapi.model.OWLAspectAxiomVisitorEx;
import xyz.aspectowl.owlapi.model.visitor.AspectOWLVisitorProvider;
import org.semanticweb.owlapi.model.OWLAxiomVisitor;
import org.semanticweb.owlapi.model.OWLAxiomVisitorEx;

import java.util.Optional;

/**
 * @author ralph
 */
public class ProtegeAspectOWLVisitorProvider implements AspectOWLVisitorProvider {

    @Override
    public Optional<OWLAspectAxiomVisitor> getAspectAxiomVisitor(OWLAxiomVisitor orig) {
        return Optional.of(new DummyVisitor());
    }

    @Override
    public Optional<OWLAspectAxiomVisitorEx> getAspectAxiomVisitor(OWLAxiomVisitorEx orig) {
        return Optional.of(new DummyVisitorEx());
    }

}
