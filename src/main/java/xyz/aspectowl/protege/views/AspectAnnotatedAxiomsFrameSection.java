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
package xyz.aspectowl.protege.views;

import java.util.Comparator;
import java.util.List;
import java.util.Set;
import java.util.TreeSet;

import org.protege.editor.core.ui.list.MListButton;
import org.protege.editor.owl.OWLEditorKit;
import org.protege.editor.owl.ui.editor.OWLObjectEditor;
import org.protege.editor.owl.ui.frame.AbstractOWLFrameSection;
import org.protege.editor.owl.ui.frame.AxiomListFrameSectionRow;
import org.protege.editor.owl.ui.frame.OWLFrame;
import org.protege.editor.owl.ui.frame.OWLFrameSectionRow;
import org.semanticweb.owlapi.model.OWLAxiom;
import org.semanticweb.owlapi.model.OWLOntology;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import xyz.aspectowl.owlapi.model.OWLAspectManager;
import xyz.aspectowl.protege.AspectOWLEditorKitHook;


/**
 * @author ralph
 *
 */
public class AspectAnnotatedAxiomsFrameSection extends AbstractOWLFrameSection<Set<OWLAxiom>, OWLAxiom, OWLAxiom> {

	private static final Logger log = LoggerFactory.getLogger(AspectAnnotatedAxiomsFrameSection.class);
	
	private OWLOntology aspectAnnotatedInferredAxioms;
	
	private OWLAspectManager aspectManager;

	
	/**
	 * @param editorKit
	 * @param frame
	 */
	public AspectAnnotatedAxiomsFrameSection(OWLEditorKit editorKit, OWLFrame<? extends Set<OWLAxiom>> frame) {
		super(editorKit, "Axioms with aspects", frame);
		aspectManager = AspectOWLEditorKitHook.getAspectManager(editorKit);
	}

	public void setAspectAnnotatedInferredAxioms(OWLOntology aspectAnnotatedInferredAxioms) {
		this.aspectAnnotatedInferredAxioms = aspectAnnotatedInferredAxioms;
	}

	/**
	 * @see org.protege.editor.owl.ui.frame.OWLFrameSection#getRowComparator()
	 */
	@Override
    public Comparator<OWLFrameSectionRow<Set<OWLAxiom>, OWLAxiom, OWLAxiom>> getRowComparator() {
        return new Comparator<OWLFrameSectionRow<Set<OWLAxiom>, OWLAxiom, OWLAxiom>>() {

            public int compare(OWLFrameSectionRow<Set<OWLAxiom>, OWLAxiom, OWLAxiom> o1,
                    OWLFrameSectionRow<Set<OWLAxiom>, OWLAxiom, OWLAxiom> o2) {

                int diff = o1.getAxiom().compareTo(o2.getAxiom());
                if(diff != 0) {
                    return diff;
                }
                else if (o1.getOntology() == null  && o2.getOntology() == null) {
                    return 0;
                }
                else if (o1.getOntology() == null) {
                    return -1;
                }
                else if (o2.getOntology() == null) {
                    return +1;
                }
                else {
                    return o1.getOntology().compareTo(o2.getOntology());
                }
            }
        };
	}
	
	/**
	 * @see org.protege.editor.owl.ui.frame.AbstractOWLFrameSection#createAxiom(Object)
	 */
	@Override
	protected OWLAxiom createAxiom(OWLAxiom axiom) {
		return axiom;
	}

	/**
	 * @see org.protege.editor.owl.ui.frame.AbstractOWLFrameSection#getObjectEditor()
	 */
	@Override
	public OWLObjectEditor<OWLAxiom> getObjectEditor() {
		return null;
	}

	/**
	 * @see org.protege.editor.owl.ui.frame.AbstractOWLFrameSection#refill(org.semanticweb.owlapi.model.OWLOntology)
	 */
	@Override
	protected void refill(OWLOntology ontology) {
		ontology.getAxioms().stream()
			.filter(axiom -> aspectManager.hasAssertedAspects(ontology, axiom))
			.forEach(axiom -> addRow(new AxiomListFrameSectionRow(getOWLEditorKit(), this, ontology, getRootObject(), axiom)));
	}
	
	@Override
	protected void refillInferred() {
		if (aspectAnnotatedInferredAxioms == null)
			return;
		
        for (OWLAxiom axiom : new TreeSet<OWLAxiom>(aspectAnnotatedInferredAxioms.getAxioms())) {
            boolean add = true;
            for (OWLOntology actOnt : getOWLModelManager().getActiveOntologies()) {
                if (actOnt.containsAxiom(axiom)) {
                    add = false;
                    break;
                }
            }
            if (add) {
            	log.info("Adding inferred axiom: " + axiom);
            	addInferredRowIfNontrivial(new AxiomListFrameSectionRow(getOWLEditorKit(), this, null, getRootObject(), axiom));
            }
        }

	}

	/**
	 * @see org.protege.editor.owl.ui.frame.AbstractOWLFrameSection#clear()
	 */
	@Override
	protected void clear() {
	}

	@Override
	public List<MListButton> getAdditionalButtons() {
		return super.getAdditionalButtons();
	}
}
