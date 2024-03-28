package xyz.aspectowl.rdf;

import org.semanticweb.owlapi.formats.PrefixDocumentFormatImpl;

/**
 * @author ralph
 */
public class AspectOWLTrigDocumentFormat extends PrefixDocumentFormatImpl {

    /**
     * @see org.semanticweb.owlapi.formats.PrefixDocumentFormatImpl#getKey()
     */
    @Override
    public String getKey() {
        return "RDF Syntax with Aspect-Oriented Extensions";
    }

}