package xyz.aspectowl.owlapi.model.impl;

import com.carrotsearch.hppcrt.maps.ObjectObjectHashMap;
import xyz.aspectowl.owlapi.model.OWLAspectAssertionAxiom;
import org.semanticweb.owlapi.model.*;

import javax.annotation.Nonnull;
import java.io.Serializable;
import java.util.*;

/**
 * @author ralph
 */
public class EntityToAspectAssertionAxiomMapping {

    // we use ObjectObjectHashMaps because the OWLAPI does so as well (probably for performance reasons)
    @Nonnull protected transient ObjectObjectHashMap<OWLEntity, Collection<OWLAspectAssertionAxiom>> entityReferences = new ObjectObjectHashMap<>();

    @Nonnull
    private final ReferencedAspectAssertionsAxiomsCollector refAxiomsCollector = new ReferencedAspectAssertionsAxiomsCollector();

    @Nonnull
    public Optional<? extends Iterable<OWLAspectAssertionAxiom>> referencingAxioms(@Nonnull OWLEntity owlEntity) {
        return owlEntity.accept(refAxiomsCollector);
    }

    public void addReferencingAxiom(@Nonnull OWLEntity entity, OWLAspectAssertionAxiom aspectAssertionAxiom) {
        getValues(entity).ifPresentOrElse(axioms -> axioms.add(aspectAssertionAxiom), () -> entityReferences.put(entity, new LinkedList<>(){{add(aspectAssertionAxiom);}}));
    }

    private Optional<Collection<OWLAspectAssertionAxiom>> getValues(OWLEntity entity) {
        return Optional.ofNullable(entityReferences.get(entity));
    }


    private class ReferencedAspectAssertionsAxiomsCollector
            implements OWLEntityVisitorEx<Optional<? extends Iterable<OWLAspectAssertionAxiom>>>, Serializable {

        @Override
        public Optional<? extends Iterable<OWLAspectAssertionAxiom>> visit(OWLClass cls) {
            return getValues(cls);
        }

        @Override
        public Optional<? extends Iterable<OWLAspectAssertionAxiom>> visit(OWLObjectProperty property) {
            return getValues(property);
        }

        @Override
        public Optional<? extends Iterable<OWLAspectAssertionAxiom>> visit(OWLDataProperty property) {
            return getValues(property);
        }

        @Override
        public Optional<? extends Iterable<OWLAspectAssertionAxiom>> visit(OWLNamedIndividual individual) {
            return getValues(individual);
        }

        @Override
        public Optional<? extends Iterable<OWLAspectAssertionAxiom>> visit(OWLDatatype datatype) {
            return getValues(datatype);
        }

        @Override
        public Optional<? extends Iterable<OWLAspectAssertionAxiom>> visit(OWLAnnotationProperty property) {
            return getValues(property);
        }
    }
}
