package xyz.aspectowl.tptp.renderer;

import net.sf.tweety.logics.fol.syntax.FolFormula;
import org.semanticweb.owlapi.model.*;
import org.semanticweb.owlapi.model.parameters.Imports;

import java.io.PrintWriter;
import java.io.Writer;
import java.util.stream.Stream;

/**
 * @author ralph
 */
public abstract class AspectOWL2TPTPObjectRenderer extends OWL2TPTPObjectRenderer implements AspectOWLObjectRenderer<Stream<FolFormula>> {

    public AspectOWL2TPTPObjectRenderer(OWLOntology ontology, Writer writer, Imports includeImportsClosure) {
        super(ontology, writer, includeImportsClosure);
    }

    @Override
    public Stream<FolFormula> visit(OWLNegativeObjectPropertyAssertionAxiom axiom) {
        return handleAspects(axiom, super.visit(axiom));
    }

    @Override
    public Stream<FolFormula> visit(OWLAsymmetricObjectPropertyAxiom axiom) {
        return handleAspects(axiom, super.visit(axiom));
    }

    @Override
    public Stream<FolFormula> visit(OWLDisjointClassesAxiom axiom) {
        return handleAspects(axiom, super.visit(axiom));
    }

    @Override
    public Stream<FolFormula> visit(OWLObjectPropertyDomainAxiom axiom) {
        return handleAspects(axiom, super.visit(axiom));
    }

    @Override
    public Stream<FolFormula> visit(OWLEquivalentObjectPropertiesAxiom axiom) {
        return handleAspects(axiom, super.visit(axiom));
    }

    @Override
    public Stream<FolFormula> visit(OWLDisjointObjectPropertiesAxiom axiom) {
        return handleAspects(axiom, super.visit(axiom));
    }

    @Override
    public Stream<FolFormula> visit(OWLObjectPropertyRangeAxiom axiom) {
        return handleAspects(axiom, super.visit(axiom));
    }

    @Override
    public Stream<FolFormula> visit(OWLFunctionalObjectPropertyAxiom axiom) {
        return handleAspects(axiom, super.visit(axiom));
    }

    @Override
    public Stream<FolFormula> visit(OWLSubObjectPropertyOfAxiom axiom) {
        return handleAspects(axiom, super.visit(axiom));
    }

    @Override
    public Stream<FolFormula> visit(OWLDisjointUnionAxiom axiom) {
        return handleAspects(axiom, super.visit(axiom));
    }

    @Override
    public Stream<FolFormula> visit(OWLSymmetricObjectPropertyAxiom axiom) {
        return handleAspects(axiom, super.visit(axiom));
    }

    @Override
    public Stream<FolFormula> visit(OWLAnnotationAssertionAxiom axiom) {
        return handleAspects(axiom, super.visit(axiom));
    }

    @Override
    public Stream<FolFormula> visit(OWLIrreflexiveObjectPropertyAxiom axiom) {
        return handleAspects(axiom, super.visit(axiom));
    }

    @Override
    public Stream<FolFormula> visit(OWLInverseFunctionalObjectPropertyAxiom axiom) {
        return handleAspects(axiom, super.visit(axiom));
    }

    @Override
    public Stream<FolFormula> visit(OWLSameIndividualAxiom axiom) {
        return handleAspects(axiom, super.visit(axiom));
    }

    @Override
    public Stream<FolFormula> visit(OWLSubPropertyChainOfAxiom axiom) {
        return handleAspects(axiom, super.visit(axiom));
    }

    @Override
    public Stream<FolFormula> visit(OWLInverseObjectPropertiesAxiom axiom) {
        return handleAspects(axiom, super.visit(axiom));
    }

    @Override
    public Stream<FolFormula> visit(OWLHasKeyAxiom axiom) {
        return handleAspects(axiom, super.visit(axiom));
    }

    @Override
    public Stream<FolFormula> visit(OWLDifferentIndividualsAxiom axiom) {
        return handleAspects(axiom, super.visit(axiom));
    }

    @Override
    public Stream<FolFormula> visit(OWLClassAssertionAxiom axiom) {
        return handleAspects(axiom, super.visit(axiom));
    }

    @Override
    public Stream<FolFormula> visit(OWLObjectPropertyAssertionAxiom axiom) {
        return handleAspects(axiom, super.visit(axiom));
    }

    @Override
    public Stream<FolFormula> visit(OWLTransitiveObjectPropertyAxiom axiom) {
        return handleAspects(axiom, super.visit(axiom));
    }

    @Override
    public Stream<FolFormula> visit(OWLReflexiveObjectPropertyAxiom axiom) {
        return handleAspects(axiom, super.visit(axiom));
    }

    @Override
    public Stream<FolFormula> visit(OWLSubClassOfAxiom axiom) {
        return handleAspects(axiom, super.visit(axiom));
    }

    @Override
    public Stream<FolFormula> visit(OWLEquivalentClassesAxiom axiom) {
        return handleAspects(axiom, super.visit(axiom));
    }
}
