package xyz.aspectowl.owlapi.model;

import org.semanticweb.owlapi.model.OWLObjectVisitor;

import javax.annotation.Nonnull;

public interface OWLAspectVisitor extends OWLObjectVisitor {
    void visit(@Nonnull OWLAspect aspect);
}
