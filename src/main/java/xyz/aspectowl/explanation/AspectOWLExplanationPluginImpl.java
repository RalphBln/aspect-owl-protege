package xyz.aspectowl.explanation;

import xyz.aspectowl.owlapi.model.OWLAspectManager;
import xyz.aspectowl.protege.AspectOWLEditorKitHook;
import org.protege.editor.owl.OWLEditorKit;
import org.protege.editor.owl.model.OWLModelManager;
import org.protege.editor.owl.ui.explanation.io.InconsistentOntologyPluginInstance;
import org.semanticweb.owlapi.model.OWLDataFactory;
import org.semanticweb.owlapi.model.OWLOntology;
import org.semanticweb.owlapi.model.OWLSubClassOfAxiom;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.swing.*;
import java.awt.*;
import java.awt.event.ComponentAdapter;
import java.awt.event.ComponentEvent;

public class AspectOWLExplanationPluginImpl implements InconsistentOntologyPluginInstance {

    private static final Logger log = LoggerFactory.getLogger(AspectOWLExplanationPluginImpl.class);

    private OWLEditorKit editorKit;

    private OWLAspectManager aspectManager;

    @Override
    public void setup(OWLEditorKit editorKit) {
        this.editorKit = editorKit;
        this.aspectManager = AspectOWLEditorKitHook.getAspectManager(editorKit);
    }

    @Override
    public void explain(OWLOntology ontology) {
        OWLModelManager owlModelManager = editorKit.getOWLModelManager();
        OWLDataFactory df = owlModelManager.getOWLDataFactory();
        OWLSubClassOfAxiom entailment = df.getOWLSubClassOfAxiom(df.getOWLThing(), df.getOWLNothing());
        final InconsistentAspectsPanel panel = new InconsistentAspectsPanel(editorKit, entailment);

        JOptionPane op = new JOptionPane(panel, JOptionPane.PLAIN_MESSAGE, JOptionPane.DEFAULT_OPTION);

        StringBuilder buf = new StringBuilder();
        aspectManager.getAllAspects(editorKit.getOWLModelManager().getActiveOntologies()).forEach(aspect -> buf.append(String.format("%s%n", aspect.getIRI().getShortForm())));

        JDialog dlg = op.createDialog("There are some inconsistencies, but there are aspects. Let's see if the contradicting axioms have different aspects:");

        dlg.add(new JTextArea(buf.toString()), BorderLayout.CENTER);



        dlg.addComponentListener(new ComponentAdapter() {
            @Override
            public void componentHidden(ComponentEvent e) {
                try {
                    panel.dispose();
                } catch (Exception e1) {
                    log.error("Error while disposing of Asoect Explanation Plugin.", e1);
                }
                dlg.dispose();
            }
        });
        dlg.setModal(false);
        dlg.setResizable(true);
        dlg.setVisible(true);
    }

    @Override
    public void initialise() throws Exception {

    }

    @Override
    public void dispose() throws Exception {

    }
}
