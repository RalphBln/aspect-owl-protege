package xyz.aspectowl.rdf;

import org.semanticweb.owlapi.formats.TrigDocumentFormat;

/**
 * @author ralph
 */
public class AspectOWLTrigDocumentFormat extends TrigDocumentFormat {

    /**
     * @see org.semanticweb.owlapi.formats.PrefixDocumentFormatImpl#getKey()
     */
    @Override
    public String getKey() {
        return "AspectOWL Trig Format";
    }

}