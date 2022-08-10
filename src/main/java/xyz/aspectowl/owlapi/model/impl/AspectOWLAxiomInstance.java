package xyz.aspectowl.owlapi.model.impl;

import xyz.aspectowl.owlapi.model.AspectContainer;
import xyz.aspectowl.owlapi.model.OWLAspect;
import xyz.aspectowl.owlapi.model.OWLAspectManager;
import org.semanticweb.owlapi.model.OWLAxiom;
import org.semanticweb.owlapi.model.OWLOntology;

import java.util.Set;

import static com.google.common.base.MoreObjects.toStringHelper;
import static com.google.common.base.Preconditions.checkNotNull;

public class AspectOWLAxiomInstance implements AspectContainer {
    private final OWLAxiom ax;
    private final OWLOntology ont;
    private final OWLAspectManager am;


    public AspectOWLAxiomInstance(OWLAxiom ax, OWLOntology ont, OWLAspectManager am) {
        this.ax = checkNotNull(ax);
        this.ont = ont;
        this.am = am;
    }

    public OWLAxiom getAxiom() {
        return ax;
    }


    public OWLOntology getOntology() {
        return ont;
    }

    @Override
    public Set<OWLAspect> getAspects() {
        return am.getAssertedAspects(ont, ax);
    }


    public boolean isAsserted() {
        return !isInferred();
    }

    public boolean isInferred() {
        return ont == null;
    }


    @Override
    public String toString() {
        return toStringHelper("AspectOWLAxiomInstance")
                .addValue(ax)
                .addValue(ont.getOntologyID())
                .toString();
    }

}
