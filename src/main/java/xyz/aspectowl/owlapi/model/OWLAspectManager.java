package xyz.aspectowl.owlapi.model;

import xyz.aspectowl.owlapi.model.impl.OWLAnonymousAspectImpl;
import xyz.aspectowl.owlapi.model.impl.OWLAspectAssertionAxiomImpl;
import xyz.aspectowl.owlapi.model.impl.OWLNamedAspectImpl;
import org.semanticweb.owlapi.model.*;
import org.semanticweb.owlapi.model.parameters.Imports;
import org.semanticweb.owlapi.util.CollectionFactory;
import org.semanticweb.owlapi.util.OWLOntologyChangeVisitorAdapter;

import javax.annotation.Nonnull;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.stream.Collectors;
import java.util.stream.Stream;

/* I know. Calling a class 'SomethingManager' is an anti-pattern. What brought me here was days of
  unsuccessful experimenting with OSGI byte code weaving (in order to add all the aspect related
  functionality to the existing OWL object interfaces in the OWLAPI, hacks for replacing the
  OWLDataFactory during or after the initialization of the Protégé OWL workspace (bottom line:
  not possible),   */

/**
 * The central facility for managing all aspect-related issues.
 * At runtime there exists exactly one instance of this class per Protege workspace instance.
 */
public class OWLAspectManager extends OWLOntologyChangeVisitorAdapter implements OWLOntologyChangeListener {

    private ConcurrentHashMap<OntologyObjectTuple<AspectOWLPointcut>, Set<OWLAspectAssertionAxiom>> aspectsForPointcut = CollectionFactory.createSyncMap();
    private ConcurrentHashMap<OWLObject, Set<OWLAspectAssertionAxiom>> axiomsBySignature = CollectionFactory.createSyncMap();

    /**
     * Creates and returns a new OWLAspect constructed from the given ontology, join point and advice class expression.
     * @param expr
     * @param annotations
     * @param aspects nested aspects
     * @return
     */
    public OWLAspect getAspect(OWLClassExpression expr, Set<OWLAnnotation> annotations, Set<OWLAspect> aspects) {
        if (expr.isAnonymous()) {
            return new OWLAnonymousAspectImpl((OWLAnonymousClassExpression)expr, annotations, aspects);
        }
        return new OWLNamedAspectImpl(((OWLClass) expr).getIRI(), annotations, aspects);
    }

    /**
     * Creates and returns a new OWLAspectAssertionAxiom for the given ontology, joint point, and aspect.
     * (Needed for creating object aspects)
     * @param ontology
     * @param pointcut
     * @param aspect
     * @return
     */
    public OWLAspectAssertionAxiom getAspectAssertionAxiom(OWLOntology ontology, AspectOWLPointcut pointcut, OWLAspect aspect) {
        return new OWLAspectAssertionAxiomImpl(ontology, pointcut, aspect);
    }

    /**
     * Returns a set containing all aspects asserted for the given object.
     * @param potentialJoinPoint a potential join point consisting of an owl object
     * @return a stream containing all aspects asserted for the given join point
     */
    public Set<OWLAspect> getAssertedAspects(OWLOntology ontology, OWLAxiom potentialJoinPoint) {
        return Optional.ofNullable(aspectsForPointcut.get(new OntologyObjectTuple(ontology, new AspectOWLJoinPointAxiomPointcut(potentialJoinPoint.getAxiomWithoutAnnotations())))).orElse(Collections.emptySet()).stream().map(axiom -> axiom.getAspect()).collect(Collectors.toSet());
    }

    /**
     * Returns a set containing all aspects asserted for the given object.
     * @param ontology the contology containing the object declaration
     * @param potentialJoinPoint a potential join point consisting of an owl object
     * @return a stream containing all aspects asserted for the given join point
     */
    public Set<OWLAspectAssertionAxiom> getAspectAssertionAxioms(OWLOntology ontology, OWLAxiom potentialJoinPoint) {
        return CollectionFactory.getCopyOnRequestSet(Optional.ofNullable(aspectsForPointcut.get(new OntologyObjectTuple(ontology, new AspectOWLJoinPointAxiomPointcut(potentialJoinPoint.getAxiomWithoutAnnotations())))).orElse(Collections.emptySet()));
    }

    /**
     * Returns a set containing all aspect assertion axioms with the given aspect for the given ontology and potentially its imports.
     * @param ontology the contology containing the object declaration
     * @param aspect aspect for filtering the axioms
     * @return a stream containing all aspects asserted for the given join point
     */
    public Set<OWLAspectAssertionAxiom> getAspectAssertionAxioms(OWLOntology ontology, OWLAspect aspect, Imports includeImportsClosure) {
        return CollectionFactory.getCopyOnRequestSet(aspectsForPointcut.values().stream().flatMap(a -> a.stream()).filter(axiom -> axiom.getAspect().equals(aspect)).collect(Collectors.toCollection(HashSet<OWLAspectAssertionAxiom>::new)));
    }

    public void removeAspectAssertionAxiom(OWLOntology ontology, OWLAspectAssertionAxiom aspectAssertionAxiom) {
        Optional.ofNullable(aspectsForPointcut.get(new OntologyObjectTuple(ontology, aspectAssertionAxiom.getPointcut()))).orElse(Collections.emptySet()).remove(aspectAssertionAxiom);
    }

    public boolean hasAssertedAspects(OWLOntology ontology, OWLAxiom joinPointAxiom) {
        return !Optional.ofNullable(aspectsForPointcut.get(new OntologyObjectTuple(ontology, new AspectOWLJoinPointAxiomPointcut(joinPointAxiom.getAxiomWithoutAnnotations())))).orElse(Collections.emptySet()).isEmpty();
    }

    public boolean hasAspects(OWLOntology ontology) {
        for (OntologyObjectTuple tuple : aspectsForPointcut.keySet()) {
            if (tuple.ontology.equals(ontology)) {
                return true;
            }
        }
        return false;
    }

    /**
     * Returns a set containing all aspects in all of the given ontologies
     * @param activeOntologies
     * @return
     */
    public Stream<OWLClass> getAllAspects(Set<OWLOntology> activeOntologies) {
        return activeOntologies.stream().flatMap(ontology -> ontology.getClassesInSignature(Imports.INCLUDED).stream()).filter(clsExpr -> isAspectInOntology(clsExpr, activeOntologies));
    }

    private HashMap<OntologyObjectTuple<OWLClassExpression>, Boolean> cache = new HashMap<>();
    
    /**
     * Returns true if the given class expression has the role of an aspect in one of the given ontologies.
     * @param clsExpr
     * @param activeOntologies
     * @return
     */
    public boolean isAspectInOntology(OWLClassExpression clsExpr, Set<OWLOntology> activeOntologies) {
        // TODO this is called often and is not efficient. Needs some sort of caching.
    	// see issue #14

//        return aspectsForPointcut.keySet().stream().filter(tuple -> activeOntologies.contains(tuple.ontology)).map(key ->
//                aspectsForPointcut.get(key)).flatMap(set -> set.stream()).map(axiom ->
//                axiom.getAspect()).filter(aspect -> aspect.equals(clsExpr)).count() != 0;

    	
    	
        for (OntologyObjectTuple<AspectOWLPointcut> tuple : aspectsForPointcut.keySet()) {
            for (OWLOntology activeOntology : activeOntologies) {
                if (activeOntology.equals(tuple.ontology)) { // HashSet.contains does not work here because an OWLOntologyImpl's hashCode is calculated based on the ontology's ID which may change after it was added to the set.
                    for (OWLAspectAssertionAxiom axiom : aspectsForPointcut.get(tuple)) {
                        OWLAspect aspect = axiom.getAspect();
                        if (aspect instanceof OWLNamedAspect) {
                            if (aspect.equals(clsExpr)) {
                                return true;
                            }
                            if (getEquivalentClassExpressions((OWLClass) aspect, activeOntologies).contains(clsExpr)) {
                                return true;
                            }
                        } else {
                            if (((OWLAnonymousAspect) aspect).getClassExpression().equals(clsExpr)) {
                                return true;
                            }
                            if (getEquivalentClassExpressions((OWLAnonymousClassExpression) aspect, activeOntologies).contains(clsExpr)) {
                                return true;
                            }
                        }
                    }
                }
            }
        }
        return false;
    }

    private Set<OWLClassExpression> getEquivalentClassExpressions(OWLClass cls, Set<OWLOntology> activeOntologies) {
        HashSet<OWLClassExpression> result = new HashSet<>();
        for (OWLOntology ontology : activeOntologies) {
            for (OWLEquivalentClassesAxiom eqClassAxiom : ontology.getEquivalentClassesAxioms(cls)) {
                result.addAll(eqClassAxiom.getClassExpressions());
            }
        }
        return result;
    }

    private Set<OWLClassExpression> getEquivalentClassExpressions(OWLAnonymousClassExpression clsExpr, Set<OWLOntology> activeOntologies) {
        HashSet<OWLClassExpression> result = new HashSet<>();
        for (OWLOntology ontology : activeOntologies) {
            for (OWLClass cls : ontology.getClassesInSignature()) {
                for (OWLEquivalentClassesAxiom eqClassAxiom : ontology.getEquivalentClassesAxioms(cls)) {
                    Set<OWLClassExpression> equivalentExpressions = eqClassAxiom.getClassExpressions();
                    if (equivalentExpressions.contains(clsExpr)) {
                        result.addAll(equivalentExpressions);
                    }
                }
            }
        }
        return result;
    }


    /**
     * Returns a stream of all inferred aspects for the given object.
     * @param potentialJoinPoint a potential join point consisting of an owl object
     * @return a stream containing all inferred aspects for the given join point
     */
    public Set<OWLAspect> getInferredAspects(OWLOntology ontology, OWLAxiom potentialJoinPoint) {
        // TODO

        return Collections.emptySet();
    }

    public void addAspect(OWLOntology ontology, OWLAspectAssertionAxiom aspectAssertionAxiom) {
        OntologyObjectTuple<AspectOWLPointcut> key = new OntologyObjectTuple<>(ontology, aspectAssertionAxiom.getPointcut());
        Set<OWLAspectAssertionAxiom> aspects = aspectsForPointcut.get(key);
        if (aspects == null) {
            aspects = new HashSet<>();
            aspectsForPointcut.put(key, aspects);
        }
        aspects.add(aspectAssertionAxiom);

        // TODO continue here... (store referenced entities)
        aspectAssertionAxiom.getPointcut();

    }

    @Override
    public void ontologiesChanged(@Nonnull List<? extends OWLOntologyChange> changes) throws OWLException {
        changes.forEach(change -> change.accept(this));
    }

    private OWLAxiom lastRemovedAxiom;
    private Set<OWLAspectAssertionAxiom> lastRemovedAspects;

    @Override
    public void visit(AddAxiom change) {
        OWLAxiom axiom = change.getAxiom();
        if (lastRemovedAxiom != null && axiom.equalsIgnoreAnnotations(lastRemovedAxiom)) {
            Optional.ofNullable(lastRemovedAspects).ifPresent(aspectAssertionAxioms -> aspectAssertionAxioms.stream().forEach(aspectAssertionAxiom -> addAspect(change.getOntology(), aspectAssertionAxiom)));
        }
        if (axiom instanceof OWLAspectAssertionAxiom) {
            OWLAspectAssertionAxiom aspectAssertionAxiom = (OWLAspectAssertionAxiom)axiom;
            addAspect(change.getOntology(), aspectAssertionAxiom);
        } else if (axiom instanceof OWLClassAxiom) {
        	// issue #11
        } else if (axiom instanceof OWLDeclarationAxiom) {
            // An entity has been renamed.
            // This concerns us if the entity is in the signature of an advice.
           OWLEntity renamedEntity = ((OWLDeclarationAxiom) axiom).getEntity();

        }
        lastRemovedAxiom = null;
    }

    @Override
    public void visit(RemoveAxiom change) {
        OWLAxiom axiom = change.getAxiom();
        lastRemovedAxiom = axiom; // store last removed axiom in case it is re-added with annotations (this is how adding an annotation works: remove axiom and re-add with annotations). Also needed in case of renaming (renaming = removing of declaration axiom + adding of new declaration axiom)
        if (axiom instanceof OWLAspectAssertionAxiom) {
            removeAspectAssertionAxiom(change.getOntology(), ((OWLAspectAssertionAxiom) axiom));
        } else {
            lastRemovedAspects = aspectsForPointcut.remove(new OntologyObjectTuple<>(change.getOntology(), new AspectOWLJoinPointAxiomPointcut(axiom.getAxiomWithoutAnnotations())));
        }
    }

    private class OntologyObjectTuple<O> {

        private OWLOntology ontology;
        private O object;

        public OntologyObjectTuple(@Nonnull OWLOntology ontology, @Nonnull O object) {
            this.ontology = ontology;
            this.object = object;
        }

        @Override
        public boolean equals(Object o) {
            if (this == o) return true;
            if (o == null || getClass() != o.getClass()) return false;
            OntologyObjectTuple<O> that = (OntologyObjectTuple<O>) o;
            return Objects.equals(ontology, that.ontology) &&
                    Objects.equals(object, that.object);
        }

        @Override
        public int hashCode() {
            return Objects.hash(ontology, object);
        }
    }
}
