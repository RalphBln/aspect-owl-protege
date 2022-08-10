package xyz.aspectowl.protege.editor.core.ui.renderer;

import xyz.aspectowl.owlapi.model.OWLAspectManager;
import xyz.aspectowl.protege.editor.core.ui.OWLAspectIcon;
import org.protege.editor.owl.model.OWLModelManager;
import org.protege.editor.owl.ui.renderer.OWLClassIcon;
import org.protege.editor.owl.ui.renderer.OWLIconProviderImpl;
import org.protege.editor.owl.ui.renderer.context.DefinedClassChecker;
import org.semanticweb.owlapi.model.*;
import org.semanticweb.owlapi.search.EntitySearcher;

import javax.annotation.Nonnull;
import javax.swing.*;
import java.util.Set;

public class AspectOWLIconProviderImpl extends OWLIconProviderImpl {

    private OWLModelManager mm;
    private OWLAspectManager am;

    private static final Icon primitiveAspectClassIcon = new OWLAspectIcon(OWLClassIcon.Type.PRIMITIVE);
    private static final Icon definedAspectClassIcon = new OWLAspectIcon(OWLClassIcon.Type.DEFINED);

    public AspectOWLIconProviderImpl(@Nonnull DefinedClassChecker definedClassChecker, OWLAspectManager am, OWLModelManager mm) {
        super(definedClassChecker);
        this.am = am;
        this.mm = mm;
    }

    @Override
    public Icon getIcon(OWLObject owlObject) {
        if (owlObject instanceof OWLClassExpression) {
            OWLClassExpression clsExpr = (OWLClassExpression) owlObject;
            if (am.isAspectInOntology(clsExpr, mm.getActiveOntologies())) {
                if (clsExpr instanceof OWLClass) {
                    for (OWLOntology ont : mm.getActiveOntologies()) {
                        if (isDefined((OWLClass)clsExpr, ont)) {
                            return definedAspectClassIcon;
                        }
                    }
                }
                return primitiveAspectClassIcon;
            }
        }
        return super.getIcon(owlObject);
    }

    private static boolean isDefined(OWLClass owlClass, OWLOntology ontology) {
        if (EntitySearcher.isDefined(owlClass, ontology)) {
            return true;
        }
        Set<OWLDisjointUnionAxiom> axioms = ontology.getDisjointUnionAxioms(owlClass);
        return !axioms.isEmpty();
    }

}
