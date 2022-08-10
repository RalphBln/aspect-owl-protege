package xyz.aspectowl.owlapi.model;

import org.semanticweb.owlapi.model.OWLAxiom;
import org.semanticweb.owlapi.model.OWLClassExpression;

import java.util.Objects;
import java.util.Set;

public class DLQueryPointcutAspect extends AspectOWLAxiomPointcut {

    private OWLClassExpression dlQuery;

    public DLQueryPointcutAspect(OWLClassExpression dlQuery) {
        this.dlQuery = dlQuery;
    }

    public OWLClassExpression getDlQuery() {
        return dlQuery;
    }

    @Override
    public Set<OWLAxiom> getAssertedAxiomsInPointcut() {
        //TODO implement DL Query
        return null;
    }

    @Override
    public Set<OWLAxiom> getInferredAxiomsInPointcut() {
        // TODO
        return null;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof DLQueryPointcutAspect)) return false;
        DLQueryPointcutAspect that = (DLQueryPointcutAspect) o;
        return Objects.equals(dlQuery, that.dlQuery);
    }

    @Override
    public int hashCode() {
        return 23 * super.hashCode() + Objects.hash(dlQuery);
    }
}
