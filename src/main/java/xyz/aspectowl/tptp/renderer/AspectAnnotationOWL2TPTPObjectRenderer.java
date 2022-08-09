package xyz.aspectowl.tptp.renderer;

import net.sf.tweety.logics.fol.syntax.Equivalence;
import net.sf.tweety.logics.fol.syntax.FolFormula;
import org.semanticweb.owlapi.OWLAPIConfigProvider;
import org.semanticweb.owlapi.manchestersyntax.parser.ManchesterOWLSyntaxParserImpl;
import org.semanticweb.owlapi.model.IRI;
import org.semanticweb.owlapi.model.OWLAxiom;
import org.semanticweb.owlapi.model.OWLClassExpression;
import org.semanticweb.owlapi.model.OWLOntology;

import java.io.Writer;
import java.util.ArrayList;
import java.util.stream.Stream;

/**
 * This class renders aspect-oriented OWL ontologies with annotations (unlike AspectOWL) as FOL belief sets serialized
 * in TPTP syntax.
 * Shortcomings of the annotation approach are that
 * 1. annotation subjects cannot be class expressions, so we had to invent a makeshift mechanism: class expressions must
 *    be denoted in Manchester syntax and added as String literal annotations.
 * 2. Annotations on axioms cannot be nested. Therefore, the annotation approach limits the aspect nesting level to 1.
 * @author ralph
 */
public class AspectAnnotationOWL2TPTPObjectRenderer extends AspectOWL2TPTPObjectRenderer {

    private static final IRI aspectAnnotationProperty = IRI.create("http://ontology.aspectowl.xyz#hasAspect");

    private final ManchesterOWLSyntaxParserImpl manchesterSyntaxParser;

    public AspectAnnotationOWL2TPTPObjectRenderer(OWLOntology ontology, Writer writer) {
        super(ontology, writer);
        manchesterSyntaxParser = new ManchesterOWLSyntaxParserImpl(new OWLAPIConfigProvider(), ontology.getOWLOntologyManager().getOWLDataFactory());
        manchesterSyntaxParser.getPrefixManager().setDefaultPrefix("");
        manchesterSyntaxParser.getPrefixManager().setPrefix("", ontology.getOntologyID().getOntologyIRI().get().toString() + "#");
        manchesterSyntaxParser.setDefaultOntology(ontology);
    }


    @Override
    public boolean hasAspect(OWLAxiom axiom) {
        return axiom.isAnnotated() && axiom.getAnnotations().stream().filter(a -> a.getProperty().getIRI().equals(aspectAnnotationProperty)).findAny().map(c -> true).get();
    }

    @Override
    public Stream<FolFormula> handleAspects(OWLAxiom axiom, Stream<FolFormula> nonAspectFormulae) {
        if (hasAspect(axiom)) {
            var result = new ArrayList<FolFormula>();
            axiom.getAnnotations().stream()
                    .filter(a -> a.getProperty().getIRI().equals(aspectAnnotationProperty))
                    .filter(a -> a.annotationValue().isLiteral())
                    .forEach(a -> {
                        var literal = a.getValue().asLiteral();
                        manchesterSyntaxParser.setStringToParse(literal.get().getLiteral());
                        OWLClassExpression aspect = manchesterSyntaxParser.parseClassExpression();
                        makeFormula(String.format("forall A: (%s(A))", translate(aspect))).forEach(aspectEquivalencePart -> nonAspectFormulae.forEach(joinpointAxiomEquivalencePart -> result.add(new Equivalence(aspectEquivalencePart, joinpointAxiomEquivalencePart))));
                    });

            return result.stream();
        }
        return nonAspectFormulae;
    }
}
