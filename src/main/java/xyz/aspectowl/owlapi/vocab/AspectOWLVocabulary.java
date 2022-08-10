package xyz.aspectowl.owlapi.vocab;

import org.semanticweb.owlapi.model.HasIRI;
import org.semanticweb.owlapi.model.HasPrefixedName;
import org.semanticweb.owlapi.model.HasShortForm;
import org.semanticweb.owlapi.model.IRI;
import org.semanticweb.owlapi.vocab.Namespaces;
import org.semanticweb.owlapi.vocab.OWLRDFVocabulary;

import javax.annotation.Nonnull;
import java.util.HashSet;
import java.util.Set;

public enum AspectOWLVocabulary implements HasShortForm, HasIRI, HasPrefixedName {
    /** ASPECT                               */  ASPECT                               ("Aspect");

    @Nonnull
    private final IRI iri;
    @Nonnull
    private final String shortName;
    @Nonnull
    private final String prefixedName;

    AspectOWLVocabulary(@Nonnull String name) {
        iri = IRI.create(Namespaces.OWL.toString(), name);
        shortName = name;
        prefixedName = Namespaces.OWL.getPrefixName() + ':' + name;
    }

    @Override
    public IRI getIRI() {
        return iri;
    }

    @Override
    public String toString() {
        return iri.toString();
    }

    static final Set<IRI> BUILT_IN_IRIS;
    static {
        BUILT_IN_IRIS = new HashSet<>();
        for (OWLRDFVocabulary v : OWLRDFVocabulary.values()) {
            BUILT_IN_IRIS.add(v.getIRI());
        }
    }

    @Override
    public String getShortForm() {
        return shortName;
    }

    @Override
    public String getPrefixedName() {
        return prefixedName;
    }
}
