package de.fuberlin.csw.aspectowl.owlapi.model.visitor;

import de.fuberlin.csw.aspectowl.owlapi.model.OWLAspectAxiomVisitor;
import de.fuberlin.csw.aspectowl.owlapi.model.OWLAspectAxiomVisitorEx;
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

    public static Optional<OWLAspectAxiomVisitor> getAspectAxiomVisitor(OWLAxiomVisitor orig) {
        return provider.getAspectAxiomVisitor(orig);
    }

    public static Optional<OWLAspectAxiomVisitorEx> getAspectAxiomVisitor(OWLAxiomVisitorEx orig) {
        return provider.getAspectAxiomVisitor(orig);
    }

}
