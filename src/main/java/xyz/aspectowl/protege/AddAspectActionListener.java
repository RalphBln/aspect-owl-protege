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
import java.awt.event.ActionListener;

import javax.swing.JOptionPane;

import org.protege.editor.core.ui.list.MList;
import org.protege.editor.owl.OWLEditorKit;
import org.protege.editor.owl.ui.UIHelper;
import org.protege.editor.owl.ui.editor.OWLAnnotationEditor;
import org.semanticweb.owlapi.model.OWLAnnotation;

public class AddAspectActionListener implements ActionListener{
	
	private java.util.List<OWLAnnotation> items;
	private OWLEditorKit eKit;
	private MList mList;
	
	public AddAspectActionListener(OWLEditorKit eKit, MList mList, java.util.List<OWLAnnotation> items){
		this.items = items; 
		this.eKit = eKit;
		this.mList = mList;
	}
	
	@SuppressWarnings("unchecked")
	@Override
	public void actionPerformed(ActionEvent arg0) {
		UIHelper uiHelper = new UIHelper(eKit);

		OWLAnnotationEditor annotationEditor = new OWLAnnotationEditor(
				eKit);
		int ret = uiHelper.showValidatingDialog("Select aspect to apply",
				annotationEditor.getEditorComponent(), null);

		if (ret == JOptionPane.OK_OPTION) {
			OWLAnnotation selectedAspectAnnotation = annotationEditor.getEditedObject();
		
			items.add((selectedAspectAnnotation));         
			mList.setListData(items.toArray());
		}
	}

}
