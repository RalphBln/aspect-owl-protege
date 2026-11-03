package xyz.aspectowl.tptp.renderer;

import org.semanticweb.owlapi.OWLAPIConfigProvider;
import org.semanticweb.owlapi.expression.ShortFormEntityChecker;
import org.semanticweb.owlapi.manchestersyntax.parser.ManchesterOWLSyntaxParserImpl;
import org.semanticweb.owlapi.model.*;
import org.semanticweb.owlapi.model.parameters.Imports;
import org.semanticweb.owlapi.util.BidirectionalShortFormProviderAdapter;
import org.semanticweb.owlapi.util.SimpleShortFormProvider;

import java.io.Writer;
import java.util.List;

/**
 * Renders hasAspect annotations as contexts. Values are either class IRIs or
 * Manchester class expressions. Nested hasAspect annotations qualify the context.
 */
public class AspectAnnotationOWL2TPTPObjectRenderer extends AspectOWL2TPTPObjectRenderer {

    private static final IRI ASPECT_PROPERTY = IRI.create("http://ontology.aspectowl.xyz#hasAspect");
    private final ManchesterOWLSyntaxParserImpl manchesterSyntaxParser;

    public AspectAnnotationOWL2TPTPObjectRenderer(OWLOntology ontology, Writer writer, Imports includeImportsClosure) {
        super(ontology, writer, includeImportsClosure);
        manchesterSyntaxParser = new ManchesterOWLSyntaxParserImpl(new OWLAPIConfigProvider(),
                ontology.getOWLOntologyManager().getOWLDataFactory());
        manchesterSyntaxParser.setDefaultOntology(ontology);
        manchesterSyntaxParser.setOWLEntityChecker(new ShortFormEntityChecker(
                new BidirectionalShortFormProviderAdapter(ontology.getImportsClosure(), new SimpleShortFormProvider())));
    }

    @Override
    protected List<AspectContext> getAspectContexts(OWLAxiom axiom) {
        return contexts(axiom.getAnnotations());
    }

    private List<AspectContext> contexts(java.util.Set<OWLAnnotation> annotations) {
        return annotations.stream().filter(a -> a.getProperty().getIRI().equals(ASPECT_PROPERTY))
                .map(annotation -> new AspectContext(parseAspect(annotation.getValue()),
                        contexts(annotation.getAnnotations()))).toList();
    }

    private OWLClassExpression parseAspect(OWLAnnotationValue value) {
        if (value instanceof IRI)
            return onto.orElseThrow().getOWLOntologyManager().getOWLDataFactory().getOWLClass((IRI) value);
        if (value instanceof OWLLiteral literal) {
            manchesterSyntaxParser.setStringToParse(literal.getLiteral());
            return manchesterSyntaxParser.parseClassExpression();
        }
        throw new IllegalArgumentException("Aspect annotations require a class IRI or Manchester expression: " + value);
    }
}
