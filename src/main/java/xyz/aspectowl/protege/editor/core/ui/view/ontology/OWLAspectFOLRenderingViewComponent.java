package xyz.aspectowl.protege.editor.core.ui.view.ontology;

import org.protege.editor.owl.ui.view.ontology.AbstractOntologyRenderingViewComponent;
import org.semanticweb.owlapi.model.OWLOntology;
import org.semanticweb.owlapi.model.parameters.Imports;
import xyz.aspectowl.protege.AspectOWLEditorKitHook;
import xyz.aspectowl.reasoner.ConcreteAspectOWL2TPTPObjectRenderer;

import java.io.Serial;
import java.io.Writer;

public class OWLAspectFOLRenderingViewComponent extends AbstractOntologyRenderingViewComponent {

    @Serial
	private static final long serialVersionUID = 3884046512458091750L;

	@Override
    protected void renderOntology(OWLOntology ontology, Writer writer) throws Exception {
        ConcreteAspectOWL2TPTPObjectRenderer ren = new ConcreteAspectOWL2TPTPObjectRenderer(AspectOWLEditorKitHook.getAspectManager(ontology.getOWLOntologyManager()), ontology, writer, Imports.INCLUDED);
        ren.setHumanReadableTptpNames(true);
        ren.visit(ontology);
        writer.write(ren.getBeliefSet().toString());
    }
}
