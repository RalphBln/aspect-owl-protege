package de.fuberlin.csw.aspectowl.owlapi.model.visitor;

import org.semanticweb.owlapi.model.OWLAxiomVisitorEx;
import org.semanticweb.owlapi.model.OWLObject;

/**
 * @author ralph
 */
public class MissingAspectVisitorMappingException extends RuntimeException {

    public <O> MissingAspectVisitorMappingException(OWLObject object, OWLAxiomVisitorEx visitor) {
        super(String.format("No mapped aspect visitor has been provided for visitor %s and object %s", visitor, object));
    }
}
