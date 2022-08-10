package xyz.aspectowl.owlapi.model;

import org.semanticweb.owlapi.model.OWLOntology;
import org.semanticweb.owlapi.model.OWLOntologyChange;

import javax.annotation.Nonnull;

public abstract class AspectOWLPointcutChange extends OWLOntologyChange {

    private AspectOWLPointcut pointcut;

    /**
     * @param ont the ontology to which the change is to be applied
     */
    public AspectOWLPointcutChange(@Nonnull OWLOntology ont, @Nonnull AspectOWLPointcut pointcut) {
        super(ont);
        this.pointcut = pointcut;
    }

    public AspectOWLPointcut getPointcut() {
        return pointcut;
    }


}
