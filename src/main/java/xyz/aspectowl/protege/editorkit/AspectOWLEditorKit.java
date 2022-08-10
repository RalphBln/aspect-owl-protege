package xyz.aspectowl.protege.editorkit;

import org.protege.editor.owl.OWLEditorKit;
import org.protege.editor.owl.model.OWLModelManager;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import xyz.aspectowl.owlapi.model.impl.AspectOWLEntityFactory;

/**
 * Created by ralph on 22.05.17.
 */
public class AspectOWLEditorKit extends OWLEditorKit {

    private static final Logger logger = LoggerFactory.getLogger(AspectOWLEditorKit.class);

    public AspectOWLEditorKit(AspectOWLEditorKitFactory factory) {
        super(factory);
        logger.info("Initializing editor kit for aspect-oriented ontology development plugin.");

        OWLModelManager modelManager = getModelManager();
        modelManager.setOWLEntityFactory(new AspectOWLEntityFactory(modelManager));
    }

}
