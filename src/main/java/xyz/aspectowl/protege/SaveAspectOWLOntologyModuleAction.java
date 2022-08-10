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
package xyz.aspectowl.protege;

import java.awt.event.ActionEvent;
import java.io.File;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import javax.swing.JCheckBox;
import javax.swing.JComboBox;
import javax.swing.JFrame;
import javax.swing.JOptionPane;
import javax.swing.SwingUtilities;

import org.coode.owlapi.latex.LatexOntologyFormat;
import org.coode.owlapi.manchesterowlsyntax.ManchesterOWLSyntaxOntologyFormat;
import org.coode.owlapi.obo.parser.OBOOntologyFormat;
import org.coode.owlapi.turtle.TurtleOntologyFormat;
import org.protege.editor.core.ui.action.ProtegeAction;
import org.protege.editor.core.ui.error.ErrorLogPanel;
import org.protege.editor.owl.OWLEditorKit;
import org.protege.editor.owl.model.OWLModelManager;
import org.protege.editor.owl.ui.OntologyFormatPanel;
import org.protege.editor.owl.ui.UIHelper;
import org.protege.editor.owl.ui.ontology.OntologyIDJDialog;
import org.semanticweb.owlapi.io.OWLFunctionalSyntaxOntologyFormat;
import org.semanticweb.owlapi.io.OWLXMLOntologyFormat;
import org.semanticweb.owlapi.io.RDFXMLOntologyFormat;
import org.semanticweb.owlapi.model.AddAxiom;
import org.semanticweb.owlapi.model.AddImport;
import org.semanticweb.owlapi.model.IRI;
import org.semanticweb.owlapi.model.OWLAnnotation;
import org.semanticweb.owlapi.model.OWLAxiom;
import org.semanticweb.owlapi.model.OWLDocumentFormat;
import org.semanticweb.owlapi.model.OWLImportsDeclaration;
import org.semanticweb.owlapi.model.OWLOntology;
import org.semanticweb.owlapi.model.OWLOntologyChange;
import org.semanticweb.owlapi.model.OWLOntologyCreationException;
import org.semanticweb.owlapi.model.OWLOntologyFormat;
import org.semanticweb.owlapi.model.OWLOntologyID;
import org.semanticweb.owlapi.model.OWLOntologyManager;

import com.google.common.base.Optional;

import de.uulm.ecs.ai.owlapi.krssparser.KRSS2OntologyFormat;
import uk.ac.manchester.cs.owl.owlapi.OWLImportsDeclarationImpl;

public class SaveAspectOWLOntologyModuleAction extends ProtegeAction {

	private enum SetOperation {
		COMPLEMENT, INTERSECTION, UNION
	}

	/**
     * 
     */
	private static final long serialVersionUID = 8969543617298643589L;
	private OWLOntology aspectOntology;
	private static boolean saveAnnotations;
	private java.util.List<OWLAnnotation> items;
	private SetOperation setOp;

	public void actionPerformed(ActionEvent e) {
		try {
			

			
			OWLEditorKit editorKit = (OWLEditorKit) getEditorKit();
	        

			items = new ArrayList<OWLAnnotation>();
			SelectAspectsPanel selectAspectPanel = new SelectAspectsPanel(
					editorKit, items);

			JComboBox<SetOperation> comboBox = new JComboBox<SetOperation>(SetOperation.values());

			Object[] params = { comboBox, selectAspectPanel };
			JOptionPane.showConfirmDialog(
					SwingUtilities.getAncestorOfClass(JFrame.class,
							editorKit.getOWLWorkspace()), params,
					"Select Aspects", JOptionPane.OK_CANCEL_OPTION,
					JOptionPane.PLAIN_MESSAGE);

			OWLOntology activeOntology = editorKit.getModelManager()
					.getActiveOntology();

			OWLModelManager om = editorKit.getModelManager();

			while (true) {
				try {

					OWLOntologyID id = showOntologyIDDialog(editorKit);

					if (id != null) {

						aspectOntology = om.createNewOntology(id, id.getDefaultDocumentIRI().get().toURI());

						List<OWLOntologyChange> changes = new ArrayList<OWLOntologyChange>();

						setOp = (SetOperation) comboBox.getSelectedItem();

						Set<OWLAxiom> AxiomsForNewOntology = getAxiomsForNewOntology(activeOntology);

						for (OWLAxiom axiom : AxiomsForNewOntology) {
							if (!saveAnnotations) {
								axiom = axiom.getAxiomWithoutAnnotations();
							}
							changes.add(new AddAxiom(aspectOntology, axiom));

						}
						
//						Set<OWLImportsDeclaration> importIRIs = activeOntology.getImportsDeclarations();
						
						// Keep all imports
						/*for ( OWLImportsDeclaration importIRI : importIRIs){
							changes.add(new AddImport(aspectOntology, importIRI));
							System.out.println(importIRI.getIRI());
						}*/
						
						OWLImportsDeclaration aspectOWLOnt = new OWLImportsDeclarationImpl(IRI.create("http://www.corporate-semantic-web.de/ontologies/aspect_owl"));
						changes.add(new AddImport(aspectOntology, aspectOWLOnt));

						
						om.applyChanges(changes);

						handleSaveAs(aspectOntology, editorKit);

						om.removeOntology(aspectOntology);
					}

					break;

				} catch (OWLOntologyCreationException CreationException) {
					JOptionPane.showMessageDialog(null, "IRI already exists.");
					System.out.println("IRI already exists");
				}
			}

		} catch (Exception e1) {

			ErrorLogPanel.showErrorDialog(e1);
		}
	}

	private OWLOntologyID showOntologyIDDialog(OWLEditorKit editorKit) {
		
		OWLOntology activeOntology = editorKit.getModelManager()
				.getActiveOntology();
		Optional<IRI> iri = Optional.of(IRI.create(activeOntology.getOntologyID().getOntologyIRI().toString(), (new Long(System.currentTimeMillis()).toString())));
		Optional<IRI> version = Optional.of(IRI.create(activeOntology.getOntologyID().getVersionIRI().toString()));
		OWLOntologyID id  = new OWLOntologyID(iri, version);
		OntologyIDJDialog dialog = new OntologyIDJDialog(id);
		JCheckBox checkbox = new JCheckBox("Keep aspect annotations");
		checkbox.setSelected(true);
		Object[] params = { checkbox, dialog };
		int response = JOptionPane.showConfirmDialog(
				SwingUtilities.getAncestorOfClass(JFrame.class,
						editorKit.getOWLWorkspace()), params,
				"Choose Ontology Name", JOptionPane.OK_CANCEL_OPTION,
				JOptionPane.PLAIN_MESSAGE);
		saveAnnotations = checkbox.isSelected();
		return response == JOptionPane.OK_OPTION ? dialog.getOntologyID()
				: null;
	}

	private boolean handleSaveAs(OWLOntology ont, OWLEditorKit editorKit)
			throws Exception { // private
		OWLOntologyManager man = editorKit.getModelManager()
				.getOWLOntologyManager();
		OWLDocumentFormat oldFormat = man.getOntologyFormat(ont);
		//workaround while showDialog is not OWLApi4-ready!
		//TODO: erase oldOntologyFormat as soon as showDialog is OWLApi4-ready
		OWLOntologyFormat oldOntologyFormat = null;
		switch (oldFormat.getClass().getName()) {
			case "org.semanticweb.owlapi.formats.RDFXMLDocumentFormat":
				oldOntologyFormat = new RDFXMLOntologyFormat();
				break;
			case "org.semanticweb.owlapi.formats.OWLXMLDocumentFormat":
				oldOntologyFormat = new OWLXMLOntologyFormat();
				break;
			case "org.semanticweb.owlapi.formats.FunctionalSyntaxDocumentFormat":
				oldOntologyFormat = new OWLFunctionalSyntaxOntologyFormat();
				break;
			case "org.semanticweb.owlapi.formats.ManchesterSyntaxDocumentFormat":
				oldOntologyFormat = new ManchesterOWLSyntaxOntologyFormat();
				break;
			case "org.semanticweb.owlapi.formats.OBODocumentFormat":
				oldOntologyFormat = new OBOOntologyFormat();
				break;
			case "org.semanticweb.owlapi.formats.KRSS2DocumentFormat":
				oldOntologyFormat = new KRSS2OntologyFormat();
				break;
			case "org.semanticweb.owlapi.formats.LatexDocumentFormat":
				oldOntologyFormat = new LatexOntologyFormat();
				break;
			case "org.semanticweb.owlapi.formats.TurtleDocumentFormat":
				oldOntologyFormat = new TurtleOntologyFormat();
				break;
			default:
				oldOntologyFormat = new OWLFunctionalSyntaxOntologyFormat();
		}
		OWLDocumentFormat format = OntologyFormatPanel.showDialog(editorKit,
				oldOntologyFormat, "Choose a format to use when saving the "
						+ editorKit.getModelManager().getRendering(ont)
						+ " ontology").get();
		if (format == null) {
			return false;
		}
		if (oldFormat.isPrefixOWLOntologyFormat()
				&& format.isPrefixOWLOntologyFormat()) {
			format.asPrefixOWLOntologyFormat().copyPrefixesFrom(oldFormat.asPrefixOWLOntologyFormat().getPrefixName2PrefixMap());
		}
		File file = getSaveAsOWLFile(ont, editorKit);
		if (file != null) {
			man.setOntologyFormat(ont, format);
			man.setOntologyDocumentIRI(ont, IRI.create(file));
			editorKit.getModelManager().save(ont);
			editorKit.addRecent(file.toURI());
			return true;
		} else {
			return false;
		}
	}

	private File getSaveAsOWLFile(OWLOntology ont, OWLEditorKit editorKit) {
		UIHelper helper = new UIHelper(editorKit);
		File file = helper
				.saveOWLFile("Please select a location in which to save: "
						+ editorKit.getModelManager().getRendering(ont));
		if (file != null) {
			int extensionIndex = file.toString().lastIndexOf('.');
			if (extensionIndex == -1) {
				file = new File(file.toString() + ".owl");
			} else if (extensionIndex != file.toString().length() - 4) {
				file = new File(file.toString() + ".owl");
			}
		}
		return file;
	}

	public void initialise() throws Exception {

	}

	public void dispose() {
	}

	private Set<OWLAxiom> getAxiomsForNewOntology(OWLOntology activeOntology) {
		Set<OWLAxiom> result = new HashSet<OWLAxiom>();

		for (OWLAnnotation item : items) {
			System.out.println("Selected Annotations : " + item);
		}
		for (OWLAxiom axiom : activeOntology.getAxioms()) {

			Set<OWLAnnotation> axiomAnnotations = axiom.getAnnotations();
			if (axiomAnnotations.equals(null)) {
				System.out.println("DEBUG: No Annotations for Axiom: " + axiom);
				break;
			}
			System.out.println("Annotations of current axiom : "
					+ axiomAnnotations);

			switch (setOp) {
			case COMPLEMENT: {
				Set<OWLAnnotation> tmp = new HashSet<OWLAnnotation>();
				for (OWLAnnotation annot : axiomAnnotations) {
					tmp.add(annot);
				}
				tmp.retainAll(items);
				if (tmp.isEmpty()) {
					result.add(axiom);
					System.out.println("Add Complement Axiom : " + axiom);

				}				
				break;
			}
			case INTERSECTION: {
				Set<OWLAnnotation> tmp = new HashSet<OWLAnnotation>();
				for (OWLAnnotation annot : axiomAnnotations) {
					tmp.add(annot);
				}
				tmp.addAll(items);
				if (tmp.equals(axiomAnnotations)) {
					result.add(axiom);
					System.out.println("Add Intersection Axiom : " + axiom);
				}
				break;
			}
			case UNION: {
				Set<OWLAnnotation> tmp = new HashSet<OWLAnnotation>();
				for (OWLAnnotation annot : axiomAnnotations) {
					tmp.add(annot);
				}
				tmp.retainAll(items);
				if (!tmp.isEmpty()) {
					result.add(axiom);
					System.out.println("Add Union Axiom : " + axiom);

				}

			}
			}
		}
		return result;
	}

}
