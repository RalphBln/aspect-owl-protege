/**
 * 
 */
package xyz.aspectowl.parser;

import xyz.aspectowl.owlapi.model.OWLAspectManager;
import org.semanticweb.owlapi.io.OWLParser;
import org.semanticweb.owlapi.io.OWLParserFactoryImpl;

/**
 * @author ralph
 */
public class AspectOWLFunctionalSyntaxParserFactory extends OWLParserFactoryImpl {

	/**
	 * 
	 */
	private static final long serialVersionUID = 2617848554167933720L;

	private OWLAspectManager am;

	/**
	 */
	public AspectOWLFunctionalSyntaxParserFactory(OWLAspectManager am) {
		super(new AspectOWLFunctionalSyntaxDocumentFormatFactory());
		this.am = am;
	}

	/* (non-Javadoc)
	 * @see org.semanticweb.owlapi.io.OWLParserFactory#createParser()
	 */
	@Override
	public OWLParser createParser() {
		return new AspectOWLFunctionalSyntaxOWLParser(am);
	}
	
}
