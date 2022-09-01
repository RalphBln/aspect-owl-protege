package xyz.aspectowl.owlapi.model;

import org.semanticweb.owlapi.model.OWLAnnotationSubject;

/**
 * Encapsulates OWL entities that may act as an aspect joint point (IRIs and anonymous individuals).
 */
public class OWLJoinPoint {

    // Subject can be either an IRI or an anonymous individual. Their most specific common interface is OWLAnnotationSubject.
    // This is syntactically correct, and we make use of this fact.
    // Whether it makes sense semantically is arguable, what with annotations having no (OWL) semantics to begin with.
    // Anyhow, it is very convenient from a programmer's perspective, so I shamelessly do it like that aaaand period.
    private OWLAnnotationSubject subject;

    public OWLJoinPoint(OWLAnnotationSubject subject) {
        this.subject = subject;
    }

    public OWLAnnotationSubject getSubject() {
        return subject;
    }
}
