/**
 * 
 */
package xyz.aspectowl.parser;

import org.protege.editor.owl.model.io.IOListener;
import org.protege.editor.owl.model.io.IOListenerEvent;
import org.semanticweb.owlapi.model.OWLOntology;
import org.semanticweb.owlapi.model.OWLOntologyManager;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * @author ralph
 */
public class AspectOWLOntologyPreSaveChecker extends IOListener {
	
	private static final Logger log = LoggerFactory.getLogger(AspectOWLOntologyPreSaveChecker.class);

	private OWLOntologyManager om;
	
	/**
	 * 
	 */
	public AspectOWLOntologyPreSaveChecker(OWLOntologyManager om) {
		this.om = om;
	}
	
	/**
	 * Checks whether there are aspects in the current ontology and invokes the
	 * translation to aspect syntax.
	 * 
	 * This gets called BEFORE the format selection dialog is displays, which
	 * is a bit unfortunate, since we only need this uf saving in OFN format.
	 * 
	 * Then again, we could ask the user if he/she wants to save in the aofn
	 * format if aspects are present.
	 */
	@Override
	public void beforeSave(IOListenerEvent event) {
		log.info("About to save ontology " + event.getOntologyID());
		
		OWLOntology ontologyToSave = om.getOntology(event.getOntologyID());
		
		// TODO search ontology for occurence of aspects and intercept normal saving
		// github issue #5 z
	}

	@Override
	public void afterSave(IOListenerEvent event) {
		// do nothing
	}

	@Override
	public void beforeLoad(IOListenerEvent event) {
		// do nothing
	}

	@Override
	public void afterLoad(IOListenerEvent event) {
		// do nothing
	}

}
