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
package xyz.aspectowl.util;

import java.util.HashSet;
import java.util.Set;

import org.protege.editor.core.prefs.PreferencesManager;
import org.semanticweb.owlapi.model.IRI;
import org.semanticweb.owlapi.model.OWLAnnotationProperty;
import org.semanticweb.owlapi.model.OWLOntology;
import org.semanticweb.owlapi.search.EntitySearcher;

import xyz.aspectowl.preferences.AspectOWLPreferencesPanel;

/**
 * 
 * @author ralph
 */
public class AspectOWLUtils {
	
//	private static final Logger log = Logger.getLogger(AspectOWLUtils.class);
	
//	public final IRI hasAspectPropertyIRI = IRI.create("http://www.corporate-semantic-web.de/ontologies/aspect_owl#hasAspect");
//	public static final IRI ASPECT_BASE_CLASS_IRI = IRI.create("http://www.corporate-semantic-web.de/ontologies/aspect_owl#Aspect");
	
	
	/**
	 * Returns all annotation properties that are transitively sub-property of
	 * the top annotation property.
	 * @param onto an ontology
	 * @return All annotation properties in the given ontology that are transitively sub-property of
	 * the top annotation property.
	 */
	public static Set<OWLAnnotationProperty> getAllAspectAnnotationProperties (OWLOntology onto) {
		IRI hasAspectPropertyIRI = IRI.create(PreferencesManager.getInstance().getPreferencesForSet(AspectOWLPreferencesPanel.PREFERENCES_SET_ID, AspectOWLPreferencesPanel.PREFERENCES_ID_DEFAULT).getString(AspectOWLPreferencesPanel.PREFERENCES_ID_ASPECT_PROPERTY_IRI, AspectOWLPreferencesPanel.PREFERENCES_ID_ASPECT_PROPERTY_IRI_DEFAULT_VALUE));
		return fillSubProperties(new HashSet<OWLAnnotationProperty>(), onto.getOWLOntologyManager().getOWLDataFactory().getOWLAnnotationProperty(hasAspectPropertyIRI), onto);
	}
	
	private static Set<OWLAnnotationProperty> fillSubProperties(Set<OWLAnnotationProperty> set, OWLAnnotationProperty property, OWLOntology onto) {
		set.add(property);
		for(OWLAnnotationProperty subProperty : EntitySearcher.getSubProperties(property, onto, true)) {
			fillSubProperties(set, subProperty, onto);
		}
		return set;
	}
	
//	public static Set<OWLAspect> getAllAspects(OWLAxiom axiom, OWLOntology onto) {
//		getAllAspectAnnotationProperties(onto).stream().
//			map(annotationProperty -> axiom.getAnnotations(annotationProperty)).
//			flatMap(annotations -> annotations.stream()).
//			map(annotation -> annotation.getValue().accept(null));
//			
//	}
	
//	/**
//	 * Converts a Jena OntModel to an OWL API OWLOntology.
//	 * @param jenaModel A Jena OntModel.
//	 * @return The corresponding OWL API OWLOntology.
//	 */
//	public static OWLOntology jenaModelToOWLOntology(Model jenaModel) throws OWLOntologyCreationException {
//		RDFWriter writer = jenaModel.getWriter("RDF/XML");
//		ByteArrayOutputStream baos = new ByteArrayOutputStream();
//		writer.write(jenaModel, baos, null);
//		byte[] bytes = baos.toByteArray();
//		ReaderDocumentSource source = new ReaderDocumentSource(new InputStreamReader(new ByteArrayInputStream(bytes)));
//		
////		ByteArrayInputStream bais = new ByteArrayInputStream(bytes);
//		
//		OWLOntologyManager manager = OWLManager.createOWLOntologyManager();
//		OWLOntologyLoaderConfiguration config = new OWLOntologyLoaderConfiguration().setMissingImportHandlingStrategy(MissingImportHandlingStrategy.SILENT);
//		
//        OWLOntology owlOntology = manager.loadOntologyFromOntologyDocument(source, config);
//        return owlOntology;
//	}
//	
//	/**
//	 * Converts an OWL API OWLOntology to a Jena OntModel.
//	 * @param ontology An OWL API OWLOntology.
//	 * @return The corresponding Jena OntModel.
//	 * @throws OWLOntologyStorageException 
//	 */
//	public static OntModel owlOntologyToJenaModel(OWLOntology owlOntology, boolean withImports) throws OWLOntologyStorageException, OWLOntologyCreationException {
//		
//		OWLOntologyManager om = owlOntology.getOWLOntologyManager();
//		
//		if (withImports) {
//			owlOntology = new OWLOntologyMerger(new OWLOntologyImportsClosureSetProvider(om, owlOntology)).createMergedOntology(om, null);
//		}
//		
//		ByteArrayOutputStream baos = new ByteArrayOutputStream();
//		owlOntology.getOWLOntologyManager().saveOntology(owlOntology, new RDFXMLDocumentFormat(), baos);
//		byte[] bytes = baos.toByteArray();
//		
////		System.out.println("\n\n\n\n");
////		System.out.println(baos.toString());
////		System.out.println("\n\n\n\n");
//		
//		ByteArrayInputStream bais = new ByteArrayInputStream(bytes);		
//		OntModel jenaModel = ModelFactory.createOntologyModel( OntModelSpec.OWL_MEM );
//		jenaModel.read(bais, null, "RDF/XML");
//		return jenaModel;
//	}
	
	

}
