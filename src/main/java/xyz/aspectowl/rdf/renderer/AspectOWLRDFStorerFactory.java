package xyz.aspectowl.rdf.renderer;

import org.semanticweb.owlapi.model.OWLDocumentFormatFactory;
import org.semanticweb.owlapi.model.OWLStorer;
import org.semanticweb.owlapi.util.OWLStorerFactoryImpl;
import xyz.aspectowl.owlapi.model.OWLAspectManager;
import xyz.aspectowl.parser.AspectOWLFunctionalSyntaxDocumentFormatFactory;
import xyz.aspectowl.rdf.AspectOWLRDFDocumentFormatFactory;
import xyz.aspectowl.renderer.AspectOWLFunctionalSyntaxStorer;
import xyz.aspectowl.renderer.AspectOWLStorerFactoryImpl;

import javax.annotation.Nonnull;

/**
 * @author ralph
 */
public class AspectOWLRDFStorerFactory extends AspectOWLStorerFactoryImpl {


    public AspectOWLRDFStorerFactory(OWLAspectManager am) {
        super(new AspectOWLRDFDocumentFormatFactory(), am);
    }

    @Nonnull
    @Override
    public OWLStorer createStorer() {
        return new AspectOWLRDFStorer(getAspectManager());
    }}
