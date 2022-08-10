package xyz.aspectowl.renderer;

import xyz.aspectowl.owlapi.model.OWLAspectManager;
import xyz.aspectowl.parser.AspectOWLFunctionalSyntaxDocumentFormatFactory;
import org.semanticweb.owlapi.model.OWLStorer;
import org.semanticweb.owlapi.util.OWLStorerFactoryImpl;

import javax.annotation.Nonnull;

public class AspectOWLFunctionalSyntaxStorerFactory extends OWLStorerFactoryImpl {

    private static final long serialVersionUID = 4225557529363900763L;

    private OWLAspectManager am;

    public AspectOWLFunctionalSyntaxStorerFactory(OWLAspectManager am) {
        super(new AspectOWLFunctionalSyntaxDocumentFormatFactory());
        this.am = am;
    }

    @Nonnull
    @Override
    public OWLStorer createStorer() {
        return new AspectOWLFunctionalSyntaxStorer(am);
    }
}
