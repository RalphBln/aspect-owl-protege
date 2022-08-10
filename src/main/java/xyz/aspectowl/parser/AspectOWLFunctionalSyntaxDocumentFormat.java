/**
 * 
 */
package xyz.aspectowl.parser;

import org.semanticweb.owlapi.formats.PrefixDocumentFormatImpl;

/**
 * @author ralph
 */
public class AspectOWLFunctionalSyntaxDocumentFormat extends PrefixDocumentFormatImpl {

	/**
	 * 
	 */
	private static final long serialVersionUID = -2753197048778914910L;

	/**
	 * @see org.semanticweb.owlapi.formats.PrefixDocumentFormatImpl#getKey()
	 */
	@Override
	public String getKey() {
		return "OWL Functional Syntax with Aspect-Oriented Extensions";
	}
	
}
