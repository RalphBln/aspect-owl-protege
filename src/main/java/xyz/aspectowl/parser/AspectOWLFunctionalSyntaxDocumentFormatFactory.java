/**
 * 
 */
package xyz.aspectowl.parser;

import org.semanticweb.owlapi.model.OWLDocumentFormat;
import org.semanticweb.owlapi.util.OWLDocumentFormatFactoryImpl;

/**
 * @author ralph
 */
public class AspectOWLFunctionalSyntaxDocumentFormatFactory extends OWLDocumentFormatFactoryImpl {

	/**
	 * 
	 */
	private static final long serialVersionUID = 5377353436199155322L;

	/**
	 * 
	 */
	public AspectOWLFunctionalSyntaxDocumentFormatFactory() {
		// TODO Auto-generated constructor stub
	}

	/* (non-Javadoc)
	 * @see org.semanticweb.owlapi.model.OWLDocumentFormatFactory#createFormat()
	 */
	@Override
	public OWLDocumentFormat createFormat() {
		return new AspectOWLFunctionalSyntaxDocumentFormat();
	}
	
	/* (non-Javadoc)
	 * @see org.semanticweb.owlapi.util.OWLDocumentFormatFactoryImpl#getKey()
	 */
	@Override
	public String getKey() {
		return "OWL Functional Syntax with Aspect-Oriented Extensions";
	}

}
