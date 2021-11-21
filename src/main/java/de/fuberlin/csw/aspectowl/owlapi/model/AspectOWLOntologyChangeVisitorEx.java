package de.fuberlin.csw.aspectowl.owlapi.model;

import javax.annotation.Nonnull;

import org.semanticweb.owlapi.model.OWLOntologyChangeVisitorEx;

public interface AspectOWLOntologyChangeVisitorEx<O> extends OWLOntologyChangeVisitorEx<O> {

    /**
     * visit AddAxiom type
     *
     * @param change
     *        change to visit
     * @return visitor value
     */
    @Nonnull
    O visit(@Nonnull AddPointcut change);

    /**
     * visit RemoveAxiom type
     *
     * @param change
     *        change to visit
     * @return visitor value
     */
    @Nonnull
    O visit(@Nonnull RemovePointcut change);

}
