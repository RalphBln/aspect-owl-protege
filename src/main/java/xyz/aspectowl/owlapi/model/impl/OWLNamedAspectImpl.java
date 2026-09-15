package xyz.aspectowl.owlapi.model.impl;

import com.google.common.collect.Sets;
import xyz.aspectowl.owlapi.model.OWLAspect;
import xyz.aspectowl.owlapi.model.OWLNamedAspect;
import org.semanticweb.owlapi.model.*;
import uk.ac.manchester.cs.owl.owlapi.OWLClassImpl;

import javax.annotation.Nonnull;
import java.util.Collections;
import java.util.HashSet;
import java.util.Set;

public class OWLNamedAspectImpl extends OWLClassImpl implements OWLNamedAspect {

    private HashSet<OWLAnnotation> annotations =  new HashSet<>();
    
    /**
     * @param iri class iri
     * @param annotations
     */
    public OWLNamedAspectImpl(@Nonnull IRI iri, Set<OWLAnnotation> annotations) {
        super(iri);
    }

    @Override
    public Set<OWLObjectProperty> getAccessibilityRelations() {
        return getObjectPropertiesInSignature();
    }

    @Override
    public OWLAspect getAspectWithoutAnnotations() {
        return new OWLNamedAspectImpl(getIRI(), Collections.EMPTY_SET);
    }

    @Nonnull
    @Override
    public Set<OWLAnnotation> getAnnotations() {
        return annotations;
    }
    
    @Override
    public OWLClassExpression asClassExpression() {
        return (OWLClassExpression)this;
    }
}
