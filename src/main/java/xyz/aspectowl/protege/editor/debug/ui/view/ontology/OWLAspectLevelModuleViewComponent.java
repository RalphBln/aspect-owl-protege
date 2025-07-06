package xyz.aspectowl.protege.editor.debug.ui.view.ontology;

import java.awt.*;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.util.*;
import javax.annotation.Nonnull;
import javax.swing.*;
import org.graphstream.algorithm.ConnectedComponents;
import org.graphstream.graph.Edge;
import org.graphstream.graph.EdgeFactory;
import org.graphstream.graph.Graph;
import org.graphstream.graph.Node;
import org.graphstream.graph.implementations.AbstractEdge;
import org.graphstream.graph.implementations.AbstractNode;
import org.graphstream.graph.implementations.MultiGraph;
import org.graphstream.ui.graphicGraph.GraphicElement;
import org.graphstream.ui.layout.Layout;
import org.graphstream.ui.layout.Layouts;
import org.graphstream.ui.swing_viewer.SwingViewer;
import org.graphstream.ui.view.View;
import org.graphstream.ui.view.util.InteractiveElement;
import org.paukov.combinatorics3.Generator;
import org.protege.editor.owl.model.event.EventType;
import org.protege.editor.owl.model.event.OWLModelManagerChangeEvent;
import org.protege.editor.owl.model.event.OWLModelManagerListener;
import org.protege.editor.owl.model.selection.OWLSelectionModelListener;
import org.protege.editor.owl.ui.view.AbstractActiveOntologyViewComponent;
import org.semanticweb.owlapi.model.*;
import org.semanticweb.owlapi.model.parameters.Imports;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import xyz.aspectowl.owlapi.model.AspectOWLAxiomPointcut;
import xyz.aspectowl.owlapi.model.AspectOWLPointcut;
import xyz.aspectowl.owlapi.model.OWLAspectAssertionAxiom;
import xyz.aspectowl.owlapi.model.OWLAspectManager;
import xyz.aspectowl.protege.AspectOWLEditorKitHook;

/**
 * @author Ralph Schäfermeier
 */
public class OWLAspectLevelModuleViewComponent extends AbstractActiveOntologyViewComponent
    implements OWLOntologyChangeVisitor {

  private Logger logger = LoggerFactory.getLogger(OWLAspectLevelModuleViewComponent.class);

  private static final String GRAPH_STYLESHEET =
      "node { "
          + "size-mode: fit; "
          + "shape: box; "
          + "stroke-width: 1; "
          + "fill-color: white; "
          + "stroke-mode: plain; "
          + "padding: 3px, 0px; "
          + "icon-mode: at-left; "
          + "} "
          + "edge { "
          + "arrow-shape: arrow; "
          + "} "
          + "node:clicked { "
          + "stroke-width: 2; "
          + "} "
          + "node:selected { "
          + "stroke-width: 3; "
          + "}";

  private static final String ASPECT_EDGE_STYLES =
      "shape: freeplane; "
          + "size: 3px; "
          + "fill-color: #FFD31C; "
          + "arrow-shape: arrow; "
          + "arrow-size: 8px, 8px;";

  public enum AxiomType {
    ONTOLOGY,
    ASPECT
  }

  private EnumSet<InteractiveElement> selectableElements = EnumSet.of(InteractiveElement.NODE);

  private HashMap<OWLOntology, Graph> graphs = new HashMap<>();
  private HashMap<OWLOntology, View> views = new HashMap<>();
  private HashMap<OWLOntology, ConnectedComponents> modulesByOntology = new HashMap<>();
  private OntologyOrAspectEdgeFactory edgeFactory = new OntologyOrAspectEdgeFactory();

  private int idGenerator = 0;

  private OWLOntologyChangeListener ontologyChangeListener = this::onOntologyChanged;
  private OWLModelManagerListener modelManagerListener = this::onModelManagerChange;
  private OWLSelectionModelListener selectionModelListener = this::onSelectionChanged;

  private void onSelectionChanged() {
    OWLEntity selectedEntity = getOWLWorkspace().getOWLSelectionModel().getSelectedEntity();
    if (selectedEntity != null) {
      Graph graph = graphs.get(getOWLModelManager().getActiveOntology());
      Node selectedNode = graph.getNode(getNodeId(selectedEntity));
      if (selectedNode != null) {
        selectedNode.setAttribute("ui.selected");
        graph
            .nodes()
            .filter(node -> node != selectedNode)
            .forEach(node -> node.removeAttribute("ui.selected"));
      }
    }
  }

  @Override
  protected void initialiseOntologyView() throws Exception {
    setLayout(new BorderLayout());
    getOWLModelManager().addOntologyChangeListener(ontologyChangeListener);
    getOWLModelManager().addListener(modelManagerListener);
    getOWLWorkspace().getOWLSelectionModel().addListener(selectionModelListener);
    updateView(getOWLModelManager().getActiveOntology());
  }

  @Override
  protected void disposeOntologyView() {
    getOWLModelManager().removeOntologyChangeListener(ontologyChangeListener);
    getOWLModelManager().removeListener(modelManagerListener);
    getOWLWorkspace().getOWLSelectionModel().removeListener(selectionModelListener);
  }

  private void onOntologyChanged(Collection<? extends OWLOntologyChange> changes) {
    changes.forEach(change -> change.accept(this));
  }

  private void onModelManagerChange(OWLModelManagerChangeEvent event) {
    if (event.isType(EventType.ONTOLOGY_RELOADED)) {
      logger.info("Ontology reloaded.");
      OWLOntology activeOntology = event.getSource().getActiveOntology();
      graphs.remove(activeOntology);
      views.remove(activeOntology);
      modulesByOntology.remove(activeOntology);
      try {
        updateView(activeOntology);
      } catch (Exception e) {
        logger.error("Error updating view", e);
      }
    }
  }

  @Override
  protected void updateView(OWLOntology activeOntology) throws Exception {
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
    add((JPanel) view);
    repaint();
  }

  private View createGraphAndView(OWLOntology ontology) {
    Graph graph = getGraph(ontology);

    graph.setAttribute("ui.stylesheet", GRAPH_STYLESHEET);
    graph.setAttribute("ui.antialias");

    // Get partition
    ConnectedComponents modules =
        new ConnectedComponents() {
          @Override
          public void edgeAdded(
              String graphId,
              long timeId,
              String edgeId,
              String fromNodeId,
              String toNodeId,
              boolean directed) {
            Edge edge = graph.getEdge(edgeId);
            if (!isCutEdge(edge)) {
              super.edgeAdded(graphId, timeId, edgeId, fromNodeId, toNodeId, directed);
            }
          }
        };
    modulesByOntology.put(ontology, modules);
    modules.setCutAttribute("aspectEdge");
    modules.init(graph);
    SwingViewer viewer = new SwingViewer(graph, SwingViewer.ThreadingModel.GRAPH_IN_ANOTHER_THREAD);
    Layout layout = Layouts.newLayoutAlgorithm();
    viewer.enableAutoLayout(layout);
    View view = viewer.addDefaultView(false);
    view.addListener(
        "Mouse",
        new MouseAdapter() {
          @Override
          public void mousePressed(MouseEvent event) {
            super.mousePressed(event);
            GraphicElement curElement =
                view.findGraphicElementAt(selectableElements, event.getX(), event.getY());
            if (curElement != null && curElement instanceof Node) {
              OWLEntity selectedEntity =
                  graph.getNode(curElement.getId()).getAttribute("owlEntity", OWLEntity.class);
              getOWLWorkspace().getOWLSelectionModel().setSelectedEntity(selectedEntity);
              getOWLWorkspace().displayOWLEntity(selectedEntity);
            }
          }
        });

    // Construct ontology graph
    ontology.getSignature(Imports.INCLUDED).forEach(entity -> addNode(graph, entity));
    var axioms = ontology.getLogicalAxioms(Imports.INCLUDED);
    axioms.forEach(
        axiom -> {
          addClique(graph, axiom);
        });

    // Construct aspect graph
    OWLAspectManager am = AspectOWLEditorKitHook.getAspectManager(getOWLEditorKit());
    axioms.forEach(
        axiom -> {
          am.getAspectAssertionAxioms(ontology, axiom)
              .forEach(
                  aspectAssertionAxiom -> {
                    addAspect(graph, aspectAssertionAxiom);
                  });
        });
    return view;
  }

  private Graph getGraph(OWLOntology ontology) {
    Graph ontologyGraph = graphs.get(ontology);
    if (ontologyGraph == null) {
      ontologyGraph =
          new MultiGraph(
              ontology.getOntologyID().toString(),
              false,
              true,
              ontology.getSignature(Imports.INCLUDED).size(),
              ontology.getLogicalAxiomCount(Imports.INCLUDED) * 3);
      ontologyGraph.setEdgeFactory(edgeFactory);
      graphs.put(ontology, ontologyGraph);
    }
    return ontologyGraph;
  }

  private class OntologyOrAspectEdgeFactory implements EdgeFactory<OntologyOrAspectEdge> {

    private AxiomType axiomType;

    public void setAxiomType(AxiomType axiomType) {
      this.axiomType = axiomType;
    }

    @Override
    public OntologyOrAspectEdge newInstance(String id, Node src, Node dst, boolean directed) {
      var edge = new OntologyOrAspectEdge(id, (AbstractNode) src, (AbstractNode) dst, directed);
      if (axiomType == AxiomType.ASPECT) {
        edge.setAttribute("aspectEdge");
      }
      return edge;
    }
  }

  private class OntologyOrAspectEdge extends AbstractEdge {

    /**
     * Constructs a new edge. This constructor copies the parameters into the corresponding fields.
     *
     * @param id Unique identifier of this edge.
     * @param source Source node.
     * @param target Target node.
     * @param directed Indicates if the edge is directed.
     */
    protected OntologyOrAspectEdge(
        String id, AbstractNode source, AbstractNode target, boolean directed) {
      super(id, source, target, directed);
    }
  }

  private void addNode(Graph graph, OWLEntity entity) {
    graph
        .addNode(entity.toString())
        .setAttributes(
            Map.of(
                "ui.label",
                entity.getIRI().getShortForm(),
                "ui.style",
                String.format("icon: url('%s');", getIconURL(entity)),
                "owlEntity",
                entity));
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
    Generator.combination(axiom.getSignature())
        .simple(2)
        .forEach(
            owlEntityPair -> {
              addEdge(graph, axiom, AxiomType.ONTOLOGY, owlEntityPair.get(0), owlEntityPair.get(1));
            });
  }

  private Edge addEdge(
      Graph graph, OWLAxiom axiom, AxiomType axiomType, OWLEntity entity1, OWLEntity entity2) {
    // Check if edge already exists
    // There might be multiple edges between the two entity nodes (one ontology and one aspect
    // edge), so we need to
    // find the correct one (if it is present)
    Edge edge =
        findEdge(graph, axiomType, entity1, entity2)
            .orElse(createEdge(graph, axiom, axiomType, entity1, entity2));
    edge.getAttribute("axioms", HashSet.class).add(axiom);
    return edge;
  }

  private Optional<Edge> findEdge(
      Graph graph, AxiomType axiomType, OWLEntity entity1, OWLEntity entity2) {
    return graph
        .getNode(getNodeId(entity1))
        .leavingEdges()
        .filter(
            potentialEdge ->
                potentialEdge.getOpposite(graph.getNode(getNodeId(entity1)))
                        == graph.getNode(getNodeId(entity2))
                    && potentialEdge.getAttribute("axiom.type", AxiomType.class) == axiomType)
        .findFirst();
  }

  private Edge createEdge(
      Graph graph, OWLAxiom axiom, AxiomType axiomType, OWLEntity entity1, OWLEntity entity2) {
    edgeFactory.setAxiomType(axiomType);
    Edge edge =
        graph.addEdge(
            "" + idGenerator++,
            getNodeId(entity1),
            getNodeId(entity2),
            axiomType == AxiomType.ASPECT);
    edge.setAttribute("axioms", new HashSet<>());
    edge.setAttribute("layout.weight", axiomType == AxiomType.ASPECT ? 3 : 2);
    edge.setAttribute("axiom.type", axiomType);
    if (axiomType == AxiomType.ASPECT) {
      edge.setAttribute("ui.style", ASPECT_EDGE_STYLES);
    }
    logger.info(axiomType + " Axiom added: " + axiom);
    logger.info("Modules:");
    modulesByOntology
        .get(getOWLModelManager().getActiveOntology())
        .forEach(
            component -> {
              logger.info("  " + component.toString());
              component.nodes().forEach(node -> logger.info("    " + node.toString()));
            });
    return edge;
  }

  private AxiomType getAxiomType(OWLAxiom axiom) {
    return axiom instanceof OWLAspectAssertionAxiom ? AxiomType.ASPECT : AxiomType.ONTOLOGY;
  }

  private String getNodeId(OWLEntity entity) {
    return entity.toString();
  }

  private void recolorEdges() {
    Graph graph = graphs.get(getOWLModelManager().getActiveOntology());
    ConnectedComponents modules = modulesByOntology.get(getOWLModelManager().getActiveOntology());
    graph
        .edges()
        .filter(edge -> edge.getAttribute("axiom.type", AxiomType.class) == AxiomType.ASPECT)
        .forEach(
            edge -> {
              if (modules.getConnectedComponentOf(edge.getSourceNode())
                  == modules.getConnectedComponentOf(edge.getTargetNode())) {
                edge.setAttribute("ui.style", "fill-color: red;");
              } else {
                edge.setAttribute("ui.style", "fill-color: #FFD31C;");
              }
            });
  }

  @Override
  public void visit(@Nonnull AddAxiom addAxiom) {
    OWLAxiom axiom = addAxiom.getAxiom();
    if (axiom instanceof OWLAspectAssertionAxiom) {
      addAspect(
          getGraph(getOWLModelManager().getActiveOntology()), (OWLAspectAssertionAxiom) axiom);
    } else if (axiom instanceof OWLDeclarationAxiom) {
      addNode(
          getGraph(getOWLModelManager().getActiveOntology()),
          ((OWLDeclarationAxiom) axiom).getEntity());
    } else if (axiom.isLogicalAxiom()) {
      addClique(getGraph(getOWLModelManager().getActiveOntology()), axiom);
      recolorEdges();
    }
  }

  private void addAspect(Graph graph, OWLAspectAssertionAxiom aspectAssertionAxiom) {
    ConnectedComponents modules = modulesByOntology.get(getOWLModelManager().getActiveOntology());
    AspectOWLPointcut pointcut = aspectAssertionAxiom.getPointcut();
    if (pointcut instanceof AspectOWLAxiomPointcut) {
      ((AspectOWLAxiomPointcut) pointcut)
          .getAssertedAxiomsInPointcut()
          .forEach(
              axiom ->
                  axiom
                      .getSignature()
                      .forEach(
                          ontologyEntity -> {
                            aspectAssertionAxiom
                                .getAspect()
                                .getSignature()
                                .forEach(
                                    aspectEntity -> {
                                      Edge aspectEdge =
                                          addEdge(
                                              graph,
                                              axiom,
                                              AxiomType.ASPECT,
                                              ontologyEntity,
                                              aspectEntity);
                                      if (modules.getConnectedComponentOf(
                                              aspectEdge.getSourceNode())
                                          == modules.getConnectedComponentOf(
                                              aspectEdge.getTargetNode())) {
                                        aspectEdge.setAttribute("ui.style", "fill-color: red;");
                                      }
                                    });
                          }));
    }
  }

  @Override
  public void visit(@Nonnull RemoveAxiom removeAxiom) {
    Graph graph = getGraph(getOWLModelManager().getActiveOntology());
    OWLAxiom axiom = removeAxiom.getAxiom();
    if (axiom instanceof OWLAspectAssertionAxiom) {
      AspectOWLPointcut pointcut = ((OWLAspectAssertionAxiom) axiom).getPointcut();
      if (pointcut instanceof AspectOWLAxiomPointcut) {
        ((AspectOWLAxiomPointcut) pointcut)
            .getAssertedAxiomsInPointcut()
            .forEach(
                owlAxiom -> {
                  owlAxiom
                      .getSignature()
                      .forEach(
                          ontologyEntity ->
                              ((OWLAspectAssertionAxiom) axiom)
                                  .getAspect()
                                  .getSignature()
                                  .forEach(
                                      aspectEntity -> {
                                        findEdge(
                                                graph,
                                                AxiomType.ASPECT,
                                                ontologyEntity,
                                                aspectEntity)
                                            .ifPresent(
                                                edge -> {
                                                  HashSet<OWLAxiom> axioms =
                                                      edge.getAttribute("axioms", HashSet.class);
                                                  axioms.remove(owlAxiom);
                                                  if (axioms.isEmpty()) {
                                                    graph.removeEdge(edge);
                                                  }
                                                });
                                      }));
                });
      }
    } else if (axiom instanceof OWLDeclarationAxiom) {
      // An entity has been removed from the ontology.
      // This also removes all axioms the entity is part of.
      // We can just delete the node for the entity.
      // All edges will be removed automatically.
      graph.removeNode(getNodeId(((OWLDeclarationAxiom) removeAxiom.getAxiom()).getEntity()));
    } else if (axiom.isLogicalAxiom()) {
      // An axiom has been removed.
      // The entities in the removed axiom's signature remain in the ontology.
      // We need to check if the corresponding edges in the graph represent other axioms.
      // Only if no other axiom in the ontology is responsible for an edge, we can remove the edge.
      Generator.combination(removeAxiom.getSignature())
          .simple(2)
          .forEach(
              owlEntityPair -> {
                findEdge(graph, AxiomType.ONTOLOGY, owlEntityPair.get(0), owlEntityPair.get(1))
                    .ifPresent(
                        edge -> {
                          HashSet<OWLAxiom> axioms = edge.getAttribute("axioms", HashSet.class);
                          axioms.remove(removeAxiom.getAxiom());
                          if (axioms.isEmpty()) {
                            graph.removeEdge(edge);
                          }
                        });
              });
      recolorEdges();
    }
  }

  @Override
  public void visit(@Nonnull SetOntologyID setOntologyID) {}

  @Override
  public void visit(@Nonnull AddImport addImport) {}

  @Override
  public void visit(@Nonnull RemoveImport removeImport) {}

  @Override
  public void visit(@Nonnull AddOntologyAnnotation addOntologyAnnotation) {}

  @Override
  public void visit(@Nonnull RemoveOntologyAnnotation removeOntologyAnnotation) {}
}
