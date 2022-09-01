package xyz.aspectowl.owlapi.visitor;

import org.semanticweb.owlapi.model.OWLAxiomVisitorEx;
import org.semanticweb.owlapi.model.OWLClassExpression;
import org.semanticweb.owlapi.model.OWLClassExpressionVisitor;
import org.semanticweb.owlapi.util.OWLClassExpressionCollector;
import xyz.aspectowl.owlapi.model.*;

import java.util.HashSet;
import java.util.Set;

/**
 * @author ralph
 */
public class AspectOWLClassExpressionCollector extends OWLClassExpressionCollector implements AspectOWLAxiomVisitorEx<Set<OWLClassExpression>> {

    // in case we get a subclass instance
    private OWLClassExpressionCollector orig;

    public AspectOWLClassExpressionCollector(OWLClassExpressionCollector orig) {
        this.orig = orig;
    }


    @Override
    public Set<OWLClassExpression> visit(OWLAspectAssertionAxiom axiom) {

        var result = new HashSet<OWLClassExpression>();

        result.addAll(axiom.getAspect().asClassExpression().accept(orig));

        // Only axiom pointcuts can contain class expressions, entity pointcuts cannot.
        AspectOWLPointcut pointcut = axiom.getPointcut();
        if (pointcut instanceof AspectOWLAxiomPointcut)
            ((AspectOWLAxiomPointcut)pointcut).getAssertedAxiomsInPointcut().forEach(pointcutAxiom -> result.addAll(pointcutAxiom.accept(orig)));

        return result;
    }
}
