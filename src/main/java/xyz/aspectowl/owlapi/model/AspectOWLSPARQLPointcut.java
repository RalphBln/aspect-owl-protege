package xyz.aspectowl.owlapi.model;

import org.semanticweb.owlapi.model.OWLAxiom;

import java.util.Objects;
import java.util.Set;

public class AspectOWLSPARQLPointcut extends AspectOWLAxiomPointcut {

    private String query;

    public AspectOWLSPARQLPointcut(String query) {
        this.query = query;
    }

    public String getQuery() {
        return query;
    }

    @Override
    public Set<OWLAxiom> getAssertedAxiomsInPointcut() {
        //TODO code already implemented in xyz.aspectowl.inference.AspectOWLSparqlQueryExecutor. Just call this code.
        return null;
    }

    @Override
    public Set<OWLAxiom> getInferredAxiomsInPointcut() {
        //TODO
        return null;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof AspectOWLSPARQLPointcut)) return false;
        AspectOWLSPARQLPointcut that = (AspectOWLSPARQLPointcut) o;
        return Objects.equals(query, that.query);
    }

    @Override
    public int hashCode() {
        return 13 * super.hashCode() + Objects.hash(query);
    }
}
