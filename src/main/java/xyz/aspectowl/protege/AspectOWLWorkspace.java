package xyz.aspectowl.protege;

import xyz.aspectowl.owlapi.model.OWLAspectManager;
import xyz.aspectowl.protege.editor.core.ui.renderer.AspectOWLIconProviderImpl;
import org.protege.editor.owl.model.OWLModelManager;
import org.protege.editor.owl.model.OWLWorkspace;
import org.protege.editor.owl.ui.renderer.OWLIconProvider;
import org.semanticweb.owlapi.model.OWLOntology;
import org.semanticweb.owlapi.search.EntitySearcher;

/**
 * @author ralph
 */
public class AspectOWLWorkspace extends OWLWorkspace {

    private final OWLIconProvider iconProvider;

    public AspectOWLWorkspace(OWLAspectManager aspectManager, OWLModelManager modelManager) {
        iconProvider = new AspectOWLIconProviderImpl(
                cls -> {
                    for(OWLOntology ontology : getOWLModelManager().getActiveOntologies()) {
                        if(EntitySearcher.isDefined(cls, ontology)) {
                            return true;
                        }
                        if(!ontology.getDisjointUnionAxioms(cls).isEmpty()) {
                            return true;
                        }
                    }
                    return false;
                }
        , aspectManager, modelManager);
    }

    @Override
    public OWLIconProvider getOWLIconProvider() {
        return iconProvider;
    }
}
