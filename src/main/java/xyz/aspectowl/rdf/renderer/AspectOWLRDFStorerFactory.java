package xyz.aspectowl.rdf.renderer;

import org.semanticweb.owlapi.model.OWLDocumentFormatFactory;
import org.semanticweb.owlapi.model.OWLStorer;
import org.semanticweb.owlapi.util.OWLStorerFactoryImpl;

import javax.annotation.Nonnull;

/**
 * @author ralph
 */
public class AspectOWLRDFStorerFactory extends OWLStorerFactoryImpl {


    protected AspectOWLRDFStorerFactory(@Nonnull OWLDocumentFormatFactory format) {
        super(format);
    }

    @Nonnull
    @Override
    public OWLStorer createStorer() {
        return null;
    }
}
