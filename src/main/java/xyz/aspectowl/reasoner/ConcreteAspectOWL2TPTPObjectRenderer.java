package xyz.aspectowl.reasoner;

import xyz.aspectowl.owlapi.model.OWLAspectManager;
import net.sf.tweety.logics.fol.syntax.Equivalence;
import net.sf.tweety.logics.fol.syntax.FolFormula;
import org.semanticweb.owlapi.model.OWLAxiom;
import org.semanticweb.owlapi.model.OWLOntology;
import xyz.aspectowl.tptp.renderer.AspectOWL2TPTPObjectRenderer;

import java.io.Writer;
import java.util.ArrayList;
import java.util.stream.Stream;

/**
    @author ralph
*/
public class ConcreteAspectOWL2TPTPObjectRenderer extends AspectOWL2TPTPObjectRenderer {

    private OWLAspectManager am;
    private OWLOntology ontology;

    public ConcreteAspectOWL2TPTPObjectRenderer(OWLAspectManager am, OWLOntology ontology, Writer writer) {
        super(ontology, writer);
        this.am = am;
        this.ontology = ontology;
    }

    @Override
    public boolean hasAspect(OWLAxiom axiom) {
        return am.hasAssertedAspects(ontology, axiom);
    }

    @Override
    public Stream<FolFormula> handleAspects(OWLAxiom axiom, Stream<FolFormula> nonAspectFormulae) {
        if (hasAspect(axiom)) {
            var result = new ArrayList<FolFormula>();
            am.getAssertedAspects(ontology, axiom).forEach(aspect -> makeFormula(String.format("forall A: (%s(A))", translate(aspect))).forEach(aspectEquivalencePart -> nonAspectFormulae.forEach(joinpointAxiomEquivalencePart -> result.add(new Equivalence(aspectEquivalencePart, joinpointAxiomEquivalencePart)))));
            return result.stream();
        }
        return nonAspectFormulae;
    }
}
