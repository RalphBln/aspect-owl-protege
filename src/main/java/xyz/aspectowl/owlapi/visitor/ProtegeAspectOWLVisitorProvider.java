package xyz.aspectowl.owlapi.visitor;

import org.semanticweb.owlapi.util.OWLClassExpressionCollector;
import xyz.aspectowl.owlapi.model.AspectOWLAxiomVisitor;
import xyz.aspectowl.owlapi.model.AspectOWLAxiomVisitorEx;
import xyz.aspectowl.owlapi.model.visitor.AspectOWLVisitorProvider;
import org.semanticweb.owlapi.model.OWLAxiomVisitor;
import org.semanticweb.owlapi.model.OWLAxiomVisitorEx;

import java.util.Optional;

/**
 * @author ralph
 */
public class ProtegeAspectOWLVisitorProvider implements AspectOWLVisitorProvider {

    @Override
    public Optional<AspectOWLAxiomVisitor> getAspectAxiomVisitor(OWLAxiomVisitor orig) {
        return Optional.of(new AspectOWLAxiomVisitorAdapter());
    }

    @Override
    public Optional<AspectOWLAxiomVisitorEx> getAspectAxiomVisitor(OWLAxiomVisitorEx orig) {
        if (orig instanceof OWLClassExpressionCollector)
            return Optional.of(new AspectOWLClassExpressionCollector((OWLClassExpressionCollector) orig));
        return Optional.of(new AspectOWLAxiomVisitorAdapterEx());
    }

}
