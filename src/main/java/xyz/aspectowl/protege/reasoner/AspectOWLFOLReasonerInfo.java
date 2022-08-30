package xyz.aspectowl.protege.reasoner;

import org.protege.editor.owl.model.inference.AbstractProtegeOWLReasonerInfo;
import org.semanticweb.owlapi.reasoner.BufferingMode;
import org.semanticweb.owlapi.reasoner.OWLReasonerFactory;
import xyz.aspectowl.reasoner.AspectOWLFOLReasonerFactory;

/**
 * @author ralph
 */
public class AspectOWLFOLReasonerInfo extends AbstractProtegeOWLReasonerInfo {

    @Override
    public BufferingMode getRecommendedBuffering() {
        return BufferingMode.NON_BUFFERING;
    }

    @Override
    public OWLReasonerFactory getReasonerFactory() {
        return new AspectOWLFOLReasonerFactory();
    }
}
