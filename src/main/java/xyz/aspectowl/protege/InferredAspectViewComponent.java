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

import java.awt.BorderLayout;
import java.util.Set;

import javax.swing.JScrollPane;

import org.protege.editor.owl.model.event.OWLModelManagerChangeEvent;
import org.protege.editor.owl.model.event.OWLModelManagerListener;
import org.protege.editor.owl.model.inference.NoOpReasoner;
import org.protege.editor.owl.ui.framelist.OWLFrameList;
import org.protege.editor.owl.ui.view.AbstractActiveOntologyViewComponent;
import org.semanticweb.owlapi.model.OWLAxiom;
import org.semanticweb.owlapi.model.OWLOntology;
import org.semanticweb.owlapi.reasoner.OWLReasoner;

import xyz.aspectowl.inference.InferredAspectAnnotationGenerator;
import xyz.aspectowl.protege.views.AspectOWLAnnotatedAxiomsFrame;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;


/**
 * This class implements the Aspects view component.
 * 
 * @author ralph
 *
 */
public class InferredAspectViewComponent extends AbstractActiveOntologyViewComponent {

	private static final long serialVersionUID = -1509104956712586572L;

	private static final Logger log = LoggerFactory.getLogger(InferredAspectViewComponent.class);
	
//	private static final HashSet<InferredAspectViewComponent> components = new HashSet<InferredAspectViewComponent>();
	
	private InferredAspectAnnotationGenerator inferredAspectAnnotationGenerator;
	
	private OWLFrameList<Set<OWLAxiom>> frameList;
	
	private AspectOWLAnnotatedAxiomsFrame frame;
	
//	private OWLOntology inferredAspectAnnotatedAxioms;
	
	private OWLModelManagerListener listener = new OWLModelManagerListener() {

		@Override
		public void handleChange(OWLModelManagerChangeEvent event) {
			if (isSynchronizing()) {
				switch (event.getType()) {
				case ONTOLOGY_CLASSIFIED:
					try {
						log.info("Hurray! New inferred axioms coming in!");
						frame.setInferredAspectAnnotatedAxioms(inferredAspectAnnotationGenerator.inferAspects());
						updateView(getOWLModelManager().getActiveOntology());
					} catch (Exception e) {
						throw new RuntimeException(e);
					}
					break;
					
				case ABOUT_TO_CLASSIFY:
					
					break;
					
				case REASONER_CHANGED:
					log.info("Reasoner changed. Resetting view");
					frame.setInferredAspectAnnotatedAxioms(null);
					try {
						updateView(getOWLModelManager().getActiveOntology());
					} catch (Exception e) {
						throw new RuntimeException(e);
					}
					break;

				default:
					break;
				}
			}
		}
	};
	
//	private OWLOntologyChangeListener changeListener = new OWLOntologyChangeListener() {
//		
//		@Override
//		public void ontologiesChanged(List<? extends OWLOntologyChange> changes)
//				throws OWLException {
//			for (OWLOntologyChange change : changes) {
//				if (change instanceof OWLAxiomChange) {
//					Set<OWLEntity> signature = change.getSignature();
//					OWLAxiom axiom = ((OWLAxiomChange)change).getAxiom();
//					if (axiom instanceof OWLAnnotationAssertionAxiom) {
//						signature = ((OWLAnnotationAssertionAxiom)axiom).getSubject().getSignature();
//					}
//					System.out.println("Signature: " + signature);
//				}
//			}
//		}
//	};

	public InferredAspectViewComponent() throws Exception {
		
	}

//	private AspectPanel aspectPanel;


	// @Override
	protected void initialiseOntologyView() throws Exception {

		log.info("Aspect View Component initialized");

		setLayout(new BorderLayout());
		
		frame = new AspectOWLAnnotatedAxiomsFrame(getOWLEditorKit());
		frameList = new OWLFrameList<Set<OWLAxiom>>(getOWLEditorKit(), frame);
		add(new JScrollPane(frameList), BorderLayout.CENTER);
		updateHeader();
		
		inferredAspectAnnotationGenerator = new InferredAspectAnnotationGenerator(getOWLWorkspace());
		getOWLModelManager().addListener(listener);
		
//		getOWLModelManager().addOntologyChangeListener(changeListener);
		
		updateView(getOWLModelManager().getActiveOntology());
	}

	private void updateHeader() {
		OWLReasoner reasoner = getOWLModelManager().getOWLReasonerManager().getCurrentReasoner();
		if (reasoner instanceof NoOpReasoner) {
	        getView().setHeaderText("(no reasoner active)");
		} else {
	        getView().setHeaderText("(inferred axioms supplied by " + reasoner.getReasonerName() + ")");
		}
    }
	
	@Override
	protected void disposeOntologyView() {
		log.info("Aspect View Component disposed");
		
		getOWLModelManager().removeListener(listener);
//		getOWLModelManager().removeOntologyChangeListener(changeListener);
		
		frameList.dispose();
		
//		components.remove(this);
//		if (components.isEmpty()) {
//			getOWLModelManager().removeListener(InferredAspectAnnotationGenerator.getInstance(getOWLWorkspace()));
//		}
	}

	@Override
	protected void updateView(OWLOntology activeOntology) throws Exception {
        if (isSynchronizing()) {
            frameList.setRootObject(activeOntology.getAxioms());
            updateHeader();
        }
	}

}
