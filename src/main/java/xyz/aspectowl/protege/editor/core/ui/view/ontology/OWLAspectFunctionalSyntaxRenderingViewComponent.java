package xyz.aspectowl.protege.editor.core.ui.view.ontology;

import xyz.aspectowl.protege.AspectOWLEditorKitHook;
import xyz.aspectowl.renderer.AspectOWLFunctionalSyntaxRenderer;
import org.protege.editor.owl.ui.view.ontology.AbstractOntologyRenderingViewComponent;
import org.semanticweb.owlapi.model.OWLOntology;

import java.io.Writer;

public class OWLAspectFunctionalSyntaxRenderingViewComponent extends AbstractOntologyRenderingViewComponent {

	private static final long serialVersionUID = 1898383172918948099L;

	@Override
    protected void renderOntology(OWLOntology ontology, Writer writer) throws Exception {
        AspectOWLFunctionalSyntaxRenderer ren = new AspectOWLFunctionalSyntaxRenderer(AspectOWLEditorKitHook.getAspectManager(getOWLEditorKit()));
        ren.render(ontology, writer);
    }
}
