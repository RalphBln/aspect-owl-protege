/**
 * 
 */
package de.fuberlin.csw.aspectowl.inference;

import java.io.File;
import java.util.Set;

import org.protege.editor.owl.model.OWLWorkspace;
import org.semanticweb.owlapi.apibinding.OWLManager;
import org.semanticweb.owlapi.model.IRI;
import org.semanticweb.owlapi.model.OWLAxiom;
import org.semanticweb.owlapi.model.OWLOntology;
import org.semanticweb.owlapi.model.OWLOntologyCreationException;
import org.semanticweb.owlapi.model.OWLOntologyManager;
import org.semanticweb.owlapi.model.OWLOntologyStorageException;
import org.semanticweb.owlapi.util.SimpleIRIMapper;

import com.hp.hpl.jena.ontology.OntModel;
import com.hp.hpl.jena.ontology.OntModelSpec;
import com.hp.hpl.jena.query.Query;
import com.hp.hpl.jena.query.QueryExecution;
import com.hp.hpl.jena.query.QueryExecutionFactory;
import com.hp.hpl.jena.query.QueryFactory;
import com.hp.hpl.jena.rdf.model.Model;
import com.hp.hpl.jena.rdf.model.ModelFactory;

import de.fuberlin.csw.aspectowl.util.AspectOWLUtils;

/**
 * This class handles the graph-based selection of parts of an ontology and
 * annotation of selected axioms with aspects. 
 * 
 * @author ralph
 */
public class AspectSparqlQueryExecutor {

	private OWLWorkspace workspace;
	
	/**
	 * 
	 */
	public AspectSparqlQueryExecutor(OWLWorkspace workspace) {
		this.workspace = workspace;
	}

	/**
	 * Annotates the resulting axioms of a SPARQL query with an aspect.
	 * @param query The SPARQL query the resulting OWL axioms of which should be annotated with an aspect. 
	 * @param aspectProperty The aspect annotation property that should be used.
	 * @param aspectObject The entity that should be the object of the aspect annotations.
	 * @throws OWLOntologyCreationException 
	 * @throws OWLOntologyStorageException 
	 */
	public OWLOntology getOntologyModule(String queryString) throws OWLOntologyStorageException, OWLOntologyCreationException {
		OntModel jenaModel = AspectOWLUtils.owlOntologyToJenaModel(workspace.getOWLModelManager().getActiveOntology(), true);
		
		Query query = QueryFactory.create(queryString) ;
		QueryExecution qexec = QueryExecutionFactory.create(query, jenaModel) ;
		
		Model result = ModelFactory.createOntologyModel( OntModelSpec.OWL_MEM );
		result = qexec.execConstruct(result);

		OWLOntology module = AspectOWLUtils.jenaModelToOWLOntology(result);
		return module;
	}
	
	
	/**
	 * @param args
	 */
	public static void main(String[] args) {
		String QUERY = "PREFIX rdf: <http://www.w3.org/1999/02/22-rdf-syntax-ns#>\n" + 
				"PREFIX owl: <http://www.w3.org/2002/07/owl#>\n" + 
				"PREFIX xsd: <http://www.w3.org/2001/XMLSchema#>\n" + 
				"PREFIX rdfs: <http://www.w3.org/2000/01/rdf-schema#>\n" + 
				"PREFIX aspect: <http://www.corporate-semantic-web.de/ontologies/aspect_owl#>\n" + 
				"\n" + 
				"CONSTRUCT {?s ?p ?o}\n" + 
				"WHERE {\n" + 
				"	?s ?p ?o .\n" + 
				"	?o rdfs:subClassOf+ aspect:Aspect .\n" + 
				"}";

		System.out.println("\n\n" + QUERY + "\n\n");
		
		IRI exampleOntoIRI = IRI.create("http://csw.inf.fu-berlin.de/aood/example");
		
		IRI aspectOntoIRI = IRI.create("http://www.corporate-semantic-web.de/ontologies/aspect_owl");

		OWLOntologyManager om = OWLManager.createOWLOntologyManager();

		om.addIRIMapper(new SimpleIRIMapper(exampleOntoIRI, IRI.create(new File("/Users/ralph/Documents/Arbeit/Ontologien/AspectOWL/AspectsExample.owl"))));
		om.addIRIMapper(new SimpleIRIMapper(aspectOntoIRI, IRI.create(new File("/Users/ralph/Documents/Arbeit/Ontologien/AspectOWL/aspectOWL.owl"))));
		om.addIRIMapper(new SimpleIRIMapper(IRI.create("http://www.w3.org/2006/time"), IRI.create(new File("/Users/ralph/Documents/Arbeit/Ontologien/University/Ralph/time.owl"))));
		
		om.addIRIMapper(new SimpleIRIMapper(IRI.create("http://csw.inf.fu-berlin.de/ontologies/aood/test_inverse"), IRI.create(new File("/Users/ralph/Documents/Arbeit/Ontologien/AspectOWL/testInverse.owl"))));

		
		try {
			OWLOntology onto = om.loadOntology(exampleOntoIRI);
			
			System.out.println("orig: " + onto.getAxiomCount());
			
			OntModel jenaModel = AspectOWLUtils.owlOntologyToJenaModel(onto, true);
			
			Query query = QueryFactory.create(QUERY) ;
			QueryExecution qexec = QueryExecutionFactory.create(query, jenaModel) ;
			
			Model result = ModelFactory.createOntologyModel( OntModelSpec.OWL_MEM );
			result = qexec.execConstruct(result);

			OWLOntology module = AspectOWLUtils.jenaModelToOWLOntology(result);
			
			System.out.println("\nmodule: " + module.getAxiomCount() + "\n");
			
			Set<OWLAxiom> axioms = module.getAxioms();
			for (OWLAxiom axiom : axioms) {
				if (onto.containsAxiomIgnoreAnnotations(axiom, true)) {
					System.out.println("YES :) -> " + axiom);					
				} else {
					System.out.println("NO :( -> " + axiom);					
				}
			}
			
		} catch (Exception e) {
			e.printStackTrace();
		}

	}

}
