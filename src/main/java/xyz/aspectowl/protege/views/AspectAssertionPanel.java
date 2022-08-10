package xyz.aspectowl.protege.views;

import xyz.aspectowl.owlapi.model.impl.AspectOWLAxiomInstance;
import org.protege.editor.owl.OWLEditorKit;
import org.protege.editor.owl.model.OWLEditorKitIRIShortFormProvider;
import org.protege.editor.owl.model.OWLEditorKitOntologyShortFormProvider;
import org.protege.editor.owl.model.OWLEditorKitShortFormProvider;
import org.protege.editor.owl.ui.renderer.context.OWLObjectRenderingContext;
import org.protege.editor.owl.ui.renderer.styledstring.OWLObjectStyledStringRenderer;
import org.protege.editor.owl.ui.renderer.styledstring.StyledString;
import org.protege.editor.owl.ui.renderer.styledstring.StyledStringPanel;
import org.semanticweb.owlapi.model.OWLAxiom;
import org.semanticweb.owlapi.util.SimpleIRIShortFormProvider;

import javax.swing.*;
import java.awt.*;

import static com.google.common.base.Preconditions.checkNotNull;

public class AspectAssertionPanel extends JComponent {

	private static final long serialVersionUID = -5974200966389671425L;

	private static final int PREF_WIDTH = 500;

    private AspectAssertionsList aspectAssertionComponent;

    private final OWLEditorKit editorKit;

    private final StyledStringPanel axiomPanel = new StyledStringPanel();

    public AspectAssertionPanel(OWLEditorKit eKit) {
        this.editorKit = checkNotNull(eKit);
        setLayout(new BorderLayout(6, 6));
        setPreferredSize(new Dimension(PREF_WIDTH, 300));

        aspectAssertionComponent = new AspectAssertionsList(eKit);

        axiomPanel.setPreferredSize(new Dimension(PREF_WIDTH, 50));
        add(axiomPanel, BorderLayout.NORTH);
        add(new JScrollPane(aspectAssertionComponent), BorderLayout.CENTER);

        setVisible(true);
    }

    private StyledString getAxiomRendering(OWLAxiom axiom) {
        OWLEditorKitShortFormProvider sfp = new OWLEditorKitShortFormProvider(editorKit);
        OWLEditorKitOntologyShortFormProvider ontologySfp = new OWLEditorKitOntologyShortFormProvider(editorKit);
        OWLEditorKitIRIShortFormProvider iriSfp = new OWLEditorKitIRIShortFormProvider(editorKit, new SimpleIRIShortFormProvider());
        OWLObjectRenderingContext renderingContext = new OWLObjectRenderingContext(sfp, iriSfp, ontologySfp);
        OWLObjectStyledStringRenderer renderer = new OWLObjectStyledStringRenderer(renderingContext);
        return renderer.getRendering(axiom);
    }


    public void setAxiom(AspectOWLAxiomInstance axiom) {
        axiomPanel.setStyledString(StyledString.EMPTY_STYLED_STRING);
        if (axiom != null) {
            StyledString axiomRendering = getAxiomRendering(axiom.getAxiom());
            axiomPanel.setStyledString(axiomRendering);
            aspectAssertionComponent.setRootObject(axiom);
        }
        else {
            aspectAssertionComponent.setRootObject(null);
        }
    }


    public AspectOWLAxiomInstance getAxiom() {
        return aspectAssertionComponent.getRoot();
    }


    public void dispose() {
        aspectAssertionComponent.dispose();
    }

}
