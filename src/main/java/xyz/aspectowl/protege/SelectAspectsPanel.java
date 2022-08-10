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
import java.util.List;

import javax.swing.DefaultListModel;
import javax.swing.JButton;
import javax.swing.JComponent;
import javax.swing.JScrollPane;

import org.protege.editor.core.ui.list.MList;
import org.protege.editor.owl.OWLEditorKit;
import org.protege.editor.owl.ui.renderer.OWLAnnotationCellRenderer;
import org.protege.editor.owl.ui.renderer.OWLCellRenderer;
import org.semanticweb.owlapi.model.OWLAnnotation;
public class SelectAspectsPanel extends JComponent {

	/**
	 * 
	 */
//	private List<OWLAnnotation> items;
	private static final long serialVersionUID = 1L;
	private MList mList;
	private JScrollPane jScrollPane;
	private DefaultListModel<OWLAnnotation> defaultListModel;
	private JButton jButton;
//	private OWLEditorKit eKit;

	@SuppressWarnings("unchecked")
	public SelectAspectsPanel(final OWLEditorKit eKit, List<OWLAnnotation> items) {
//		this.items = items;
//		this.eKit = eKit;
		BorderLayout thisLayout = new BorderLayout();
		//this.setDefaultCloseOperation(WindowConstants.DISPOSE_ON_CLOSE);
		setLayout(thisLayout);

		defaultListModel = new DefaultListModel<OWLAnnotation>();

		mList = new MList();
        
		mList.setModel(defaultListModel);
		mList.setBackground(getBackground());
		mList.setEnabled(false);
		mList.setOpaque(true);
		mList.setCellRenderer(new OWLAnnotationCellRenderer(eKit));
		
		jScrollPane = new JScrollPane();
		jScrollPane.setPreferredSize(new java.awt.Dimension(392, 245));
		jScrollPane.setViewportView(mList);

		jButton = new JButton();
		jButton.setText("add");
		
		AddAspectActionListener actionListener = new AddAspectActionListener(eKit, mList, items);
		jButton.addActionListener(actionListener);

		setLayout(new BorderLayout(6, 6));
		//setPreferredSize( new Dimension(100, 300));

		// we need to use the OWLCellRenderer, so create a singleton JList
		final OWLCellRenderer ren = new OWLCellRenderer(eKit);
		ren.setHighlightKeywords(true);


		final JScrollPane scroller = new JScrollPane();

	    add(jButton, BorderLayout.SOUTH); 
		add(scroller, BorderLayout.NORTH);

		scroller.setViewportView(mList);
		
		setVisible(true);
	}

	public void dispose() {
	}

}
