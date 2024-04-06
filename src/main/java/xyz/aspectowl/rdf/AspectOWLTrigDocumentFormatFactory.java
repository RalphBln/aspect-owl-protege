package xyz.aspectowl.rdf;

import org.semanticweb.owlapi.model.OWLDocumentFormat;
import org.semanticweb.owlapi.util.OWLDocumentFormatFactoryImpl;

/**
 * @author Ralph Schäfermeier
 */
public class AspectOWLTrigDocumentFormatFactory extends OWLDocumentFormatFactoryImpl {

    /**
     *
     */
    private static final long serialVersionUID = 5377353436199155322L;

    /**
     *
     */
    public AspectOWLTrigDocumentFormatFactory() {
        // TODO Auto-generated constructor stub
    }

    /* (non-Javadoc)
     * @see org.semanticweb.owlapi.model.OWLDocumentFormatFactory#createFormat()
     */
    @Override
    public OWLDocumentFormat createFormat() {
        return new AspectOWLTrigDocumentFormat();
    }

    /* (non-Javadoc)
     * @see org.semanticweb.owlapi.util.OWLDocumentFormatFactoryImpl#getKey()
     */
    @Override
    public String getKey() {
        return "Trig format with Aspect-Oriented Extensions";
    }

}