package xyz.aspectowl.owlapi.model;

import org.semanticweb.owlapi.model.OWLAxiom;
import org.semanticweb.owlapi.util.CollectionFactory;

import java.util.Objects;
import java.util.Set;

public class AspectOWLJoinPointAxiomPointcut extends AspectOWLAxiomPointcut {

    private OWLAxiom joinPointAxiom;

    public AspectOWLJoinPointAxiomPointcut(OWLAxiom joinPointAxiom) {
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
        if (!(o instanceof AspectOWLJoinPointAxiomPointcut)) return false;
        AspectOWLJoinPointAxiomPointcut that = (AspectOWLJoinPointAxiomPointcut) o;
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
