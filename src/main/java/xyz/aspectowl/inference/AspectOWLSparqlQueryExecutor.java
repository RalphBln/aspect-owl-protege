/*******************************************************************************
 * Copyright (c) 2015 Freie Universitaet Berlin, Department of
 * Computer Science. All rights reserved.
 *
 * This file is part of the Corporate Smart Content Project.
 *
 * This work has been partially supported by the InnoProfile-Transfer
 * Corporate Smart Content project funded by the German Federal Ministry
 * of Education and Research (BMBF) and the BMBF Innovation Initiative
 * for the New German Laender - Entrepreneurial Regions.
 *
 * <http://sce.corporate-smart-content.de/>
 *
 * Copyright (c) 2013-2016,
 *
 * Freie Universitaet Berlin
 * Institut für Informatik
 * Corporate Semantic Web group
 * Koenigin-Luise-Strasse 24-26
 * 14195 Berlin
 * <http://www.mi.fu-berlin.de/en/inf/groups/ag-csw/>
 *
 * This library is free software; you can redistribute it and/or modify
 * it under the terms of the GNU Lesser General Public License as
 * published by the Free Software Foundation; either version 3 of the
 * License, or (at your option) any later version. This library is
 * distributed in the hope that it will be useful, but WITHOUT ANY
 * WARRANTY; without even the implied warranty of MERCHANTABILITY or
 * FITNESS FOR A PARTICULAR PURPOSE. See the GNU Lesser General Public
 * License for more details.
 *
 * You should have received a copy of the GNU Lesser General Public
 * License along with this library; if not, write to the Free Software
 * Foundation, Inc., 59 Temple Place, Suite 330, Boston, MA 02111-1307
 * USA or see <http://www.gnu.org/licenses/>.
 *******************************************************************************/
package xyz.aspectowl.inference;

import org.openrdf.query.GraphQuery;
import org.openrdf.query.IncompatibleOperationException;
import org.openrdf.query.MalformedQueryException;
import org.openrdf.query.Query;
import org.openrdf.query.QueryEvaluationException;
import org.openrdf.query.QueryLanguage;
import org.openrdf.repository.RepositoryConnection;
import org.openrdf.repository.RepositoryException;
import org.openrdf.rio.RDFHandlerException;
import org.protege.editor.owl.model.OWLWorkspace;
import org.protege.editor.owl.rdf.SparqlResultSet;
import org.protege.editor.owl.rdf.repository.GraphQueryHandler;
import org.protege.owl.rdf.Utilities;
import org.protege.owl.rdf.api.OwlTripleStore;
import org.semanticweb.owlapi.apibinding.OWLManager;
import org.semanticweb.owlapi.model.OWLOntology;
import org.semanticweb.owlapi.model.OWLOntologyCreationException;
import org.semanticweb.owlapi.model.OWLOntologyManager;
import org.semanticweb.owlapi.model.OWLOntologyStorageException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * This class handles the graph-based selection of parts of an ontology and
 * annotation of selected axioms with aspects. 
 * 
 * @author ralph
 */
public class AspectOWLSparqlQueryExecutor {

	private static final Logger log = LoggerFactory.getLogger(AspectOWLSparqlQueryExecutor.class);

	
	private OWLWorkspace workspace;
	OwlTripleStore tripleStore;
//	private SparqlReasoner sparqlReasoner;
	
	/**
	 * @throws RepositoryException 
	 * 
	 */
	public AspectOWLSparqlQueryExecutor(OWLWorkspace workspace) throws RepositoryException {
		this.workspace = workspace;
//		sparqlReasoner = new BasicSparqlReasonerFactory().createReasoner(workspace.getOWLModelManager().getOWLOntologyManager());
		tripleStore = Utilities.getOwlTripleStore(workspace.getOWLModelManager().getOWLOntologyManager(), true);
	}

	/**
	 * Annotates the resulting axioms of a SPARQL query with an aspect.
	 * @param query The SPARQL query the resulting OWL axioms of which should be annotated with an aspect. 
	 * @param aspectProperty The aspect annotation property that should be used.
	 * @param aspectObject The entity that should be the object of the aspect annotations.
	 * @throws RepositoryException 
	 * @throws MalformedQueryException 
	 * @throws RDFHandlerException 
	 * @throws QueryEvaluationException 
	 * @throws OWLOntologyCreationException 
	 * @throws OWLOntologyStorageException 
	 */
	public OWLOntology getOntologyModule(String queryString) throws RepositoryException, MalformedQueryException, QueryEvaluationException, RDFHandlerException, OWLOntologyCreationException {
		
//		OntModel jenaModel = AspectOWLUtils.owlOntologyToJenaModel(workspace.getOWLModelManager().getActiveOntology(), true);
//		
		RepositoryConnection conn = tripleStore.getRepository().getConnection();
		Query query = conn.prepareQuery(QueryLanguage.SPARQL, queryString);
		
		if (!(query instanceof GraphQuery)) {
			throw new IncompatibleOperationException("Only CONSTRUCT queries are allowed for the definition of pointcuts.");
		}
		
		GraphQueryHandler handler = new GraphQueryHandler(tripleStore);
		((GraphQuery)query).evaluate(handler);
		SparqlResultSet resultSet = handler.getQueryResult();

		int tripleCount = resultSet.getRowCount();
		log.info("*** Query result ***");
		for (int i = 0; i < tripleCount; i++) {
			Object subject = resultSet.getResult(i, 0);
			Object predicate = resultSet.getResult(i, 1);
			Object object = resultSet.getResult(i, 2);
			log.info(subject + " (CLASS:" + subject.getClass().getName() +  ")\t" + predicate + " (CLASS:" + predicate.getClass().getName()+ ")\t" + object + " (CLASS:" + object.getClass().getName() + ")");
		}
		
		OWLOntologyManager om = OWLManager.createOWLOntologyManager();
		OWLOntology module = om.createOntology();
		return module;
		
//		RDFGraph moduleGraph = new RDFGraph();
//		
//
//		
//		
//		
//		SailRepository moduleRep = null; // new SailRepository();
//		
//		
//		RDFTranslator translator = new RDFTranslator(om, module, true);
//		
//		translator.
//		
//		
//		resultSet.
		
//		QueryExecution qexec = QueryExecutionFactory.create(query, jenaModel) ;
//		
//		Model result = ModelFactory.createOntologyModel( OntModelSpec.OWL_MEM );
//		result = qexec.execConstruct(result);
//
//		OWLOntology module = AspectOWLUtils.jenaModelToOWLOntology(result);
//		return module;
	}
	
	
//	/**
//	 * @param args
//	 */
//	public static void main(String[] args) {
//		String QUERY = sparqlReasoner.getSampleQuery();
//		
//		
//		String QUERY = "PREFIX rdf: <http://www.w3.org/1999/02/22-rdf-syntax-ns#>\n" + 
//				"PREFIX owl: <http://www.w3.org/2002/07/owl#>\n" + 
//				"PREFIX xsd: <http://www.w3.org/2001/XMLSchema#>\n" + 
//				"PREFIX rdfs: <http://www.w3.org/2000/01/rdf-schema#>\n" + 
//				"PREFIX aspect: <https://ontology.aspectowl.xyz/aspect_owl#>\n" + 
//				"\n" + 
//				"CONSTRUCT {?s ?p ?o}\n" + 
//				"WHERE {\n" + 
//				"	?s ?p ?o .\n" + 
//				"	?o rdfs:subClassOf+ aspect:Aspect .\n" + 
//				"}";
//
//		System.out.println("\n\n" + QUERY + "\n\n");
//		
//		IRI exampleOntoIRI = IRI.create("http://csw.inf.fu-berlin.de/aood/example");
//		
//		IRI aspectOntoIRI = IRI.create("https://ontology.aspectowl.xyz/aspect_owl");
//
//		OWLOntologyManager om = OWLManager.createOWLOntologyManager();
//
//		om.getIRIMappers().add(new SimpleIRIMapper(exampleOntoIRI, IRI.create(new File("/Users/ralph/Documents/Arbeit/Ontologien/AspectOWL/AspectsExample.owl"))));
//		om.getIRIMappers().add(new SimpleIRIMapper(aspectOntoIRI, IRI.create(new File("/Users/ralph/Documents/Arbeit/Ontologien/AspectOWL/aspectOWL.owl"))));
//		om.getIRIMappers().add(new SimpleIRIMapper(IRI.create("http://www.w3.org/2006/time"), IRI.create(new File("/Users/ralph/Documents/Arbeit/Ontologien/University/Ralph/time.owl"))));
//		
//		//om.getIRIMappers().add(new SimpleIRIMapper(IRI.create("http://csw.inf.fu-berlin.de/ontologies/aood/test_inverse"), IRI.create(new File("/Users/ralph/Documents/Arbeit/Ontologien/AspectOWL/testInverse.owl"))));
//
//		
//		try {
//			OWLOntology onto = om.loadOntology(exampleOntoIRI);
//			
//			System.out.println("orig: " + onto.getAxiomCount());
//			
//			OntModel jenaModel = AspectOWLUtils.owlOntologyToJenaModel(onto, true);
//			
//			Query query = QueryFactory.create(QUERY) ;
//			QueryExecution qexec = QueryExecutionFactory.create(query, jenaModel) ;
//			
//			Model result = ModelFactory.createOntologyModel( OntModelSpec.OWL_MEM );
//			result = qexec.execConstruct(result);
//
//			OWLOntology module = AspectOWLUtils.jenaModelToOWLOntology(result);
//			
//			System.out.println("\nmodule: " + module.getAxiomCount() + "\n");
//			
//			Set<OWLAxiom> axioms = module.getAxioms();
//			for (OWLAxiom axiom : axioms) {
//				if (EntitySearcher.containsAxiomIgnoreAnnotations(axiom, onto, true)) {
//					System.out.println("YES :) -> " + axiom);					
//				} else {
//					System.out.println("NO :( -> " + axiom);					
//				}
//			}
//			
//		} catch (Exception e) {
//			e.printStackTrace();
//		}
//
//	}

}
