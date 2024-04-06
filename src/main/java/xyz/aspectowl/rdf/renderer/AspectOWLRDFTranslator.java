package xyz.aspectowl.rdf.renderer;

import org.semanticweb.owlapi.model.*;
import org.semanticweb.owlapi.rdf.model.RDFTranslator;
import org.semanticweb.owlapi.util.AxiomAppearance;
import org.semanticweb.owlapi.util.IndividualAppearance;
import xyz.aspectowl.owlapi.model.OWLAspectManager;
import xyz.aspectowl.rdf.RDFQuadGraph;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.atomic.AtomicInteger;

/**
 * @author Ralph Schäfermeier
 */
public class AspectOWLRDFTranslator extends RDFTranslator {
  
  private OWLAspectManager aspectManager;
  
  /**
   * @param manager          the manager
   * @param ontology         the ontology
   * @param format           target format
   * @param useStrongTyping  true if strong typing is required
   * @param occurrences      will tell whether anonymous individuals need an id or not
   * @param axiomOccurrences axiom occurrences
   * @param nextNode         counter for blank nodes
   * @param blankNodeMap     base for remapping nodes
   * @param translatedAxioms translated axioms
   */
  public AspectOWLRDFTranslator(@Nonnull OWLAspectManager aspectManager, @Nonnull OWLOntologyManager manager, @Nonnull OWLOntology ontology, @Nullable OWLDocumentFormat format, boolean useStrongTyping, IndividualAppearance occurrences, AxiomAppearance axiomOccurrences, AtomicInteger nextNode, Map<Object, Integer> blankNodeMap, Set<OWLAxiom> translatedAxioms) {
    super(manager, ontology, format, useStrongTyping, occurrences, axiomOccurrences, nextNode, blankNodeMap, translatedAxioms);
    this.aspectManager = aspectManager;
    graph = new RDFQuadGraph();
  }
  
  @Override
  public void visit(@Nonnull OWLSubClassOfAxiom axiom) {
    super.visit(axiom);
  }
  
  @Override
  public void visit(@Nonnull OWLEquivalentClassesAxiom axiom) {
    super.visit(axiom);
  }
  
  @Override
  public void visit(@Nonnull OWLDisjointClassesAxiom axiom) {
    super.visit(axiom);
  }
  
  @Override
  public void visit(@Nonnull OWLDisjointUnionAxiom axiom) {
    super.visit(axiom);
  }
  
  @Override
  public void visit(@Nonnull OWLSubObjectPropertyOfAxiom axiom) {
    super.visit(axiom);
  }
  
  @Override
  public void visit(@Nonnull OWLSubPropertyChainOfAxiom axiom) {
    super.visit(axiom);
  }
  
  @Override
  public void visit(@Nonnull OWLEquivalentObjectPropertiesAxiom axiom) {
    super.visit(axiom);
  }
  
  @Override
  public void visit(@Nonnull OWLDisjointObjectPropertiesAxiom axiom) {
    super.visit(axiom);
  }
  
  @Override
  public void visit(@Nonnull OWLObjectPropertyDomainAxiom axiom) {
    super.visit(axiom);
  }
  
  @Override
  public void visit(@Nonnull OWLObjectPropertyRangeAxiom axiom) {
    super.visit(axiom);
  }
  
  @Override
  public void visit(@Nonnull OWLInverseObjectPropertiesAxiom axiom) {
    super.visit(axiom);
  }
  
  @Override
  public void visit(@Nonnull OWLFunctionalObjectPropertyAxiom axiom) {
    super.visit(axiom);
  }
  
  @Override
  public void visit(@Nonnull OWLInverseFunctionalObjectPropertyAxiom axiom) {
    super.visit(axiom);
  }
  
  @Override
  public void visit(@Nonnull OWLReflexiveObjectPropertyAxiom axiom) {
    super.visit(axiom);
  }
  
  @Override
  public void visit(@Nonnull OWLIrreflexiveObjectPropertyAxiom axiom) {
    super.visit(axiom);
  }
  
  @Override
  public void visit(@Nonnull OWLSymmetricObjectPropertyAxiom axiom) {
    super.visit(axiom);
  }
  
  @Override
  public void visit(@Nonnull OWLAsymmetricObjectPropertyAxiom axiom) {
    super.visit(axiom);
  }
  
  @Override
  public void visit(@Nonnull OWLTransitiveObjectPropertyAxiom axiom) {
    super.visit(axiom);
  }
  
  @Override
  public void visit(@Nonnull OWLSubDataPropertyOfAxiom axiom) {
    super.visit(axiom);
  }
  
  @Override
  public void visit(@Nonnull OWLEquivalentDataPropertiesAxiom axiom) {
    super.visit(axiom);
  }
  
  @Override
  public void visit(@Nonnull OWLDisjointDataPropertiesAxiom axiom) {
    super.visit(axiom);
  }
  
  @Override
  public void visit(@Nonnull OWLDataPropertyDomainAxiom axiom) {
    super.visit(axiom);
  }
  
  @Override
  public void visit(@Nonnull OWLDataPropertyRangeAxiom axiom) {
    super.visit(axiom);
  }
  
  @Override
  public void visit(@Nonnull OWLFunctionalDataPropertyAxiom axiom) {
    super.visit(axiom);
  }
  
  @Override
  public void visit(@Nonnull OWLDatatypeDefinitionAxiom axiom) {
    super.visit(axiom);
  }
  
  @Override
  public void visit(@Nonnull OWLHasKeyAxiom axiom) {
    super.visit(axiom);
  }
  
  @Override
  public void visit(@Nonnull OWLSameIndividualAxiom axiom) {
    super.visit(axiom);
  }
  
  @Override
  public void visit(@Nonnull OWLDifferentIndividualsAxiom axiom) {
    super.visit(axiom);
  }
  
  @Override
  public void visit(@Nonnull OWLClassAssertionAxiom axiom) {
    super.visit(axiom);
  }
  
  @Override
  public void visit(@Nonnull OWLObjectPropertyAssertionAxiom axiom) {
    super.visit(axiom);
  }
  
  @Override
  public void visit(@Nonnull OWLNegativeObjectPropertyAssertionAxiom axiom) {
    super.visit(axiom);
  }
  
  @Override
  public void visit(@Nonnull OWLDataPropertyAssertionAxiom axiom) {
    super.visit(axiom);
  }
  
  @Override
  public void visit(@Nonnull OWLNegativeDataPropertyAssertionAxiom axiom) {
    super.visit(axiom);
  }
  
  @Override
  public void reset() {
    graph = new RDFQuadGraph();
  }
}
