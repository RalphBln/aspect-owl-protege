package xyz.aspectowl.rdf;

import org.semanticweb.owlapi.model.OWLDocumentFormat;
import org.semanticweb.owlapi.model.OWLDocumentFormatFactory;
import org.semanticweb.owlapi.util.OWLDocumentFormatFactoryImpl;
import xyz.aspectowl.parser.AspectOWLFunctionalSyntaxDocumentFormat;

/**
 * @author Ralph Schäfermeier
 */
public class AspectOWLRDFDocumentFormatFactory extends OWLDocumentFormatFactoryImpl {

    /**
     *
     */
    private static final long serialVersionUID = 5377353436199155322L;

    /**
     *
     */
    public AspectOWLRDFDocumentFormatFactory() {
        // TODO Auto-generated constructor stub
    }

    /* (non-Javadoc)
     * @see org.semanticweb.owlapi.model.OWLDocumentFormatFactory#createFormat()
     */
    @Override
    public OWLDocumentFormat createFormat() {
        return new AspectOWLRDFDocumentFormat();
    }

    /* (non-Javadoc)
     * @see org.semanticweb.owlapi.util.OWLDocumentFormatFactoryImpl#getKey()
     */
    @Override
    public String getKey() {
        return "RDF format with Aspect-Oriented Extensions";
    }

}