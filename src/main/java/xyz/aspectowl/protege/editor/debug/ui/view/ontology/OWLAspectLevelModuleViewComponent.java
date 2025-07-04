package xyz.aspectowl.protege.editor.debug.ui.view.ontology;

import org.graphstream.graph.Edge;
import org.graphstream.graph.Graph;
import org.graphstream.graph.Node;
import org.graphstream.graph.implementations.MultiGraph;
import org.graphstream.ui.graphicGraph.GraphicElement;
import org.graphstream.ui.layout.Layout;
import org.graphstream.ui.layout.Layouts;
import org.graphstream.ui.swing_viewer.SwingViewer;
import org.graphstream.ui.view.View;
import org.graphstream.ui.view.Viewer;
import org.graphstream.ui.view.util.InteractiveElement;
import org.paukov.combinatorics3.Generator;
import org.protege.editor.owl.model.selection.OWLSelectionModelListener;
import org.protege.editor.owl.ui.view.AbstractActiveOntologyViewComponent;
import org.semanticweb.owlapi.model.*;
import org.semanticweb.owlapi.model.parameters.Imports;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import xyz.aspectowl.owlapi.model.OWLAspectManager;
import xyz.aspectowl.protege.AspectOWLEditorKitHook;

import javax.annotation.Nonnull;
import javax.swing.*;
import java.awt.*;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.util.*;

/**
 * @author Ralph Schäfermeier
 */
public class OWLAspectLevelModuleViewComponent extends AbstractActiveOntologyViewComponent implements OWLOntologyChangeVisitor {
  
  private Logger logger = LoggerFactory.getLogger(OWLAspectLevelModuleViewComponent.class);
  
  private static final String GRAPH_STYLESHEET = "node { " +
          "size-mode: fit; " +
          "shape: box; " +
          "stroke-width: 1; " +
          "fill-color: white; " +
          "stroke-mode: plain; " +
          "padding: 3px, 0px; " +
          "icon-mode: at-left; " +
          "} " +
          "edge { " +
          "arrow-shape: arrow; " +
          "} " +
          "node:clicked { " +
          "stroke-width: 2; " +
          "} " +
          "node:selected { " +
          "stroke-width: 3; " +
          "}";
  
  private static final String ASPECT_EDGE_STYLES =
          "shape: freeplane; " +
                  "size: 3px; " +
                  "fill-color: darkorange; " +
                  "arrow-shape: arrow; " +
                  "arrow-size: 8px, 8px;";
  
  private EnumSet<InteractiveElement> selectableElements = EnumSet.of(InteractiveElement.NODE);
  
  private HashMap<OWLOntology, Graph> graphs = new HashMap<>();
  private HashMap<OWLOntology, View> views = new HashMap<>();
  
  private int idGenerator = 0;
  
  private OWLOntologyChangeListener ontologyChangeListener = this::onOntologyChanged;
  private OWLSelectionModelListener selectionModelListener = this::onSelectionChanged;
  
  private void onSelectionChanged() {
    OWLEntity selectedEntity = getOWLWorkspace().getOWLSelectionModel().getSelectedEntity();
    if (selectedEntity != null) {
      Graph graph = graphs.get(getOWLModelManager().getActiveOntology());
      Node selectedNode = graph.getNode(getNodeId(selectedEntity));
      selectedNode.setAttribute("ui.selected");
      graph.nodes().filter(node -> node != selectedNode).forEach(node -> node.removeAttribute("ui.selected"));
    }
  }
  
  @Override
  protected void initialiseOntologyView() throws Exception {
    setLayout(new BorderLayout());
    getOWLModelManager().addOntologyChangeListener(ontologyChangeListener);
    getOWLWorkspace().getOWLSelectionModel().addListener(selectionModelListener);
    updateView(getOWLModelManager().getActiveOntology());
  }
  
  @Override
  protected void disposeOntologyView() {
    getOWLModelManager().removeOntologyChangeListener(ontologyChangeListener);
    getOWLWorkspace().getOWLSelectionModel().removeListener(selectionModelListener);
  }
  
  private void onOntologyChanged(Collection<? extends OWLOntologyChange> changes) {
    changes.forEach(change -> change.accept(this));
  }
  
  @Override
  protected void updateView(OWLOntology activeOntology) throws Exception {
    logger.info("*** START....... " + System.currentTimeMillis());
    View view = views.get(activeOntology);
    if (view == null) {
      try {
        setCursor(Cursor.getPredefinedCursor(Cursor.WAIT_CURSOR));
        view = createGraphAndView(activeOntology);
        views.put(activeOntology, view);
      } finally {
        setCursor(Cursor.getPredefinedCursor(Cursor.DEFAULT_CURSOR));
      }
    }
    removeAll();
    logger.info("   *** ADDING....... " + System.currentTimeMillis());
    add((JPanel) view);
    logger.info("      *** REPAINTING....... " + System.currentTimeMillis());
    repaint();
    logger.info("         *** DONE! " + System.currentTimeMillis());
  }
  
  private View createGraphAndView(OWLOntology ontology) {
    Graph graph = getGraph(ontology);
    
    graph.setAttribute("ui.stylesheet", GRAPH_STYLESHEET);
    graph.setAttribute("ui.antialias");
    
    SwingViewer viewer = new SwingViewer(graph, SwingViewer.ThreadingModel.GRAPH_IN_ANOTHER_THREAD);
    Layout layout = Layouts.newLayoutAlgorithm();
    viewer.enableAutoLayout(layout);
    View view = viewer.addDefaultView(false);
    view.addListener("Mouse", new MouseAdapter() {
      @Override
      public void mousePressed(MouseEvent event) {
        super.mousePressed(event);
        GraphicElement curElement = view.findGraphicElementAt(selectableElements, event.getX(), event.getY());
        if (curElement != null && curElement instanceof Node) {
          OWLEntity selectedEntity = graph.getNode(curElement.getId()).getAttribute("owlEntity", OWLEntity.class);
          getOWLWorkspace().getOWLSelectionModel().setSelectedEntity(selectedEntity);
          getOWLWorkspace().displayOWLEntity(selectedEntity);
        }
      }
    });
    
    ontology.getSignature(Imports.INCLUDED).forEach(entity -> addNode(graph, entity));
    
    var axioms = ontology.getLogicalAxioms(Imports.INCLUDED);
    axioms.forEach(axiom -> {
      addClique(graph, axiom);
    });
    
    OWLAspectManager am = AspectOWLEditorKitHook.getAspectManager(getOWLEditorKit());
    axioms.forEach(axiom -> {
      am.getAssertedAspects(ontology, axiom).forEach(aspect -> {
        axiom.getSignature().forEach(ontologyEntity -> {
          aspect.getSignature().forEach(aspectEntity -> {
            Edge aspectEdge = graph.addEdge("" + idGenerator++, aspectEntity.toString(), ontologyEntity.toString(), true);
            if (aspectEdge != null) {
              aspectEdge.setAttribute("ui.style", ASPECT_EDGE_STYLES);
              aspectEdge.setAttribute("layout.weight", 3);
            }
          });
        });
      });
    });
    return view;
  }
  
  private Graph getGraph(OWLOntology ontology) {
    Graph ontologyGraph = graphs.get(ontology);
    if (ontologyGraph == null) {
      ontologyGraph = new MultiGraph(ontology.getOntologyID().toString(), false, true, ontology.getSignature(Imports.INCLUDED).size(), ontology.getLogicalAxiomCount(Imports.INCLUDED
      ) * 3);
      graphs.put(ontology, ontologyGraph);
    }
    return ontologyGraph;
  }
  
  private void addNode(Graph graph, OWLEntity entity) {
    graph.addNode(entity.toString()).setAttributes(Map.of("ui.label", entity.getIRI().getShortForm(), "ui.style", String.format("icon: url('%s');", getIconURL(entity)), "owlEntity", entity));
  }
  
  private String getIconURL(OWLEntity entity) {
    EntityType type = entity.getEntityType();
    if (type == EntityType.CLASS)
      return "https://github.com/protegeproject/protege/blob/master/protege-editor-owl/src/main/resources/class.primitive.png?raw=true";
    if (type == EntityType.OBJECT_PROPERTY)
      return "https://github.com/protegeproject/protege/blob/master/protege-editor-owl/src/main/resources/property.object.png?raw=true";
    if (type == EntityType.DATA_PROPERTY)
      return "https://github.com/protegeproject/protege/blob/master/protege-editor-owl/src/main/resources/property.data.png?raw=true";
    if (type == EntityType.NAMED_INDIVIDUAL)
      return "https://github.com/protegeproject/protege/blob/master/protege-editor-owl/src/main/resources/individual.png?raw=true";
    return "https://upload.wikimedia.org/wikipedia/commons/thumb/d/d9/Icon-round-Question_mark.svg/16px-Icon-round-Question_mark.svg.png";
  }
  
  private void addClique(Graph graph, OWLAxiom axiom) {
    axiom.getSignature().forEach(entity -> addNode(graph, entity));
    Generator.combination(axiom.getSignature()).simple(2).forEach(owlEntityPair -> {
      addEdge(graph, axiom, owlEntityPair.get(0), owlEntityPair.get(1));
    });
  }
  
  private void addEdge(Graph graph, OWLAxiom axiom, OWLEntity entity1, OWLEntity entity2) {
    // check if edge already exists
    Edge edge = graph.getNode(getNodeId(entity1)).getEdgeBetween(getNodeId(entity2));
    HashSet<OWLAxiom> axioms;
    if (edge == null) {
      edge = graph.addEdge("" + idGenerator++, getNodeId(entity1), getNodeId(entity2), false);
      axioms = new HashSet<>();
      edge.setAttribute("axioms", axioms);
      edge.setAttribute("layout.weight", 2);
    } else {
      axioms = edge.getAttribute("axioms", HashSet.class);
    }
    axioms.add(axiom);
  }
  
  private String getNodeId(OWLEntity entity) {
    return entity.toString();
  }
  
  @Override
  public void visit(@Nonnull AddAxiom addAxiom) {
    addClique(getGraph(getOWLModelManager().getActiveOntology()), addAxiom.getAxiom());
  }
  
  @Override
  public void visit(@Nonnull RemoveAxiom removeAxiom) {
    Graph graph = getGraph(getOWLModelManager().getActiveOntology());
    if (removeAxiom.getAxiom() instanceof OWLDeclarationAxiom) {
      // An entity has been removed from the ontology.
      // This also removes all axioms the entity is part of.
      // We can just delete the node for the entity.
      // All edges will be removed automatically.
      graph.removeNode(getNodeId(((OWLDeclarationAxiom) removeAxiom.getAxiom()).getEntity()));
    } else {
      // An axiom has been removed.
      // The entities in the removed axiom's signature remain in the ontology.
      // We need to check if the corresponding edges in the graph represent other axioms.
      // Only if no other axiom in the ontology is responsible for an edge, we can remove the edge.
      Generator.combination(removeAxiom.getSignature()).simple(2).forEach(owlEntityPair -> {
        Edge edge = graph.getNode(getNodeId(owlEntityPair.get(0))).getEdgeBetween(getNodeId(owlEntityPair.get(1)));
        HashSet<OWLAxiom> axioms = edge.getAttribute("axioms", HashSet.class);
        axioms.remove(removeAxiom.getAxiom());
        if (axioms.isEmpty()) {
          graph.removeEdge(edge);
        }
      });
    }
  }
  
  @Override
  public void visit(@Nonnull SetOntologyID setOntologyID) {
  
  }
  
  @Override
  public void visit(@Nonnull AddImport addImport) {
  
  }
  
  @Override
  public void visit(@Nonnull RemoveImport removeImport) {
  
  }
  
  @Override
  public void visit(@Nonnull AddOntologyAnnotation addOntologyAnnotation) {
  
  }
  
  @Override
  public void visit(@Nonnull RemoveOntologyAnnotation removeOntologyAnnotation) {
  
  }
  
}
