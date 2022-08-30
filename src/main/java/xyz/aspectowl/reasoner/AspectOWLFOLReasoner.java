package xyz.aspectowl.reasoner;

import net.sf.tweety.logics.commons.syntax.Predicate;
import net.sf.tweety.logics.commons.syntax.Variable;
import net.sf.tweety.logics.fol.parser.FolParser;
import net.sf.tweety.logics.fol.syntax.ExistsQuantifiedFormula;
import net.sf.tweety.logics.fol.syntax.FolAtom;
import net.sf.tweety.logics.fol.syntax.FolFormula;
import net.sf.tweety.logics.fol.writer.TPTPWriter;
import org.semanticweb.owlapi.model.*;
import org.semanticweb.owlapi.reasoner.*;
import org.semanticweb.owlapi.reasoner.impl.OWLReasonerBase;
import org.semanticweb.owlapi.util.Version;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import xyz.aspectowl.tptp.reasoner.VampireTptpFolReasoner;

import javax.annotation.Nonnull;
import java.util.Arrays;
import java.util.Collections;
import java.util.Set;

/**
 * @author ralph
 */
public class AspectOWLFOLReasoner extends OWLReasonerBase {

    public static final String REASONER_NAME = "AspectOWL FOL Reasoner";

    private static final Logger log = LoggerFactory.getLogger(AspectOWLFOLReasoner.class);

    private static final Version VERSION = new Version(5, 6, 0, 0);

    // TODO add preference for reasoner (vampire, spass, ...)
    private VampireTptpFolReasoner folReasoner;

    private static final FolFormula EXISTS_OWL_NOTHING = new ExistsQuantifiedFormula(new FolAtom(new Predicate("owlNothing", 1)), new Variable("X"));

    public AspectOWLFOLReasoner(@Nonnull OWLOntology rootOntology, @Nonnull BufferingMode bufferingMode) {
        this(rootOntology, new SimpleConfiguration(
                new NullReasonerProgressMonitor(),
                FreshEntityPolicy.ALLOW, // TODO Make this configurable?
                300,
                IndividualNodeSetPolicy.BY_SAME_AS) , bufferingMode);
    }

    public AspectOWLFOLReasoner(@Nonnull OWLOntology rootOntology, @Nonnull OWLReasonerConfiguration configuration, @Nonnull BufferingMode bufferingMode) {
        super(rootOntology, configuration, bufferingMode);
    }

    @Override
    protected void handleChanges(@Nonnull Set<OWLAxiom> addAxioms, @Nonnull Set<OWLAxiom> removeAxioms) {
        // TODO reload ontology
    }

    @Nonnull
    @Override
    public String getReasonerName() {
        return REASONER_NAME;
    }

    @Nonnull
    @Override
    public Version getReasonerVersion() {
        return VERSION;
    }

    @Override
    public void interrupt() {

    }

    @Override
    public void precomputeInferences(@Nonnull InferenceType... inferenceTypes) {
        log.warn("Precomputing is not supported yet. Inferences are computed on demand and cached.");
    }

    @Override
    public boolean isPrecomputed(@Nonnull InferenceType inferenceType) {
        return false;
    }

    @Nonnull
    @Override
    public Set<InferenceType> getPrecomputableInferenceTypes() {
        return Collections.emptySet();
    }

    @Override
    public boolean isConsistent() {
        return false;
    }

    @Override
    public boolean isSatisfiable(@Nonnull OWLClassExpression classExpression) {
        return false;
    }

    @Nonnull
    @Override
    public Node<OWLClass> getUnsatisfiableClasses() {
        return null;
    }

    @Override
    public boolean isEntailed(@Nonnull OWLAxiom axiom) {
        return false;
    }

    @Override
    public boolean isEntailed(@Nonnull Set<? extends OWLAxiom> axioms) {
        return false;
    }

    @Override
    public boolean isEntailmentCheckingSupported(@Nonnull AxiomType<?> axiomType) {
        return false;
    }

    @Nonnull
    @Override
    public Node<OWLClass> getTopClassNode() {
        return null;
    }

    @Nonnull
    @Override
    public Node<OWLClass> getBottomClassNode() {
        return null;
    }

    @Nonnull
    @Override
    public NodeSet<OWLClass> getSubClasses(@Nonnull OWLClassExpression ce, boolean direct) {
        return null;
    }

    @Nonnull
    @Override
    public NodeSet<OWLClass> getSuperClasses(@Nonnull OWLClassExpression ce, boolean direct) {
        return null;
    }

    @Nonnull
    @Override
    public Node<OWLClass> getEquivalentClasses(@Nonnull OWLClassExpression ce) {
        return null;
    }

    @Nonnull
    @Override
    public NodeSet<OWLClass> getDisjointClasses(@Nonnull OWLClassExpression ce) {
        return null;
    }

    @Nonnull
    @Override
    public Node<OWLObjectPropertyExpression> getTopObjectPropertyNode() {
        return null;
    }

    @Nonnull
    @Override
    public Node<OWLObjectPropertyExpression> getBottomObjectPropertyNode() {
        return null;
    }

    @Nonnull
    @Override
    public NodeSet<OWLObjectPropertyExpression> getSubObjectProperties(@Nonnull OWLObjectPropertyExpression pe, boolean direct) {
        return null;
    }

    @Nonnull
    @Override
    public NodeSet<OWLObjectPropertyExpression> getSuperObjectProperties(@Nonnull OWLObjectPropertyExpression pe, boolean direct) {
        return null;
    }

    @Nonnull
    @Override
    public Node<OWLObjectPropertyExpression> getEquivalentObjectProperties(@Nonnull OWLObjectPropertyExpression pe) {
        return null;
    }

    @Nonnull
    @Override
    public NodeSet<OWLObjectPropertyExpression> getDisjointObjectProperties(@Nonnull OWLObjectPropertyExpression pe) {
        return null;
    }

    @Nonnull
    @Override
    public Node<OWLObjectPropertyExpression> getInverseObjectProperties(@Nonnull OWLObjectPropertyExpression pe) {
        return null;
    }

    @Nonnull
    @Override
    public NodeSet<OWLClass> getObjectPropertyDomains(@Nonnull OWLObjectPropertyExpression pe, boolean direct) {
        return null;
    }

    @Nonnull
    @Override
    public NodeSet<OWLClass> getObjectPropertyRanges(@Nonnull OWLObjectPropertyExpression pe, boolean direct) {
        return null;
    }

    @Nonnull
    @Override
    public Node<OWLDataProperty> getTopDataPropertyNode() {
        return null;
    }

    @Nonnull
    @Override
    public Node<OWLDataProperty> getBottomDataPropertyNode() {
        return null;
    }

    @Nonnull
    @Override
    public NodeSet<OWLDataProperty> getSubDataProperties(@Nonnull OWLDataProperty pe, boolean direct) {
        return null;
    }

    @Nonnull
    @Override
    public NodeSet<OWLDataProperty> getSuperDataProperties(@Nonnull OWLDataProperty pe, boolean direct) {
        return null;
    }

    @Nonnull
    @Override
    public Node<OWLDataProperty> getEquivalentDataProperties(@Nonnull OWLDataProperty pe) {
        return null;
    }

    @Nonnull
    @Override
    public NodeSet<OWLDataProperty> getDisjointDataProperties(@Nonnull OWLDataPropertyExpression pe) {
        return null;
    }

    @Nonnull
    @Override
    public NodeSet<OWLClass> getDataPropertyDomains(@Nonnull OWLDataProperty pe, boolean direct) {
        return null;
    }

    @Nonnull
    @Override
    public NodeSet<OWLClass> getTypes(@Nonnull OWLNamedIndividual ind, boolean direct) {
        return null;
    }

    @Nonnull
    @Override
    public NodeSet<OWLNamedIndividual> getInstances(@Nonnull OWLClassExpression ce, boolean direct) {
        return null;
    }

    @Nonnull
    @Override
    public NodeSet<OWLNamedIndividual> getObjectPropertyValues(@Nonnull OWLNamedIndividual ind, @Nonnull OWLObjectPropertyExpression pe) {
        return null;
    }

    @Nonnull
    @Override
    public Set<OWLLiteral> getDataPropertyValues(@Nonnull OWLNamedIndividual ind, @Nonnull OWLDataProperty pe) {
        return null;
    }

    @Nonnull
    @Override
    public Node<OWLNamedIndividual> getSameIndividuals(@Nonnull OWLNamedIndividual ind) {
        return null;
    }

    @Nonnull
    @Override
    public NodeSet<OWLNamedIndividual> getDifferentIndividuals(@Nonnull OWLNamedIndividual ind) {
        return null;
    }
}
