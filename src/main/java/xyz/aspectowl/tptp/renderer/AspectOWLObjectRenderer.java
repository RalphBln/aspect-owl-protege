package xyz.aspectowl.tptp.renderer;

import org.semanticweb.owlapi.model.OWLAxiom;
import org.semanticweb.owlapi.model.OWLObjectVisitorEx;

/** Visitor extension for applying an axiom's aspect contexts to its FOL translation. */
public interface AspectOWLObjectRenderer<T> extends OWLObjectVisitorEx<T> {
    default boolean hasAspect(OWLAxiom axiom) {
        return false;
    }

    /** Qualifies the translated axiom parts with the contexts attached to the axiom. */
    T handleAspects(OWLAxiom axiom, T nonAspectFormulae);
}
