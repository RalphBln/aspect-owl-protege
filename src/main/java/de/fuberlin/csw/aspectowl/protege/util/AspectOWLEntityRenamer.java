package de.fuberlin.csw.aspectowl.protege.util;

import de.fuberlin.csw.aspectowl.owlapi.model.OWLOntologyAspectManager;
import de.fuberlin.csw.aspectowl.owlapi.protege.AspectOWLEditorKitHook;
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

    private final OWLOntologyManager owlOntologyManager;
    private final Set<OWLOntology> ontologies;
    private final OWLOntologyAspectManager aspectManager;

    /**
     * @param owlOntologyManager the ontology manager to use
     * @param ontologies         the ontologies to use
     */
    public AspectOWLEntityRenamer(@Nonnull OWLOntologyManager owlOntologyManager, @Nonnull Set<OWLOntology> ontologies) {
        super(owlOntologyManager, ontologies);
        this.owlOntologyManager = owlOntologyManager;
        this.ontologies = ontologies;
        this.aspectManager = AspectOWLEditorKitHook.getAspectManager(owlOntologyManager);
    }

    @Nonnull
    @Override
    public List<OWLOntologyChange> changeIRI(@Nonnull IRI iri, @Nonnull IRI newIRI) {
        var changes = super.changeIRI(iri, newIRI);
        return changes;
    }

    @Nonnull
    @Override
    public List<OWLOntologyChange> changeIRI(@Nonnull OWLEntity entity, @Nonnull IRI newIRI) {
        var changes = super.changeIRI(entity, newIRI);
        return changes;
    }

    @Override
    public List<OWLOntologyChange> changeIRI(@Nonnull Map<OWLEntity, IRI> entity2IRIMap) {
        var changes = super.changeIRI(entity2IRIMap);
        return changes;
    }



}
