package xyz.aspectowl.owlapi.model.impl;

import org.semanticweb.owlapi.model.OWLDataFactory;
import org.semanticweb.owlapi.model.PriorityCollectionSorting;
import uk.ac.manchester.cs.owl.owlapi.OWLOntologyManagerImpl;

import javax.annotation.Nonnull;
import java.util.concurrent.locks.ReadWriteLock;

/**
 * Created by ralph on 25.05.17.
 */
public class AspectOWLOntologyManagerImpl extends OWLOntologyManagerImpl {
    public AspectOWLOntologyManagerImpl(@Nonnull OWLDataFactory dataFactory, ReadWriteLock readWriteLock) {
        super(dataFactory, readWriteLock);
    }

    public AspectOWLOntologyManagerImpl(@Nonnull OWLDataFactory dataFactory, ReadWriteLock readWriteLock, PriorityCollectionSorting sorting) {
        super(dataFactory, readWriteLock, sorting);
    }
}
