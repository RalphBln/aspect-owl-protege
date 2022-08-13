package xyz.aspectowl.owlapi.model.visitor;

import xyz.aspectowl.owlapi.model.AspectOWLAxiomVisitor;
import xyz.aspectowl.owlapi.model.AspectOWLAxiomVisitorEx;
import org.semanticweb.owlapi.model.OWLAxiomVisitor;
import org.semanticweb.owlapi.model.OWLAxiomVisitorEx;

import java.util.Optional;

/**
 * @author ralph
 */
public class AspectOWLVisitorMap {

    private static AspectOWLVisitorProvider provider;

    public static void setProvider(AspectOWLVisitorProvider provider) {
        AspectOWLVisitorMap.provider = provider;
    }

    public static Optional<AspectOWLAxiomVisitor> getAspectAxiomVisitor(OWLAxiomVisitor orig) {
        return provider.getAspectAxiomVisitor(orig);
    }

    public static Optional<AspectOWLAxiomVisitorEx> getAspectAxiomVisitor(OWLAxiomVisitorEx orig) {
        return provider.getAspectAxiomVisitor(orig);
    }

}
