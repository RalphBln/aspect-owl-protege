package xyz.aspectowl.util.test;

public class AspectOWLUtilsTest {
	
//	/**
//	 * A global variable of the type hashmap that gives easy access 
//	 * to the important properties of the corresponding resource.
//	 * The key of the hashmap holds the name of the entire set.
//	 * The set itself contains the IRI and the path of the associated OWL file.
//	 */
//	private HashMap<String,HashMap<String,IRI>> setup = new HashMap<String,HashMap<String,IRI>>();
//
//	/**
//	 * The global variable setup is filled by the following function 
//	 * before the test class is constructed.
//	 * Starting with an array of triples (with a NAME for the set, the IRI 
//	 * and the particular RESOURCE path) the hashmap is filled consecutively.
//	 * With get(NAME) the corresponding set is loaded.
//	 * To get to the values from the set, you need to write 
//	 * get("ontology") for the IRI and get("document") for the RESOURCE.
//	 */
//	@Before
//	public void setUp() throws Exception
//	{
//		// should be of the form: NAME, IRI, FILE_RESOURCE
//		String[][] setupContent = {
//				{"example", "http://csw.inf.fu-berlin.de/aood/example", "/AspectsExample.owl"},
//				{"aspect",  "https://ontology.aspectowl.xyz/aspect_owl", "/aspectOWL.owl"},
//				{"time",    "http://www.w3.org/2006/time", "/time.owl"},
//				{"siblings","http://www.semanticweb.org/test-ontology", "/sibs-full.owl"}
//		};
//		
//		for (String[] setupElement : setupContent)
//		{
//			HashMap<String,IRI> structuredSetup = new HashMap<String,IRI>();
//			structuredSetup.put("ontology", IRI.create(setupElement[1]));
//			structuredSetup.put("document", IRI.create(new File(getClass().getResource(setupElement[2]).getPath())));
//			this.setup.put(setupElement[0], structuredSetup);
//		}
//	}
//
//	/**
//	 * AspectOrientedPreprocessingDocumentTarget function to verify if SPARQL queries are evaluated right.
//	 * The idea is to compare whether all expected axioms are part of the original ontology.
//	 */
//	@AspectOrientedPreprocessingDocumentTarget
//	public void sparqlQueryConstructTest()
//	{
//		OWLOntologyManager om = OWLManager.createOWLOntologyManager();
//		
//		HashMap<String,IRI> exampleIRIs = this.setup.get("example");
//		HashMap<String,IRI> aspectIRIs  = this.setup.get("aspect");
//		HashMap<String,IRI> timeIRIs    = this.setup.get("time");
//		
//		om.addIRIMapper(new SimpleIRIMapper(exampleIRIs.get("ontology"), exampleIRIs.get("document")));
//		om.addIRIMapper(new SimpleIRIMapper(aspectIRIs.get("ontology"),  aspectIRIs.get("document")));
//		om.addIRIMapper(new SimpleIRIMapper(timeIRIs.get("ontology"),    timeIRIs.get("document")));
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
//		Query query = QueryFactory.create(QUERY);
//		
//		try {
//			OWLOntology onto = om.loadOntology(exampleIRIs.get("ontology"));
//			OntModel jenaModel = AspectOWLUtils.owlOntologyToJenaModel(onto, true);
//			QueryExecution qexec = QueryExecutionFactory.create(query, jenaModel);
//			
//			Model result = ModelFactory.createOntologyModel( OntModelSpec.OWL_MEM );
//			result = qexec.execConstruct(result);
//			
//			OWLOntology module = AspectOWLUtils.jenaModelToOWLOntology(result);
//			
//			assertTrue(onto.getAxiomCount() >= module.getAxiomCount());
//			
//			Set<OWLAxiom> axioms = module.getAxioms();
//			for (OWLAxiom axiom : axioms)
//			{
//				/*
//				boolean containsAxiom = onto.containsAxiomIgnoreAnnotations(axiom, true);
//				System.out.println((containsAxiom ? "PASSED" : "FAILED") + " to find axiom " + axiom);
//				assert containsAxiom;
//				*/
//				assertTrue(onto.containsAxiomIgnoreAnnotations(axiom, true));
//			}
//		}
//		catch (Exception e) {
//			fail( e.toString() );
//		}
//		
//	}
//	
//	/**
//	 * AspectOrientedPreprocessingDocumentTarget suite for ASK-Queries.
//	 * Since the result of these kind of queries is TRUE or FALSE, 
//	 * we just compare the result with an expected boolean value.
//	 */
//	@AspectOrientedPreprocessingDocumentTarget
//	public void sparqlQueryAskTest()
//	{
//		OWLOntologyManager om = OWLManager.createOWLOntologyManager();
//		
//		HashMap<String,IRI> timeIRIs = this.setup.get("time");
//		om.addIRIMapper(new SimpleIRIMapper(timeIRIs.get("ontology"), timeIRIs.get("document")));
//		
//		OWLOntology ontology = null;
//		OntModel jenaModel = null;
//		try {
//			ontology  = om.loadOntology(timeIRIs.get("ontology"));
//			jenaModel = AspectOWLUtils.owlOntologyToJenaModel(ontology, true);
//		}
//		catch (Exception e) {
//			fail( e.toString() );
//		}
//		
//		String prefixes = "PREFIX rdfs: <http://www.w3.org/2000/01/rdf-schema#>"
//						+ "PREFIX time: <http://www.w3.org/2006/time#>";
//		
//		// every line represents a query of the form: QUERY_STRING, EXPECTATION
//		String[][] queries = {
//			{ "ASK {}", "true" },
//			{ prefixes + "ASK { time:TemporalEntity rdfs:subClassOf time:Interval }", "false" },
//			{ prefixes + "ASK { time:Interval rdfs:subClassOf time:TemporalEntity }", "true" }
//		};
//		
//		for (String[] queryLine : queries)
//		{
//			String  queryString = queryLine[0];
//			boolean expectation = Boolean.parseBoolean(queryLine[1]);
//			
//			Query query = QueryFactory.create(queryString);
//			QueryExecution qexec = QueryExecutionFactory.create(query, jenaModel);
//			assertEquals( qexec.execAsk(), expectation );
//			qexec.close();
//		}
//		
//	}
//	
//	/**
//	 * Results of SELECT queries are tested by this function.
//	 * Depending on the test mode, the results are evaluated differently:
//	 * For the mode 'amount' the cardinality of the result list is compared to an expected number.
//	 * The resulting RDF nodes are compared by their expected name in the mode 'contains'.
//	 */
//	@AspectOrientedPreprocessingDocumentTarget
//	public void sparqlQuerySelectTest()
//	{
//		OWLOntologyManager om = OWLManager.createOWLOntologyManager();
//		
//		HashMap<String,IRI> aspectIRIs = this.setup.get("aspect");
//		om.addIRIMapper(new SimpleIRIMapper(aspectIRIs.get("ontology"), aspectIRIs.get("document")));
//		
//		OWLOntology ontology = null;
//		OntModel jenaModel = null;
//		try {
//			ontology  = om.loadOntology(aspectIRIs.get("ontology"));
//			jenaModel = AspectOWLUtils.owlOntologyToJenaModel(ontology, true);
//		}
//		catch (Exception e) {
//			fail( e.toString() );
//		}
//		
//		String prefixes = "PREFIX rdf: <http://www.w3.org/1999/02/22-rdf-syntax-ns#>"
//						+ "PREFIX owl: <http://www.w3.org/2002/07/owl#>"
//						+ "PREFIX rdfs: <http://www.w3.org/2000/01/rdf-schema#>"
//						+ "PREFIX xsd: <http://www.w3.org/2001/XMLSchema#>"
//						+ "PREFIX asp: <https://ontology.aspectowl.xyz/aspect_owl#>";
//		
//		// every line represents a query of the form: QUERY_STRING, EVALUATION_MODE, EXPECTATION
//		Object[][] queries = {
//			{ prefixes
//				+ "SELECT ?subject ?object"
//				+ "WHERE { ?subject rdfs:subClassOf ?object }", "amount", 16 },
//			{ prefixes
//				+ "SELECT ?subject ?object"
//				+ "WHERE { ?subject rdfs:subClassOf ?object ."
//				+ "        ?object rdfs:subClassOf ?object"
//				+ "}", "amount", 0 },
//			{ prefixes
//				+ "SELECT ?x"
//				+ "WHERE { ?x rdf:type asp:PointCut }", "contains", null },
//			{ prefixes
//				+ "SELECT ?x ?y"
//				+ "WHERE { ?x rdf:type asp:ReasoningComplexity }", "contains", ".*Owl(QL|RL|EL)Complexity.*" }
//		};
//		
//		for (Object[] queryLine : queries)
//		{
//			String queryString = String.valueOf(queryLine[0]);
//			String testingMode = String.valueOf(queryLine[1]);
//			Object expectation = queryLine[2];
//			
//			Query query = QueryFactory.create(queryString);
//			QueryExecution qexec = QueryExecutionFactory.create(query, jenaModel);
//			ResultSet results = qexec.execSelect();
//			
//			//ResultSetFormatter.out( results );
//			
//			switch ( testingMode )
//			{
//				case "amount":
//					assertEquals(Iterators.size(results), expectation);
//					break;
//				case "contains":
//					while (results.hasNext())
//					{
//						QuerySolution solution = results.nextSolution();
//						RDFNode rdfNode = solution.get("?x");
//						String rdfNodeString = String.valueOf(rdfNode);
//						assertTrue(rdfNodeString.matches(String.valueOf(expectation)));
//					}
//					break;
//				default:
//					assertTrue(false);
//			}
//			
//			qexec.close();
//		}
//		
//	}
//	
//	/**
//	 * Here we test the functionality of the converters to turn a Jena model 
//	 * into an OWL model or vice versa.
//	 * The Jena model format is used as intermediate exchange format.
//	 */
//	@AspectOrientedPreprocessingDocumentTarget
//	public void ontologyConverterTest()
//	{
//		OWLOntologyManager om = OWLManager.createOWLOntologyManager();
//		
//		HashMap<String,IRI> testIRIs = this.setup.get("aspect");
//		om.addIRIMapper(new SimpleIRIMapper(testIRIs.get("ontology"), testIRIs.get("document")));
//		
//		OWLOntology onto = null;
//		OntModel jenaModel = null;
//		OWLOntology ontoModel = null;
//		
//		try {
//			onto = om.loadOntology(testIRIs.get("ontology"));
//			jenaModel = AspectOWLUtils.owlOntologyToJenaModel(onto, false);
//			ontoModel = AspectOWLUtils.jenaModelToOWLOntology(jenaModel);
//		}
//		catch (Exception e) {
//			fail( e.toString() );
//		}
//		
//		assertEquals( onto.compareTo(ontoModel), 0 );
//	}
//	
//	/**
//	 * AspectOrientedPreprocessingDocumentTarget of the functionality to create ontologies with CONSTRUCT queries.
//	 * The resulting ontology is compared with an expected one.
//	 */
//	@AspectOrientedPreprocessingDocumentTarget
//	public void ontologyCreatorTest()
//	{
//		String QUERY = "PREFIX rdf: <http://www.w3.org/1999/02/22-rdf-syntax-ns#>\n"
//				+ "PREFIX owl: <http://www.w3.org/2002/07/owl#>\n"
//				+ "PREFIX rdfs: <http://www.w3.org/2000/01/rdf-schema#>\n"
//				+ "PREFIX xsd: <http://www.w3.org/2001/XMLSchema#>\n"
//				+ "PREFIX test: <http://www.semanticweb.org/test-ontology#>\n"
//				+ "CONSTRUCT { ?s ?p ?o }\n"
//				+ "WHERE\n"
//				+ "{\n"
//				+ "?s ?p ?o .\n"
//				+ "FILTER ( !sameTerm(?s, test:C) && !sameTerm(?o, test:C)  )\n"
//				+ "}";
//		
//		Query query = QueryFactory.create(QUERY);
//		
//		TreeSet<OWLAxiom> sortedAxioms = new TreeSet<>(new OWLAxiomComparator());
//		
//		try {
//			OWLOntologyManager om = OWLManager.createOWLOntologyManager();
//			
//			HashMap<String,IRI> testIRIs = this.setup.get("siblings");
//			om.addIRIMapper(new SimpleIRIMapper(testIRIs.get("ontology"), testIRIs.get("document")));
//			
//			OWLOntology onto = om.loadOntology(testIRIs.get("ontology"));
//			
//			OntModel jenaModel = AspectOWLUtils.owlOntologyToJenaModel(onto, true);
//			QueryExecution qexec = QueryExecutionFactory.create(query, jenaModel);
//			
//			Model result = ModelFactory.createOntologyModel( OntModelSpec.OWL_MEM );
//			result = qexec.execConstruct(result);
//			
//			OWLOntology module = AspectOWLUtils.jenaModelToOWLOntology(result);
//			Set<OWLAxiom> axioms = module.getAxioms();
//			
//			int EXPECTED_SIZE = 13;
//			//assertEquals(axioms.size(), EXPECTED_SIZE);
//			if (axioms.size() != EXPECTED_SIZE)
//			{
//				fail();
//				System.exit(0);
//			}
//			
//			sortedAxioms.addAll(axioms);
//		}
//		catch (Exception e) {
//			fail( e.toString() );
//		}
//		
//		TreeSet<OWLAxiom> expectedAxioms = new TreeSet<OWLAxiom>(new OWLAxiomComparator());
//		
//		try {
//			File file = new File(getClass().getResource("/sibs-sparse.owl").getPath());
//			IRI expectedIRI = IRI.create(file);
//			
//			OWLOntologyManager omg = OWLManager.createOWLOntologyManager();
//			OWLOntology onto = omg.loadOntology(expectedIRI);
//			
//			Set<OWLAxiom> axioms = onto.getAxioms();
//			expectedAxioms.addAll(axioms);
//		}
//		catch (OWLOntologyCreationException e)
//		{
//			fail( e.toString() );
//		}
//		
//		assertTrue(sortedAxioms.equals(expectedAxioms));
//	}

}
