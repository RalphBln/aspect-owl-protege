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
package de.fuberlin.csw.aspectowl.util;

import java.util.Set;

import org.semanticweb.owlapi.model.OWLAxiom;
import org.semanticweb.owlapi.model.OWLOntology;
import org.semanticweb.owlapi.model.OWLOntologyManager;

/**
 * This class filters duplicated axioms from inferred ontologies to circumvent a
 * particularity most OWL reasoners are affected with. Most OWL reasoners
 * duplicate annotated axioms when generating the inferred model, leaving one
 * annotated and one non-annotated version of each axiom in the ontology. This
 * class removes all non-annotated axioms if an annotated version of the same
 * axiom exists, leaving only the annotated versions.
 * 
 * @author ralph
 */
public class AnnotatedAxiomsDuplicateFilter {
	
	public static void filter(OWLOntology ontology) {
		
		OWLOntologyManager om = ontology.getOWLOntologyManager();
		
		Set<OWLAxiom> axioms = ontology.getAxioms();
		for (OWLAxiom axiom : axioms) {
			if (!axiom.getAnnotations().isEmpty()) {
				OWLAxiom axiomWithoutAnnotations = axiom.getAxiomWithoutAnnotations();
				if (ontology.containsAxiom(axiomWithoutAnnotations)) {
					om.removeAxiom(ontology, axiomWithoutAnnotations);
				}
			}
		}
	}
}
