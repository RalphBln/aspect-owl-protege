package xyz.aspectowl.reasoner;

import org.semanticweb.owlapi.model.OWLOntology;
import org.semanticweb.owlapi.reasoner.*;

import javax.annotation.Nonnull;

/**
 * @author ralph
 */
public class AspectOWLFOLReasonerFactory implements OWLReasonerFactory {

    @Nonnull
    @Override
    public String getReasonerName() {
        return AspectOWLFOLReasoner.REASONER_NAME;
    }

    @Nonnull
    @Override
    public OWLReasoner createReasoner(@Nonnull OWLOntology ontology) {
        return new AspectOWLFOLReasoner(ontology, BufferingMode.BUFFERING);
    }

    @Nonnull
    @Override
    public OWLReasoner createReasoner(@Nonnull OWLOntology ontology, @Nonnull OWLReasonerConfiguration config) {
        return new AspectOWLFOLReasoner(ontology, config, BufferingMode.BUFFERING);
    }

    @Nonnull
    @Override
    public OWLReasoner createNonBufferingReasoner(@Nonnull OWLOntology ontology) {
        return new AspectOWLFOLReasoner(ontology, BufferingMode.NON_BUFFERING);
    }

    @Nonnull
    @Override
    public OWLReasoner createNonBufferingReasoner(@Nonnull OWLOntology ontology, @Nonnull OWLReasonerConfiguration config) {
        return new AspectOWLFOLReasoner(ontology, config, BufferingMode.NON_BUFFERING);
    }
}
