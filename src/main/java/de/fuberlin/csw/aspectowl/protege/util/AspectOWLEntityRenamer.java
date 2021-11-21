package de.fuberlin.csw.aspectowl.protege.util;

import org.semanticweb.owlapi.model.*;
import org.semanticweb.owlapi.util.OWLEntityRenamer;

import javax.annotation.Nonnull;
import java.util.List;
import java.util.Map;
import java.util.Set;

/**
 * @author ralph
 */
public class AspectOWLEntityRenamer extends OWLEntityRenamer {
    /**
     * @param owlOntologyManager the ontology manager to use
     * @param ontologies         the ontologies to use
     */
    public AspectOWLEntityRenamer(@Nonnull OWLOntologyManager owlOntologyManager, @Nonnull Set<OWLOntology> ontologies) {
        super(owlOntologyManager, ontologies);
    }

    @Nonnull
    @Override
    public List<OWLOntologyChange> changeIRI(@Nonnull IRI iri, @Nonnull IRI newIRI) {
        return super.changeIRI(iri, newIRI);
    }

    @Nonnull
    @Override
    public List<OWLOntologyChange> changeIRI(@Nonnull OWLEntity entity, @Nonnull IRI newIRI) {
        return super.changeIRI(entity, newIRI);
    }

    @Override
    public List<OWLOntologyChange> changeIRI(@Nonnull Map<OWLEntity, IRI> entity2IRIMap) {
        return super.changeIRI(entity2IRIMap);
    }
}
