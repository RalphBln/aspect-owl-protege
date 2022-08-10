package xyz.aspectowl.protege.views;

import xyz.aspectowl.owlapi.model.AspectOWLJoinPointAxiomPointcut;
import xyz.aspectowl.owlapi.model.OWLAspect;
import xyz.aspectowl.owlapi.model.OWLAspectAssertionAxiom;
import xyz.aspectowl.owlapi.model.OWLAspectManager;
import xyz.aspectowl.owlapi.model.impl.AspectOWLAxiomInstance;
import xyz.aspectowl.owlapi.protege.AspectOWLEditorKitHook;
import xyz.aspectowl.protege.editor.core.ui.AspectButton;

import org.protege.editor.core.ui.list.MList;
import org.protege.editor.core.ui.list.MListButton;
import org.protege.editor.core.ui.list.MListItem;
import org.protege.editor.core.ui.list.MListSectionHeader;
import org.protege.editor.owl.OWLEditorKit;
import org.protege.editor.owl.ui.OWLClassExpressionComparator;
import org.protege.editor.owl.ui.UIHelper;
import org.protege.editor.owl.ui.editor.OWLClassDescriptionEditor;
import org.protege.editor.owl.ui.renderer.OWLCellRenderer;
import org.semanticweb.owlapi.model.*;

import javax.swing.*;
import java.awt.*;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;
import java.util.*;
import java.util.List;

/**
 * @author ralph
 */
public class AspectAssertionsList extends MList {

	private static final long serialVersionUID = -8412473069634410579L;

	
	private final OWLAspectManager aspectManager;

    private static final String HEADER_TEXT = "Aspects";

    private OWLEditorKit editorKit;

    private OWLClassDescriptionEditor editor;

    private AspectOWLAxiomInstance root;

    private OWLAxiom newAxiom;



    private MListSectionHeader header = new MListSectionHeader() {

        public String getName() {
            return HEADER_TEXT;
        }

        public boolean canAdd() {
            return true;
        }
    };

    private OWLOntologyChangeListener ontChangeListener = changes -> handleOntologyChanges(changes);

    private MouseListener mouseListener = new MouseAdapter(){
        public void mouseReleased(MouseEvent e) {
            if (e.getClickCount() == 2) {
                handleEdit();
            }
        }
    };



    public AspectAssertionsList(OWLEditorKit eKit) {
        this.editorKit = eKit;
        aspectManager = AspectOWLEditorKitHook.getAspectManager(editorKit);
        setCellRenderer(new AspectAssertionListItemRenderer(eKit));
        addMouseListener(mouseListener);
        eKit.getOWLModelManager().addOntologyChangeListener(ontChangeListener);
    }

    public void setRootObject(AspectOWLAxiomInstance root){
        this.root = root;
        refill(root);
    }

    protected void refill(AspectOWLAxiomInstance root) {
        List<Object> data = new ArrayList<>();

        data.add(header);

        if (root != null){
            List<OWLAspectAssertionAxiom> aspects = new ArrayList<>(aspectManager.getAspectAssertionAxioms(root.getOntology(), root.getAxiom()));
            Comparator<OWLObject> owlObjectComparator = editorKit.getOWLModelManager().getOWLObjectComparator();
            OWLClassExpressionComparator aspectComparator =
                    new OWLClassExpressionComparator(editorKit.getOWLModelManager());
            aspects.sort((a1, a2) -> {
                int propComp = aspectComparator.compare(a1.getAspect(), a2.getAspect());
                if(propComp != 0) {
                    return propComp;
                }
                return owlObjectComparator.compare(a1, a2);
            });
            for (OWLAspectAssertionAxiom aspect : aspects){
                data.add(new AspectAssertionsList.AspectAssertionsListItem(root.getOntology(), aspect));
            }
        }

        setListData(data.toArray());
        revalidate();
    }


    public AspectOWLAxiomInstance getRoot(){
        return root;
    }


    protected void refresh() {
        setRootObject(root);
    }


    protected void handleOntologyChanges(List<? extends OWLOntologyChange> changes) {

        // this is complicated by the fact that adding an aspect to an axiom produces a new axiom
        if (newAxiom != null){
            for (OWLOntologyChange change : changes){
                if (change instanceof OWLAxiomChange){
                    if (change.getAxiom().equalsIgnoreAnnotations(getRoot().getAxiom())){
                        // @@TODO should check that ontology contains the new axiom
                        setRootObject(new AspectOWLAxiomInstance(newAxiom, root.getOntology(), aspectManager));
                        newAxiom = null;
                        return;
                    }
                }
            }
        }
    }

    protected void handleAdd() {
        // don't need to check the section as only the direct imports can be added
        if (editor == null){
            editor = editorKit.getWorkspace().getOWLComponentFactory().getOWLClassDescriptionEditor(null, AspectOWLEditorKitHook.OWL_ASPECT_ASSERTION_AXIOM_TYPE);
        }

        editor.setEditedObject(null);

        UIHelper uiHelper = new UIHelper(editorKit);
        int ret = uiHelper.showDialog("Create Aspect", editor.getEditorComponent(), null);

        if (ret == JOptionPane.OK_OPTION) {
            OWLClassExpression expression = editor.getEditedObject();
            if (expression != null) {
                OWLOntology ontology = getRoot().getOntology();
//                ontology.getOWLOntologyManager()
                Set <OWLAnnotation> annotations = Collections.EMPTY_SET; // TODO add annotation editor to UI, github issue #8
                OWLAspect aspect = aspectManager.getAspect(expression, annotations, Collections.EMPTY_SET);
                OWLAspectAssertionAxiom aspectAssertionAxiom = aspectManager.getAspectAssertionAxiom(ontology, new AspectOWLJoinPointAxiomPointcut(getRoot().getAxiom()), aspect);
                editorKit.getModelManager().applyChange(new AddAxiom(ontology, aspectAssertionAxiom));
            }
            refresh();
        }
    }



    public void dispose() {
        editorKit.getOWLModelManager().removeOntologyChangeListener(ontChangeListener);
        if (editor != null) {
            editor.dispose();
            editor = null;
        }
    }
    
    /**
     * @see org.protege.editor.core.ui.list.MList#getListItemButtons(org.protege.editor.core.ui.list.MListItem)
     */
    @Override
    protected List<MListButton> getListItemButtons(MListItem item) {
    	if (!(item instanceof AspectAssertionsListItem)) {
    		// this should not happen since an AspectASsertionsList should only contain AspectAssertionsListItems
    		return super.getListItemButtons(item);
    	}
    	
    	ArrayList<MListButton> result = new ArrayList<>();
    	
    	OWLAspectAssertionAxiom axiom = ((AspectAssertionsListItem)item).aspectAssertionAxiom;
    	OWLOntology ontology = ((AspectAssertionsListItem)item).ontology;
    	
		AspectButton button = new AspectButton(axiom, ontology, aspectManager);
		button.setActionListener(e -> {
			AspectAssertionPanel aspectAssertionPanel = new AspectAssertionPanel(editorKit);
			aspectAssertionPanel.setAxiom(new AspectOWLAxiomInstance(axiom, ontology, aspectManager));
			new UIHelper(editorKit).showDialog("Aspects for " + axiom.getAxiomType().toString() + " axiom", aspectAssertionPanel, JOptionPane.CLOSED_OPTION);
			aspectAssertionPanel.dispose();
//			editorKit.getModelManager().fireEvent(EventType.ACTIVE_ONTOLOGY_CHANGED);
		});
    	result.add(button);
    	result.addAll(super.getListItemButtons(item));
    	return Collections.unmodifiableList(result);
    }

    public class AspectAssertionsListItem implements MListItem {

        private OWLOntology ontology;

        private OWLAspectAssertionAxiom aspectAssertionAxiom;

        public AspectAssertionsListItem(OWLOntology ontology, OWLAspectAssertionAxiom aspectAssertionAxiom) {
            this.ontology = ontology;
            this.aspectAssertionAxiom = aspectAssertionAxiom;
        }


        public OWLAspectAssertionAxiom getAnnotation() {
            return aspectAssertionAxiom;
        }


        public boolean isEditable() {
            return true;
        }

        public void handleEdit() {
            // don't need to check the section as only the direct imports can be added
            if (editor == null){
                editor = editorKit.getWorkspace().getOWLComponentFactory().getOWLClassDescriptionEditor(null, AspectOWLEditorKitHook.OWL_ASPECT_ASSERTION_AXIOM_TYPE);
            }
            OWLAspect originalAspect = aspectAssertionAxiom.getAspect();
            editor.setEditedObject(originalAspect);
            UIHelper uiHelper = new UIHelper(editorKit);
            int ret = uiHelper.showValidatingDialog("Axiom Aspect", editor.getEditorComponent(), null);

            if (ret == JOptionPane.OK_OPTION) {
                OWLClassExpression newAspect =  editor.getEditedObject();
                if (newAspect != null && !newAspect.equals(originalAspect)) {
                    ArrayList<OWLOntologyChange> changes = new ArrayList<>(2);
                    changes.add(new RemoveAxiom(ontology, aspectAssertionAxiom));
                    Set <OWLAnnotation> annotations = Collections.EMPTY_SET; // TODO add annotation editor to UI, github issue #8
                    changes.add(new AddAxiom(ontology, aspectManager.getAspectAssertionAxiom(ontology, new AspectOWLJoinPointAxiomPointcut(getRoot().getAxiom()), aspectManager.getAspect(newAspect, annotations, originalAspect.getAspects()))));

//                    List<OWLOntologyChange> changes = getReplaceChanges(aspectAssertionAxiom.getAspect(), newAspect);
                    editorKit.getModelManager().applyChanges(changes);
                }
                refresh();
            }
        }


        public boolean isDeleteable() {
            return true;
        }


        public boolean handleDelete() {
            editorKit.getModelManager().applyChange(new RemoveAxiom(ontology, aspectAssertionAxiom));
            refresh();
            return true;
        }

        public String getTooltip() {
            return "Asserted aspect";
        }
    }

//    protected List<OWLOntologyChange> getReplaceChanges(OWLAspect oldAspect, OWLAspect newAspect) {
//        List<OWLOntologyChange> changes = new ArrayList<>();
//        final OWLAxiom ax = getRoot().getAxiom();
//        final OWLOntology ont = getRoot().getOntology();
//        Set<OWLAspect> aspects = new HashSet<>(aspectManager.getAssertedAspects(ont, ax));
//        aspects.remove(oldAspect);
//        aspects.add(newAspect);
//
//        newAxiom = ax.getAxiomWithoutAnnotations().getAnnotatedAxiom(aspects);
//
//        changes.add(new RemoveAxiom(ont, ax));
//        changes.add(new AddAxiom(ont, newAxiom));
//        return changes;
//    }
//
//
//    protected List<OWLOntologyChange> getDeleteChanges(OWLAspectAssertionAxiom oldAspectAssertionAxiom) {
//        List<OWLOntologyChange> changes = new ArrayList<>();
//        final OWLAxiom ax = getRoot();
//        final OWLOntology ont = getRoot().getOntology();
//
//        Set<OWLAnnotation> annotations = new HashSet<>(ax.getAnnotations());
//        annotations.remove(oldAnnotation);
//
//        newAxiom = ax.getAxiomWithoutAnnotations().getAnnotatedAxiom(annotations);
//
//        changes.add(new RemoveAxiom(ont, ax));
//        changes.add(new AddAxiom(ont, newAxiom));
//        return changes;
//    }

    private class AspectAssertionListItemRenderer implements ListCellRenderer {

        private OWLCellRenderer ren;


        public AspectAssertionListItemRenderer(OWLEditorKit editorKit) {
            ren = new OWLCellRenderer(editorKit);
        }


        public Component getListCellRendererComponent(JList list, Object value, int index, boolean isSelected,
                                                      boolean cellHasFocus) {
            if (value instanceof AspectAssertionsListItem) {
                AspectAssertionsListItem item = ((AspectAssertionsListItem) value);
                ren.setOntology(item.ontology);
                ren.setHighlightKeywords(true);
                ren.setWrap(false);
                return ren.getListCellRendererComponent(list, item.aspectAssertionAxiom.getAspect(), index, isSelected, cellHasFocus);
            }
            else {
                return ren.getListCellRendererComponent(list, value, index, isSelected, cellHasFocus);
            }
        }
    }

}
