package xyz.aspectowl.owlapi.model.impl;

import xyz.aspectowl.owlapi.model.visitor.AspectOWLVisitorMap;
import xyz.aspectowl.owlapi.model.visitor.MissingAspectVisitorMappingException;
import xyz.aspectowl.owlapi.protege.AspectOWLEditorKitHook;
import org.semanticweb.owlapi.model.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import uk.ac.manchester.cs.owl.owlapi.OWLLogicalAxiomImplWithEntityAndAnonCaching;
import xyz.aspectowl.owlapi.model.*;

import javax.annotation.Nonnull;
import java.util.Objects;
import java.util.Set;

public class OWLAspectAssertionAxiomImpl extends OWLLogicalAxiomImplWithEntityAndAnonCaching implements OWLAspectAssertionAxiom {

    private static final Logger log = LoggerFactory.getLogger(OWLAspectAssertionAxiomImpl.class);

    private OWLOntology ontology;
    private AspectOWLPointcut pointcut;
    private OWLAspect aspect;

    public OWLAspectAssertionAxiomImpl(@Nonnull OWLOntology ontology, @Nonnull AspectOWLPointcut pointcut, @Nonnull OWLAspect aspect) {
        super(aspect.getAnnotations());
        this.ontology = ontology;
        this.pointcut = pointcut;
        this.aspect = aspect;
    }

    @Override
    public OWLAspect getAspect() {
        return aspect;
    }

    @Override
    public AspectOWLPointcut getPointcut() {
        return pointcut;
    }

    @Override
    public void accept(@Nonnull OWLAxiomVisitor visitor) {
        if (visitor instanceof OWLAspectAxiomVisitor) {
            ((OWLAspectAxiomVisitor)visitor).visit(this);
        } else {
            AspectOWLVisitorMap.getAspectAxiomVisitor(visitor).ifPresentOrElse(v -> v.visit(this), () -> log.warn("No aspect visitor defined for visitor {}", visitor));
        }
    }

    @Nonnull
    @Override
    public <O> O accept(@Nonnull OWLAxiomVisitorEx<O> visitor) {
        if (visitor instanceof OWLAspectAxiomVisitorEx) {
            return ((OWLAspectAxiomVisitorEx<O>)visitor).visit(this);
        }
        return (O) AspectOWLVisitorMap.getAspectAxiomVisitor(visitor).map(v -> v.visit(this)).orElseThrow(() -> new MissingAspectVisitorMappingException(this, visitor));

    }

    @Nonnull
    @Override
    public OWLAxiom getAxiomWithoutAnnotations() {
        OWLAspect aspectWithoutAnnotations = aspect.getAspectWithoutAnnotations();
        return new OWLAspectAssertionAxiomImpl(ontology, pointcut, aspectWithoutAnnotations);
    }

    @Nonnull
    @Override
    public OWLAxiom getAnnotatedAxiom(@Nonnull Set<OWLAnnotation> annotations) {
        return new OWLAspectAssertionAxiomImpl(ontology, pointcut, aspect);
    }

    @Nonnull
    @Override
    public AxiomType<?> getAxiomType() {
        return AspectOWLEditorKitHook.OWL_ASPECT_ASSERTION_AXIOM_TYPE;
    }

    @Override
    protected int compareObjectOfSameType(@Nonnull OWLObject object) {
        OWLAspectAssertionAxiomImpl otherAx = (OWLAspectAssertionAxiomImpl) object;
        int diff = getPointcut().compareTo(otherAx.getPointcut());
        if (diff != 0) {
            return diff;
        } else {
            return getAspect().compareTo(otherAx.getAspect());
        }
    }

    @Override
    public void accept(@Nonnull OWLObjectVisitor visitor) {
        if (visitor instanceof OWLAspectAxiomVisitor) {
            ((OWLAspectAxiomVisitor) visitor).visit(this);
        } else {
            AspectOWLVisitorMap.getAspectAxiomVisitor(visitor).get().visit(this);
        }
    }

    @Nonnull
    @Override
    public <O> O accept(@Nonnull OWLObjectVisitorEx<O> visitor) {
        if (visitor instanceof OWLAspectAxiomVisitorEx) {
            return ((OWLAspectAxiomVisitorEx<O>) visitor).visit(this);
        } else {
            return ((OWLAspectAxiomVisitorEx<O>)AspectOWLVisitorMap.getAspectAxiomVisitor(visitor).get()).visit(this);
        }
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof OWLAspectAssertionAxiomImpl)) return false;
        if (!super.equals(o)) return false;
        OWLAspectAssertionAxiomImpl that = (OWLAspectAssertionAxiomImpl) o;
        return Objects.equals(ontology, that.ontology) &&
                Objects.equals(pointcut, that.pointcut) &&
                Objects.equals(aspect, that.aspect);
    }

    @Override
    public int hashCode() {
        return Objects.hash(super.hashCode(), ontology, pointcut, aspect);
    }

    @Override
    public String toString() {
        return String.format("Aspect(%s %s)", pointcut, aspect);
    }
}
