/**
 * 
 */
package de.fuberlin.csw.aspectowl.util;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.util.HashSet;
import java.util.Set;

import org.apache.log4j.Logger;
import org.semanticweb.owlapi.apibinding.OWLManager;
import org.semanticweb.owlapi.io.RDFXMLOntologyFormat;
import org.semanticweb.owlapi.model.IRI;
import org.semanticweb.owlapi.model.OWLAnnotationProperty;
import org.semanticweb.owlapi.model.OWLOntology;
import org.semanticweb.owlapi.model.OWLOntologyCreationException;
import org.semanticweb.owlapi.model.OWLOntologyManager;
import org.semanticweb.owlapi.model.OWLOntologyStorageException;
import org.semanticweb.owlapi.util.OWLOntologyImportsClosureSetProvider;
import org.semanticweb.owlapi.util.OWLOntologyMerger;

import com.hp.hpl.jena.ontology.OntModel;
import com.hp.hpl.jena.ontology.OntModelSpec;
import com.hp.hpl.jena.rdf.model.Model;
import com.hp.hpl.jena.rdf.model.ModelFactory;
import com.hp.hpl.jena.rdf.model.RDFWriter;

/**
 * 
 * @author ralph
 */
public class AspectOWLUtils {
	
	private static final Logger log = Logger.getLogger(AspectOWLUtils.class);
	
	public static final IRI hasAspectPropertyIRI = IRI.create("http://www.corporate-semantic-web.de/ontologies/aspect_owl#hasAspect");
	public static final IRI ASPECT_BASE_CLASS_IRI = IRI.create("http://www.corporate-semantic-web.de/ontologies/aspect_owl#Aspect");
	
	
	/**
	 * Returns all annotation properties that are transitively sub-property of
	 * the top annotation property.
	 * @param an ontology
	 * @return All annotation properties in the given ontology that are transitively sub-property of
	 * the top annotation property.
	 */
	public static Set<OWLAnnotationProperty> getAllAspectAnnotationProperties (OWLOntology onto) {
		return fillSubProperties(new HashSet<OWLAnnotationProperty>(), onto.getOWLOntologyManager().getOWLDataFactory().getOWLAnnotationProperty(hasAspectPropertyIRI), onto);
	}
	
	private static Set<OWLAnnotationProperty> fillSubProperties(Set<OWLAnnotationProperty> set, OWLAnnotationProperty property, OWLOntology onto) {
		set.add(property);
		for(OWLAnnotationProperty subProperty : property.getSubProperties(onto, true)) {
			fillSubProperties(set, subProperty, onto);
		}
		return set;
	}
	
	/**
	 * Converts a Jena OntModel to an OWL API OWLOntology.
	 * @param jenaModel A Jena OntModel.
	 * @return The corresponding OWL API OWLOntology.
	 */
	public static OWLOntology jenaModelToOWLOntology(Model jenaModel) throws OWLOntologyCreationException {
		RDFWriter writer = jenaModel.getWriter("RDF/XML");
		ByteArrayOutputStream baos = new ByteArrayOutputStream();
		writer.write(jenaModel, baos, null);
		byte[] bytes = baos.toByteArray();
		
		ByteArrayInputStream bais = new ByteArrayInputStream(bytes);
		OWLOntologyManager manager = OWLManager.createOWLOntologyManager();
		
		manager.setSilentMissingImportsHandling(true);
        OWLOntology owlOntology = manager.loadOntologyFromOntologyDocument(bais);
        return owlOntology;
	}
	
	/**
	 * Converts an OWL API OWLOntology to a Jena OntModel.
	 * @param ontology An OWL API OWLOntology.
	 * @return The corresponding Jena OntModel.
	 * @throws OWLOntologyStorageException 
	 */
	public static OntModel owlOntologyToJenaModel(OWLOntology owlOntology, boolean withImports) throws OWLOntologyStorageException, OWLOntologyCreationException {
		
		OWLOntologyManager om = owlOntology.getOWLOntologyManager();
		
		if (withImports) {
			owlOntology = new OWLOntologyMerger(new OWLOntologyImportsClosureSetProvider(om, owlOntology)).createMergedOntology(om, null);
		}
		
		ByteArrayOutputStream baos = new ByteArrayOutputStream();
		owlOntology.getOWLOntologyManager().saveOntology(owlOntology, new RDFXMLOntologyFormat(), baos);
		byte[] bytes = baos.toByteArray();
		
//		System.out.println("\n\n\n\n");
//		System.out.println(baos.toString());
//		System.out.println("\n\n\n\n");
		
		ByteArrayInputStream bais = new ByteArrayInputStream(bytes);		
		OntModel jenaModel = ModelFactory.createOntologyModel( OntModelSpec.OWL_MEM );
		jenaModel.read(bais, null, "RDF/XML");
		return jenaModel;
	}	

}
