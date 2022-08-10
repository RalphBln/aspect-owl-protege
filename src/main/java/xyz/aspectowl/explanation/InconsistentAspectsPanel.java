package xyz.aspectowl.explanation;

import org.protege.editor.core.Disposable;
import org.protege.editor.owl.OWLEditorKit;
import org.protege.editor.owl.model.event.OWLModelManagerChangeEvent;
import org.protege.editor.owl.model.event.OWLModelManagerListener;
import org.semanticweb.owlapi.model.OWLAxiom;
import uk.ac.manchester.cs.owl.explanation.*;

import javax.swing.*;
import java.util.Set;

/**
 * @author Ralph Schaefermeier
 */
public class InconsistentAspectsPanel extends JPanel implements Disposable, OWLModelManagerListener, EntailmentSelectionListener, AxiomSelectionModel, ExplanationManagerListener {

    public InconsistentAspectsPanel(OWLEditorKit editorKit, OWLAxiom entailment) {

    }

    @Override
    public void dispose() throws Exception {

    }

    @Override
    public void handleChange(OWLModelManagerChangeEvent event) {

    }

    @Override
    public void addAxiomSelectionListener(AxiomSelectionListener axiomSelectionListener) {

    }

    @Override
    public void removeAxiomSelectionListener(AxiomSelectionListener axiomSelectionListener) {

    }

    @Override
    public void setAxiomSelected(OWLAxiom owlAxiom, boolean b) {

    }

    @Override
    public Set<OWLAxiom> getSelectedAxioms() {
        return null;
    }

    @Override
    public void selectionChanged() {

    }

    @Override
    public void explanationLimitChanged(JustificationManager justificationManager) {

    }

    @Override
    public void explanationsComputed(OWLAxiom owlAxiom) {

    }
}
