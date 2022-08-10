/**
 * 
 */
package xyz.aspectowl.util.test;

import java.util.Comparator;

import org.semanticweb.owlapi.model.OWLAxiom;

/**
 * @author ralph
 */
public class OWLAxiomComparator implements Comparator<OWLAxiom> {

	/* (non-Javadoc)
	 * @see java.util.Comparator#compare(java.lang.Object, java.lang.Object)
	 */
	@Override
	public int compare(OWLAxiom o1, OWLAxiom o2) {
		return o1.toString().compareTo(o2.toString());
	}

}
