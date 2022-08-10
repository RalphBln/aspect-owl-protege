package xyz.aspectowl.owlapi.model;

import org.semanticweb.owlapi.model.OWLOntologyChangeVisitor;

import javax.annotation.Nonnull;

public interface AspectOWLOntologyChangeVisitor extends OWLOntologyChangeVisitor {
    /**
     * visit AddAPointcut type
     *
     * @param change
     *        change to visit
     */
    void visit(@Nonnull AddPointcut change);

    /**
     * visit RemoveAxiom type
     *
     * @param change
     *        change to visit
     */
    void visit(@Nonnull RemovePointcut change);

}
