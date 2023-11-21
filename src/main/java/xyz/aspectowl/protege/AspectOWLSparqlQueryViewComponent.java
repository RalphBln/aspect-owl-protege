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
import java.awt.FlowLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;

import javax.swing.JButton;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTextPane;
import javax.swing.SwingUtilities;

import xyz.aspectowl.inference.AspectOWLSparqlQueryExecutor;
import org.protege.editor.core.ui.error.ErrorLogPanel;
import org.protege.editor.owl.ui.UIHelper;
import org.protege.editor.owl.ui.editor.OWLAnnotationEditor;
import org.protege.editor.owl.ui.list.OWLAxiomList;
import org.protege.editor.owl.ui.prefix.PrefixUtilities;
import org.protege.editor.owl.ui.view.AbstractActiveOntologyViewComponent;
import org.semanticweb.owlapi.model.AddAxiom;
import org.semanticweb.owlapi.model.OWLAnnotation;
import org.semanticweb.owlapi.model.OWLAxiom;
import org.semanticweb.owlapi.model.OWLOntology;
import org.semanticweb.owlapi.model.OWLOntologyChange;
import org.semanticweb.owlapi.model.OWLOntologyManager;
import org.semanticweb.owlapi.model.PrefixManager;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * @author ralph
 *
 */
public class AspectOWLSparqlQueryViewComponent extends AbstractActiveOntologyViewComponent {
	
	private static final long serialVersionUID = 5901526849643522633L;

	private static final Logger log = LoggerFactory.getLogger(AspectOWLSparqlQueryExecutor.class);

	private UIHelper uiHelper;
//	private NamespaceUtil nsUtil;
	private AspectOWLSparqlQueryExecutor sparqlExecutor;
	private JTextPane queryPane;

	private JButton executeButton;
	
	private OWLAnnotation selectedAspectAnnotation;
	
	/**
	 * 
	 */
	public AspectOWLSparqlQueryViewComponent() {
	}

	/**
	 * @see org.protege.editor.owl.ui.view.AbstractOWLViewComponent#initialiseOWLView()
	 */
	@Override
	protected void initialiseOntologyView() throws Exception {
		sparqlExecutor = new AspectOWLSparqlQueryExecutor(getOWLWorkspace());
//		nsUtil = new NamespaceUtil();
		uiHelper = new UIHelper(getOWLEditorKit());
		setLayout(new BorderLayout());
		add(getQueryPane(), BorderLayout.CENTER);
		add(getBottomPanel(), BorderLayout.SOUTH);
	}
	
	private JTextPane getQueryPane() {
		queryPane = new JTextPane();
		queryPane.setText(getSampleQuery());
		return queryPane;
	}
	
	/**
	 * 
	 * @return
	 */
	private JPanel getBottomPanel() {
		
		JPanel panel = new JPanel(new FlowLayout(FlowLayout.LEADING));
		
		JButton entitySelectionButton = new JButton("Select aspect");
		entitySelectionButton.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent e) {
				OWLAnnotationEditor annotationEditor = new OWLAnnotationEditor(getOWLEditorKit());
				int ret = uiHelper.showValidatingDialog("Select aspect to apply", annotationEditor.getEditorComponent(), null);

	            if (ret == JOptionPane.OK_OPTION) {
	                selectedAspectAnnotation = annotationEditor.getEditedObject();
	            }
	            
	            annotationEditor.dispose();
	            
                executeButton.setEnabled(selectedAspectAnnotation != null);
			}
		});
		panel.add(entitySelectionButton);
		
		executeButton = new JButton("Execute");
		executeButton.setEnabled(false);
		executeButton.addActionListener(new ActionListener() {
			
			@SuppressWarnings("unchecked")
			@Override
			public void actionPerformed(ActionEvent e) {
				try {
					String query = queryPane.getText();			
					OWLOntology module = sparqlExecutor.getOntologyModule(query);
					Set<OWLAxiom> axiomsInModule = module.getAxioms();
					
					// show results
					
					OWLAxiomList axiomList = new OWLAxiomList(getOWLEditorKit());
					axiomList.setListData(axiomsInModule.toArray());
					axiomList.setFixedCellHeight(24);

					JPanel panel = new JPanel(new BorderLayout());
//			        panel.setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10));
//			        panel.setPreferredSize(new Dimension(400, 300));
			        JScrollPane scrollPane = new JScrollPane(axiomList);
					panel.add(scrollPane, BorderLayout.CENTER);
					
					panel.add(new JLabel("<html><body style='width:400px'>Do you really want to add the aspect <b>" + selectedAspectAnnotation.getProperty().getIRI().getRemainder() + " " + selectedAspectAnnotation.getValue() +  " </b>to these axioms?</body></html>"), BorderLayout.NORTH);
					
					int result = JOptionPane.showConfirmDialog(SwingUtilities.getWindowAncestor(getOWLWorkspace()), panel, "Confirm", JOptionPane.YES_NO_OPTION, JOptionPane.PLAIN_MESSAGE);
					if (result == JOptionPane.YES_OPTION) {
						Set<OWLAnnotation> annotation = Collections.singleton(selectedAspectAnnotation);
						OWLOntologyManager om = getOWLModelManager().getOWLOntologyManager();
						OWLOntology activeOntology = getOWLModelManager().getActiveOntology();
						
				        List<OWLOntologyChange> changes = new ArrayList<OWLOntologyChange>();
						for (OWLAxiom axiom : axiomsInModule) {
							OWLAxiom axiomWithAspect = axiom.getAnnotatedAxiom(annotation);
							changes.add(new AddAxiom(activeOntology, axiomWithAspect));
						}
						om.applyChanges(changes);
					}
					
				}
				catch (Exception ex) {
//					ProtegeApplication.getErrorLog().logError(ex);
					ErrorLogPanel.showErrorDialog(ex);
					JOptionPane.showMessageDialog(getOWLWorkspace(), ex.getMessage() + "\nSee the logs for more information.");
				}
			}
		});
		
		panel.add(executeButton);
		
		return panel;
		
	}
	
	@Override
	protected void updateView(OWLOntology activeOntology) throws Exception {
		log.info("updateView");
	}

	/**
	 * @see org.protege.editor.owl.ui.view.AbstractOWLViewComponent#disposeOWLView()
	 */
	@Override
	protected void disposeOntologyView() {
		sparqlExecutor = null;
//		nsUtil = null;
		uiHelper = null;
	}
	
	/**
	 * Generates a sample query to be filled in the query text box.
	 * In particular, ns prefix mappings for all loaded ontologies (if defined)
	 * are added to the query.
	 * @return
	 */
	private String getSampleQuery() {
		
		StringBuilder buf = new StringBuilder();
		
		HashMap<String, String> namespace2PrefixMap = new HashMap<String, String>();
			
		// fill in default namespace prefixes
		// not necessary, because all these prefix mappings are duplicated in the PrefixManager
//		namespace2PrefixMap.putAll(nsUtil.getNamespace2PrefixMap());
		
		// fill in namespace prefixes declared in active ontology
		PrefixManager prefixManager = PrefixUtilities.getPrefixOWLOntologyFormat(getOWLModelManager());
		namespace2PrefixMap.putAll(prefixManager.getPrefixName2PrefixMap());
		
		fillBufferWithPrefixes(buf, namespace2PrefixMap);
		
		
		buf.append("PREFIX aspect: <https://ontology.aspectowl.xyz/aspect_owl#>\n");
		buf.append("PREFIX ex: <http://csw.inf.fu-berlin.de/aood/example#>\n");
				
		buf.append("CONSTRUCT {?s ?p ?o}\n");
		buf.append("	WHERE {?s ?p ?o}");
		return buf.toString();
	}
	
	private void fillBufferWithPrefixes(StringBuilder buf, Map<String, String> namespace2PrefixMap) {
		for (Entry<String, String> entry: namespace2PrefixMap.entrySet()) {
			String prefix = entry.getKey();
			String ns = entry.getValue();
			buf.append("PREFIX ");
			buf.append(prefix); // prefix already contains :
			buf.append(" <");
			buf.append(ns);
			buf.append(">\n");
		}

	}


}
