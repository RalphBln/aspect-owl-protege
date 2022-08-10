package xyz.aspectowl.owlapi.model;

import org.semanticweb.owlapi.change.OWLOntologyChangeData;
import org.semanticweb.owlapi.model.*;

import javax.annotation.Nonnull;
import java.util.Objects;
import java.util.Set;

public class RemovePointcut extends AspectOWLPointcutChange {

    /**
     * @param ont the ontology to which the change is to be applied
     */
    public RemovePointcut(@Nonnull OWLOntology ont, AspectOWLPointcut pointcut) {
        super(ont, pointcut);
    }

    @Override
    public boolean isAxiomChange() {
        return false;
    }

    @Override
    public boolean isAddAxiom() {
        return false;
    }

    @Nonnull
    @Override
    public OWLAxiom getAxiom() {
        return null;
    }

    @Override
    public boolean isImportChange() {
        return false;
    }

    @Nonnull
    @Override
    public OWLOntologyChangeData getChangeData() {
        return null; //TODO
    }

    @Nonnull
    @Override
    public Set<OWLEntity> getSignature() {
        return null; //TODO
    }

    @Override
    public boolean equals(Object obj) {
        if (obj == this) {
            return true;
        }
        if (!(obj instanceof RemovePointcut)) {
            return false;
        }
        RemovePointcut other = (RemovePointcut) obj;
        return other.getOntology().equals(getOntology()) && other.getPointcut()
                .equals(getPointcut());
    }

    @Override
    public int hashCode() {
        return 37 + Objects.hash(super.hashCode(), getPointcut());
    }

    @Nonnull
    @Override
    public String toString() {
        return String.format("RemovePointcut(%s OntologyID(%s))", getPointcut(),
                getOntology().getOntologyID());

    }

    @Override
    public void accept(@Nonnull OWLOntologyChangeVisitor visitor) {
        if (visitor instanceof AspectOWLOntologyChangeVisitor) {
            ((AspectOWLOntologyChangeVisitor)visitor).visit(this);
        }
    }

    @Override
    public <O> O accept(@Nonnull OWLOntologyChangeVisitorEx<O> visitor) {
        if (visitor instanceof AspectOWLOntologyChangeVisitorEx) {
            return (O)((AspectOWLOntologyChangeVisitorEx) visitor).visit(this);
        }
        return null;
    }

    @Override
    public OWLOntologyChange reverseChange() {
        return new AddPointcut(getOntology(), getPointcut());
    }
}
