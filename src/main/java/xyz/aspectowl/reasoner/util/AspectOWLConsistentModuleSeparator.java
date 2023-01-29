package xyz.aspectowl.reasoner.util;

import org.jgrapht.alg.ConnectivityInspector;
import org.jgrapht.graph.DefaultEdge;
import org.jgrapht.graph.SimpleGraph;
import org.paukov.combinatorics3.Generator;
import org.semanticweb.owlapi.apibinding.OWLManager;
import org.semanticweb.owlapi.model.*;
import org.semanticweb.owlapi.model.parameters.Imports;
import xyz.aspectowl.Preferences;

import java.util.HashMap;
import java.util.Set;
import java.util.UUID;

public class AspectOWLConsistentModuleSeparator {

    public enum Directedness {Directed, Undirected}


    private void reset() {

    }

    public Set<OWLOntology> getMinimalSetOfConsistentModules(OWLOntology onto) {
        return null;
    }

    public Set<OWLOntology> getDisconnectedModules(OWLOntology onto) throws OWLOntologyCreationException {

        HashMap<OWLEntity, OWLAxiom> axiomsByEntities = new HashMap<>();

        var axioms = onto.getLogicalAxioms(Imports.INCLUDED);
        OWLOntologyManager man = OWLManager.createOWLOntologyManager();

        String ontoIRI = onto.getOntologyID().getDefaultDocumentIRI().
                or(IRI.create(
                        Preferences.ASPECTOWL_NAMESPACE.toString().concat("/").concat(UUID.randomUUID().toString())
                )).toString();

        HashMap<OWLEntity, OWLAxiom> axiomsByEntity = new HashMap<>();

        // Transform ontology to undirected graph
        SimpleGraph<OWLEntity, DefaultEdge> ontologyGraph = new SimpleGraph<>(DefaultEdge.class);
        axioms.forEach(axiom -> {
            axiomsByEntity.put(axiom.getSignature().stream().findAny().get(), axiom); // map one arbitrary entity to this axiom so that we can find the axioms for a module later
            Generator.combination(axiom.getSignature()).simple(2).forEach(owlEntityPair -> {
                OWLEntity entity1 = owlEntityPair.get(0);
                OWLEntity entity2 = owlEntityPair.get(1);
                ontologyGraph.addVertex(entity1);
                ontologyGraph.addVertex(entity2);
                ontologyGraph.addEdge(entity1, entity2);
            });
        });

        // If graph is disconnected, get all maximally connected components
        ConnectivityInspector<OWLEntity, DefaultEdge> ci = new ConnectivityInspector(ontologyGraph);
        int count = 1;
        for(Set<OWLEntity> owlEntities : ci.connectedSets()) {
            OWLOntology module = man.createOntology(IRI.create(ontoIRI + "/" + count++));
            owlEntities.forEach(owlEntity -> System.out.printf("%s ", owlEntity.getIRI().getShortForm()));
        }

        return null;
    }
}
