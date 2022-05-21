package de.fuberlin.csw.aspectowl.owlapi.model;

import org.semanticweb.owlapi.model.OWLAnnotation;
import org.semanticweb.owlapi.model.OWLAxiom;
import org.semanticweb.owlapi.util.CollectionFactory;

import java.util.Objects;
import java.util.Set;

public class OWLJoinPointAxiomPointcut extends OWLAxiomPointcut {

    private OWLAxiom joinPointAxiom;

    public OWLJoinPointAxiomPointcut(OWLAxiom joinPointAxiom) {
        this.joinPointAxiom = joinPointAxiom.getAxiomWithoutAnnotations();
    }

    @Override
    public Set<OWLAxiom> getAssertedAxiomsInPointcut() {
        return CollectionFactory.createSet(joinPointAxiom);
    }

    @Override
    public Set<OWLAxiom> getInferredAxiomsInPointcut() {
        // TODO
        return null;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof OWLJoinPointAxiomPointcut)) return false;
        OWLJoinPointAxiomPointcut that = (OWLJoinPointAxiomPointcut) o;
        return Objects.equals(joinPointAxiom, that.joinPointAxiom.getAxiomWithoutAnnotations());
    }

    @Override
    public int hashCode() {
        return Objects.hash(joinPointAxiom);
    }

    @Override
    public String toString() {
        return joinPointAxiom.toString();
    }
}
