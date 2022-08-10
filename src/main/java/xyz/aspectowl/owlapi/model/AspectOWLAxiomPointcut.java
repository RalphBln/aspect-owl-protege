package xyz.aspectowl.owlapi.model;

import org.semanticweb.owlapi.model.OWLAxiom;

import java.util.Set;

public abstract class AspectOWLAxiomPointcut implements AspectOWLPointcut<OWLAxiom> {

    /**
     * Returns a set containing the axioms that are asserted to belong to this pointcut.
     * @return
     */
    public abstract Set<OWLAxiom> getAssertedAxiomsInPointcut();

    /**
     * Returns a set containing the axioms that are inferred to belong to this pointcut.
     * @return
     */
    public abstract Set<OWLAxiom> getInferredAxiomsInPointcut();

    @Override
    public int compareTo(AspectOWLPointcut o) {
        // TODO
        return 0;
    }
}
