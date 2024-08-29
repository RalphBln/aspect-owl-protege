package xyz.aspectowl.rdf.renderer;

import org.semanticweb.owlapi.model.OWLStorer;
import xyz.aspectowl.owlapi.model.OWLAspectManager;
import xyz.aspectowl.rdf.AspectOWLTrigDocumentFormatFactory;
import xyz.aspectowl.renderer.AspectOWLStorerFactoryImpl;

import javax.annotation.Nonnull;

/**
 * @author ralph
 */
public class AspectOWLTrigStorerFactory extends AspectOWLStorerFactoryImpl {


    public AspectOWLTrigStorerFactory(OWLAspectManager am) {
        super(new AspectOWLTrigDocumentFormatFactory(), am);
    }

    @Nonnull
    @Override
    public OWLStorer createStorer() {
        return new AspectOWLTrigStorer(getFormatFactory(), getAspectManager());
    }}
