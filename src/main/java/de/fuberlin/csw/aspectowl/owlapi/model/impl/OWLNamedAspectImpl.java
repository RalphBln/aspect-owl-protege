package de.fuberlin.csw.aspectowl.owlapi.model.impl;

import com.google.common.collect.Sets;
import de.fuberlin.csw.aspectowl.owlapi.model.OWLAspect;
import de.fuberlin.csw.aspectowl.owlapi.model.OWLNamedAspect;
import org.semanticweb.owlapi.model.*;
import uk.ac.manchester.cs.owl.owlapi.OWLClassImpl;

import javax.annotation.Nonnull;
import java.util.Collections;
import java.util.Set;

public class OWLNamedAspectImpl extends OWLClassImpl implements OWLNamedAspect {

    private final OWLAspectImplDelegate delegate;

    /**
     * @param iri class iri
     * @param annotations
     * @param aspects
     */
    public OWLNamedAspectImpl(@Nonnull IRI iri, Set<OWLAnnotation> annotations, Set<OWLAspect> aspects) {
        super(iri);
        delegate = new OWLAspectImplDelegate(this, annotations, aspects);
    }

    @Override
    public Set<OWLObjectProperty> getAccessibilityRelations() {
        return Sets.union(delegate.getAccessibilityRelations(), getObjectPropertiesInSignature());
    }

    @Override
    public OWLAspect getAspectWithoutAnnotations() {
        return new OWLNamedAspectImpl(getIRI(), Collections.EMPTY_SET, delegate.getAspects());
    }

    @Nonnull
    @Override
    public Set<OWLAnnotation> getAnnotations() {
        return delegate.getAnnotations();
    }
    
    /**
     * @see de.fuberlin.csw.aspectowl.owlapi.model.OWLAspect#getAspects()
     */
    @Override
    @Nonnull
    public Set<OWLAspect> getAspects() {
    	return delegate.getAspects();
    }

    @Override
    public OWLClassExpression asClassExpression() {
        return (OWLClassExpression)this;
    }
}
