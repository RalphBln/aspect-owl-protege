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

import org.semanticweb.owlapi.model.OWLAnnotationAssertionAxiom;
import org.semanticweb.owlapi.model.OWLAnnotationPropertyDomainAxiom;
import org.semanticweb.owlapi.model.OWLAnnotationPropertyRangeAxiom;
import org.semanticweb.owlapi.model.OWLAsymmetricObjectPropertyAxiom;
import org.semanticweb.owlapi.model.OWLAxiom;
import org.semanticweb.owlapi.model.OWLClassAssertionAxiom;
import org.semanticweb.owlapi.model.OWLDataPropertyAssertionAxiom;
import org.semanticweb.owlapi.model.OWLDataPropertyDomainAxiom;
import org.semanticweb.owlapi.model.OWLDataPropertyRangeAxiom;
import org.semanticweb.owlapi.model.OWLDatatypeDefinitionAxiom;
import org.semanticweb.owlapi.model.OWLDeclarationAxiom;
import org.semanticweb.owlapi.model.OWLDifferentIndividualsAxiom;
import org.semanticweb.owlapi.model.OWLDisjointClassesAxiom;
import org.semanticweb.owlapi.model.OWLDisjointDataPropertiesAxiom;
import org.semanticweb.owlapi.model.OWLDisjointObjectPropertiesAxiom;
import org.semanticweb.owlapi.model.OWLDisjointUnionAxiom;
import org.semanticweb.owlapi.model.OWLEquivalentClassesAxiom;
import org.semanticweb.owlapi.model.OWLEquivalentDataPropertiesAxiom;
import org.semanticweb.owlapi.model.OWLEquivalentObjectPropertiesAxiom;
import org.semanticweb.owlapi.model.OWLFunctionalDataPropertyAxiom;
import org.semanticweb.owlapi.model.OWLFunctionalObjectPropertyAxiom;
import org.semanticweb.owlapi.model.OWLHasKeyAxiom;
import org.semanticweb.owlapi.model.OWLInverseFunctionalObjectPropertyAxiom;
import org.semanticweb.owlapi.model.OWLInverseObjectPropertiesAxiom;
import org.semanticweb.owlapi.model.OWLIrreflexiveObjectPropertyAxiom;
import org.semanticweb.owlapi.model.OWLNegativeDataPropertyAssertionAxiom;
import org.semanticweb.owlapi.model.OWLNegativeObjectPropertyAssertionAxiom;
import org.semanticweb.owlapi.model.OWLObjectPropertyAssertionAxiom;
import org.semanticweb.owlapi.model.OWLObjectPropertyDomainAxiom;
import org.semanticweb.owlapi.model.OWLObjectPropertyRangeAxiom;
import org.semanticweb.owlapi.model.OWLReflexiveObjectPropertyAxiom;
import org.semanticweb.owlapi.model.OWLSameIndividualAxiom;
import org.semanticweb.owlapi.model.OWLSubAnnotationPropertyOfAxiom;
import org.semanticweb.owlapi.model.OWLSubClassOfAxiom;
import org.semanticweb.owlapi.model.OWLSubDataPropertyOfAxiom;
import org.semanticweb.owlapi.model.OWLSubObjectPropertyOfAxiom;
import org.semanticweb.owlapi.model.OWLSubPropertyChainOfAxiom;
import org.semanticweb.owlapi.model.OWLSymmetricObjectPropertyAxiom;
import org.semanticweb.owlapi.model.OWLTransitiveObjectPropertyAxiom;
import org.semanticweb.owlapi.model.SWRLRule;
import org.semanticweb.owlapi.util.OWLAxiomVisitorAdapter;

/**
 * This class implements the handleDefault method as in newer versions of the
 * OWL API. The current OWL API version used in Protege does not implement this
 * method. This class is subject to deletion as soon as Protege is updated to
 * the new OWL API version.
 * 
 * @author ralph
 * 
 */
public class DefaultOWLAxiomVisitorAdapter extends OWLAxiomVisitorAdapter {

	protected void handleDefault(OWLAxiom owlObject) {
	}

	@Override
	public void visit(OWLAsymmetricObjectPropertyAxiom axiom) {
		handleDefault(axiom);
	}

	@Override
	public void visit(OWLClassAssertionAxiom axiom) {
		handleDefault(axiom);
	}

	@Override
	public void visit(OWLDataPropertyAssertionAxiom axiom) {
		handleDefault(axiom);
	}

	@Override
	public void visit(OWLDataPropertyDomainAxiom axiom) {
		handleDefault(axiom);
	}

	@Override
	public void visit(OWLDataPropertyRangeAxiom axiom) {
		handleDefault(axiom);
	}

	@Override
	public void visit(OWLDeclarationAxiom axiom) {
		handleDefault(axiom);
	}

	@Override
	public void visit(OWLDifferentIndividualsAxiom axiom) {
		handleDefault(axiom);
	}

	@Override
	public void visit(OWLDisjointClassesAxiom axiom) {
		handleDefault(axiom);
	}

	@Override
	public void visit(OWLDisjointDataPropertiesAxiom axiom) {
		handleDefault(axiom);
	}

	@Override
	public void visit(OWLDisjointObjectPropertiesAxiom axiom) {
		handleDefault(axiom);
	}

	@Override
	public void visit(OWLDisjointUnionAxiom axiom) {
		handleDefault(axiom);
	}

	@Override
	public void visit(OWLEquivalentClassesAxiom axiom) {
		handleDefault(axiom);
	}

	@Override
	public void visit(OWLEquivalentDataPropertiesAxiom axiom) {
		handleDefault(axiom);
	}

	@Override
	public void visit(OWLEquivalentObjectPropertiesAxiom axiom) {
		handleDefault(axiom);
	}

	@Override
	public void visit(OWLFunctionalDataPropertyAxiom axiom) {
		handleDefault(axiom);
	}

	@Override
	public void visit(OWLFunctionalObjectPropertyAxiom axiom) {
		handleDefault(axiom);
	}

	@Override
	public void visit(OWLHasKeyAxiom axiom) {
		handleDefault(axiom);
	}

	@Override
	public void visit(OWLInverseFunctionalObjectPropertyAxiom axiom) {
		handleDefault(axiom);
	}

	@Override
	public void visit(OWLInverseObjectPropertiesAxiom axiom) {
		handleDefault(axiom);
	}

	@Override
	public void visit(OWLIrreflexiveObjectPropertyAxiom axiom) {
		handleDefault(axiom);
	}

	@Override
	public void visit(OWLNegativeDataPropertyAssertionAxiom axiom) {
		handleDefault(axiom);
	}

	@Override
	public void visit(OWLNegativeObjectPropertyAssertionAxiom axiom) {
		handleDefault(axiom);
	}

	@Override
	public void visit(OWLObjectPropertyAssertionAxiom axiom) {
		handleDefault(axiom);
	}

	@Override
	public void visit(OWLSubPropertyChainOfAxiom axiom) {
		handleDefault(axiom);
	}

	@Override
	public void visit(OWLObjectPropertyDomainAxiom axiom) {
		handleDefault(axiom);
	}

	@Override
	public void visit(OWLObjectPropertyRangeAxiom axiom) {
		handleDefault(axiom);
	}

	@Override
	public void visit(OWLReflexiveObjectPropertyAxiom axiom) {
		handleDefault(axiom);
	}

	@Override
	public void visit(OWLSameIndividualAxiom axiom) {
		handleDefault(axiom);
	}

	@Override
	public void visit(OWLSubClassOfAxiom axiom) {
		handleDefault(axiom);
	}

	@Override
	public void visit(OWLSubDataPropertyOfAxiom axiom) {
		handleDefault(axiom);
	}

	@Override
	public void visit(OWLSubObjectPropertyOfAxiom axiom) {
		handleDefault(axiom);
	}

	@Override
	public void visit(OWLSymmetricObjectPropertyAxiom axiom) {
		handleDefault(axiom);
	}

	@Override
	public void visit(OWLTransitiveObjectPropertyAxiom axiom) {
		handleDefault(axiom);
	}

	@Override
	public void visit(SWRLRule rule) {
		handleDefault(rule);
	}

	@Override
	public void visit(OWLAnnotationAssertionAxiom axiom) {
		handleDefault(axiom);
	}

	@Override
	public void visit(OWLAnnotationPropertyDomainAxiom axiom) {
		handleDefault(axiom);
	}

	@Override
	public void visit(OWLAnnotationPropertyRangeAxiom axiom) {
		handleDefault(axiom);
	}

	@Override
	public void visit(OWLSubAnnotationPropertyOfAxiom axiom) {
		handleDefault(axiom);
	}

	@Override
	public void visit(OWLDatatypeDefinitionAxiom axiom) {
		handleDefault(axiom);
	}
}
