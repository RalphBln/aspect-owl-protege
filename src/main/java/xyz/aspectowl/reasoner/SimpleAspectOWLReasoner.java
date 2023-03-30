/**
 * 
 */
package xyz.aspectowl.reasoner;

// TODO Ralph commented out for now since this would require more fumbling with the OSGI manifest, and I'd prefer to 
// deal with this later. 

//import java.io.File;
//import java.io.FileInputStream;
//import java.io.FileOutputStream;
//import java.io.IOException;
//import java.io.ObjectInputStream;
//import java.io.ObjectOutputStream;
//import java.security.MessageDigest;
//import java.security.NoSuchAlgorithmException;
//import java.util.HashMap;
//import java.util.Set;
//import java.util.TreeSet;
//
//import org.mindswap.pellet.exceptions.InconsistentOntologyException;
//import org.protege.xmlcatalog.owlapi.XMLCatalogIRIMapper;
//import org.semanticweb.owl.explanation.api.Explanation;
//import org.semanticweb.owl.explanation.api.ExplanationGenerator;
//import org.semanticweb.owl.explanation.impl.blackbox.checker.InconsistentOntologyExplanationGeneratorFactory;
//import org.semanticweb.owlapi.apibinding.OWLManager;
//import org.semanticweb.owlapi.model.IRI;
//import org.semanticweb.owlapi.model.OWLAxiom;
//import org.semanticweb.owlapi.model.OWLClassExpression;
//import org.semanticweb.owlapi.model.OWLDataFactory;
//import org.semanticweb.owlapi.model.OWLIndividual;
//import org.semanticweb.owlapi.model.OWLOntology;
//import org.semanticweb.owlapi.model.OWLOntologyCreationException;
//import org.semanticweb.owlapi.model.OWLOntologyManager;
//import org.semanticweb.owlapi.model.OWLSubClassOfAxiom;
//import org.semanticweb.owlapi.model.parameters.Imports;
//import org.semanticweb.owlapi.reasoner.InferenceType;
//import org.semanticweb.owlapi.reasoner.OWLReasoner;
//import org.semanticweb.owlapi.util.InferredOntologyGenerator;
//
//import com.clarkparsia.modularity.IncrementalClassifier;
//import com.clarkparsia.modularity.PelletIncremantalReasonerFactory;
//import com.clarkparsia.pellet.owlapiv3.PelletReasonerFactory;

/**
 * @author ralph
 */
public class SimpleAspectOWLReasoner {

	// private HashMap<OWLClassExpression, Set<OWLAxiom>> axiomsWithAspects;
	//
	// private Set<Set<OWLIndividual>> incompatibleWorlds;

	/**
	 * 
	 */
	public SimpleAspectOWLReasoner() {
	}

	// public static void main2(String[] args) {
	// try {
	// OWLOntologyManager om = OWLManager.createOWLOntologyManager();
	// OWLOntology onto = om.loadOntologyFromOntologyDocument(IRI.create(new
	// File("/Users/ralph/Documents/Diss/Ontologien/Test/Inonconsistent.ofn")));
	// PelletIncremantalReasonerFactory rFac =
	// PelletIncremantalReasonerFactory.getInstance();
	// IncrementalClassifier reasoner = rFac.createReasoner(onto);
	// reasoner.setMultiThreaded(false);
	// try {
	// reasoner.classify();
	// } catch (InconsistentOntologyException ex) {
	// InconsistentOntologyExplanationGeneratorFactory expGenFac = new
	// InconsistentOntologyExplanationGeneratorFactory(rFac, Long.MAX_VALUE);
	// ExplanationGenerator<OWLAxiom> expGen =
	// expGenFac.createExplanationGenerator(onto);
	//
	// OWLDataFactory df = om.getOWLDataFactory();
	// OWLSubClassOfAxiom entailment =
	// df.getOWLSubClassOfAxiom(df.getOWLThing(), df.getOWLNothing());
	//
	// Set<Explanation<OWLAxiom>> explanations =
	// expGen.getExplanations(entailment);
	// for (Explanation<OWLAxiom> explanation : explanations) {
	// System.out.println(explanation);
	// System.out.println();
	// }
	//
	// }
	//
	// } catch (OWLOntologyCreationException e) {
	// e.printStackTrace();
	// }
	//
	// }
	//
	// // TODO remove
	// public static void main(String[] args) {
	// try {
	//
	// OWLOntologyManager om = OWLManager.createOWLOntologyManager();
	// OWLOntologyManager tempOM = OWLManager.createOWLOntologyManager();
	//
	// om.addIRIMapper(new XMLCatalogIRIMapper(new
	// File("/Users/ralph/Documents/Diss/Ontologien/AspectOWL/ontology/examples/temporal/catalog-v001.xml")));
	//
	// OWLOntology onto = om.loadOntologyFromOntologyDocument(IRI.create(new
	// File("/Users/ralph/Documents/Diss/Ontologien/AspectOWL/ontology/examples/temporal/temporal.owl")));
	//
	// OWLReasoner reasoner =
	// PelletReasonerFactory.getInstance().createReasoner(onto);
	// reasoner.precomputeInferences(InferenceType.values());
	//
	// OWLOntology inferredOnt =
	// tempOM.createOntology(IRI.create("http://another.com/ontology" +
	// System.currentTimeMillis()));
	// InferredOntologyGenerator ontGen = new
	// InferredOntologyGenerator(reasoner);
	// ontGen.fillOntology(tempOM.getOWLDataFactory(), inferredOnt);
	//
	//
	//
	// TreeSet<OWLAxiom> inferredAxioms = new TreeSet<OWLAxiom>();
	// for (OWLAxiom axiom : inferredOnt.getAxioms(Imports.INCLUDED)) {
	// if(!onto.containsAxiomIgnoreAnnotations(axiom)) {
	// inferredAxioms.add(axiom);
	// }
	// }
	//
	// MessageDigest md = MessageDigest.getInstance("SHA");
	// md.update(inferredAxioms.toString().getBytes());
	//
	// byte[] mdbytes = md.digest();
	//
	// StringBuilder sb = new StringBuilder();
	// for (int i = 0; i < mdbytes.length; i++) {
	// sb.append(Integer.toString((mdbytes[i] & 0xff) + 0x100,
	// 16).substring(1));
	// }
	//
	// String hash = sb.toString();
	// System.out.println(hash);
	//
	//
	//
	// File[] dir = new File("/Users/ralph/temp").listFiles();
	// for (File file : dir) {
	// String name = file.getName();
	// if (name.startsWith(hash)) {
	// ObjectInputStream in = new ObjectInputStream(new FileInputStream(file));
	//
	// break;
	// }
	// }
	//
	// ObjectOutputStream out = new ObjectOutputStream(new
	// FileOutputStream("/Users/ralph/"));
	//
	//
	//
	// } catch (OWLOntologyCreationException e) {
	// e.printStackTrace();
	// } catch (IOException e) {
	// e.printStackTrace();
	// } catch (NoSuchAlgorithmException e) {
	// e.printStackTrace();
	// }
	//
	// }
}
