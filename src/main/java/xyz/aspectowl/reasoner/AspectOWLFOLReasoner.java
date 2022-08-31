package xyz.aspectowl.reasoner;

import net.sf.tweety.logics.commons.syntax.Predicate;
import net.sf.tweety.logics.commons.syntax.Variable;
import net.sf.tweety.logics.fol.syntax.*;
import org.semanticweb.owlapi.model.*;
import org.semanticweb.owlapi.model.parameters.Imports;
import org.semanticweb.owlapi.reasoner.*;
import org.semanticweb.owlapi.reasoner.impl.OWLClassNode;
import org.semanticweb.owlapi.reasoner.impl.OWLReasonerBase;
import org.semanticweb.owlapi.util.Version;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import xyz.aspectowl.protege.AspectOWLEditorKitHook;
import xyz.aspectowl.tptp.reasoner.InconsistentOntologyException;
import xyz.aspectowl.tptp.reasoner.VampireTptpFolReasoner;

import javax.annotation.Nonnull;
import java.io.OutputStream;
import java.io.PrintStream;
import java.io.PrintWriter;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

/**
 * @author ralph
 */
public class AspectOWLFOLReasoner extends OWLReasonerBase {

    public static final String REASONER_NAME = "AspectOWL FOL Reasoner";

    private static final Logger log = LoggerFactory.getLogger(AspectOWLFOLReasoner.class);

    private static final Version VERSION = new Version(5, 6, 0, 0);

    private FolBeliefSet beliefSet;

    private ConcreteAspectOWL2TPTPObjectRenderer folTranslator;

    // TODO add preference for reasoner (vampire, spass, ...)
    private VampireTptpFolReasoner folReasoner;

    public AspectOWLFOLReasoner(@Nonnull OWLOntology rootOntology, @Nonnull BufferingMode bufferingMode) {
        this(rootOntology, new SimpleConfiguration(
                new NullReasonerProgressMonitor(),
                FreshEntityPolicy.ALLOW, // TODO Make this configurable?
                300,
                IndividualNodeSetPolicy.BY_SAME_AS) , bufferingMode);
    }

    public AspectOWLFOLReasoner(@Nonnull OWLOntology rootOntology, @Nonnull OWLReasonerConfiguration configuration, @Nonnull BufferingMode bufferingMode) {
        super(rootOntology, configuration, bufferingMode);
        // TODO get binary location this from prefs
        folReasoner = new VampireTptpFolReasoner("/Users/ralph/Diss/development/fol-theorem-provers/vampire-build/bin/vampire_rel_master_6344");

        // TODO find a way to get the AspectOWLManager without relying on protege-related code
        folTranslator = new ConcreteAspectOWL2TPTPObjectRenderer(AspectOWLEditorKitHook.getAspectManager(rootOntology.getOWLOntologyManager()), rootOntology, new PrintWriter(new PrintStream(OutputStream.nullOutputStream())), Imports.INCLUDED);
        folTranslator.setHumanReadableTptpNames(true);

        reloadOntology();
    }

    @Override
    protected void handleChanges(@Nonnull Set<OWLAxiom> addAxioms, @Nonnull Set<OWLAxiom> removeAxioms) {
        reloadOntology();
    }

    private void reloadOntology() {
        getRootOntology().accept(folTranslator);
        beliefSet = folTranslator.getBeliefSet();
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

    private static final FolFormula EXISTS_OWL_NOTHING = new ExistsQuantifiedFormula(new FolAtom(new Predicate("owlNothing", 1), new Variable("X")), new Variable("X"));

    @Override
    public boolean isConsistent() {
        try {
            folReasoner.query(beliefSet, EXISTS_OWL_NOTHING);
            return true;
        } catch (InconsistentOntologyException e) {
            return false;
        }
    }

    @Override
    public boolean isSatisfiable(@Nonnull OWLClassExpression classExpression) {

        if (classExpression instanceof OWLClass) {
            // TODO
            return true;
        } else {
            // TODO this is wrong. For a class expression c we must show that "not exists X : c(X)" is unprovable

            var folFormulae = folTranslator.makeFormula(folTranslator.translate(classExpression)).collect(Collectors.toList());
            var firstUniversalQuantification = (ForallQuantifiedFormula) folFormulae.get(0);
            firstUniversalQuantification.getPredicates();

            return folReasoner.query(beliefSet, new Conjunction(folFormulae));
        }
    }

    @Nonnull
    @Override
    public Node<OWLClass> getUnsatisfiableClasses() {
        return new OWLClassNode(getRootOntology().getClassesInSignature(Imports.INCLUDED).stream().
                filter(owlClass -> folReasoner.query(beliefSet,
                        new ExistsQuantifiedFormula(
                                new FolAtom(new Predicate(folTranslator.translate(owlClass), 1), new Variable("X")),
                                new Variable("X"))
                )).collect(Collectors.toSet()));
    }

    @Override
    public boolean isEntailed(@Nonnull OWLAxiom axiom) {
        var folFormulae = axiom.accept(folTranslator);
        return folReasoner.query(beliefSet, new Conjunction(folFormulae.collect(Collectors.toList())));
    }

    @Override
    public boolean isEntailed(@Nonnull Set<? extends OWLAxiom> axioms) {
//        var folFormulae = axiom.accept(folTranslator);
//        return folReasoner.query(beliefSet, new Conjunction(folFormulae.collect(Collectors.toList())));
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
