/**
 * 
 */
package de.fuberlin.csw.aspectowl.owlapi.protege;

import de.fuberlin.csw.aspectowl.owlapi.model.OWLAspectAssertionAxiom;
import de.fuberlin.csw.aspectowl.owlapi.model.OWLOntologyAspectManager;
import de.fuberlin.csw.aspectowl.owlapi.model.impl.OWLAxiomInstance;
import de.fuberlin.csw.aspectowl.owlapi.vocab.AspectOWLVocabulary;
import de.fuberlin.csw.aspectowl.parser.AspectOrientedFunctionalSyntaxDocumentFormat;
import de.fuberlin.csw.aspectowl.parser.AspectOrientedOWLFunctionalSyntaxParserFactory;
import de.fuberlin.csw.aspectowl.parser.AspectOrientedOntologyPreSaveChecker;
import de.fuberlin.csw.aspectowl.protege.AspectOWLWorkspace;
import de.fuberlin.csw.aspectowl.protege.editor.core.ui.AspectButton;
import de.fuberlin.csw.aspectowl.protege.views.AspectAssertionPanel;
import de.fuberlin.csw.aspectowl.renderer.AspectOWLFunctionalSyntaxStorerFactory;
import javassist.*;
import org.osgi.framework.hooks.weaving.WeavingHook;
import org.osgi.framework.hooks.weaving.WovenClass;
import org.protege.editor.core.ModelManager;
import org.protege.editor.core.editorkit.EditorKit;
import org.protege.editor.core.editorkit.plugin.EditorKitHook;
import org.protege.editor.core.plugin.PluginUtilities;
import org.protege.editor.core.ui.list.MListButton;
import org.protege.editor.owl.OWLEditorKit;
import org.protege.editor.owl.model.OWLModelManager;
import org.protege.editor.owl.ui.UIHelper;
import org.protege.editor.owl.ui.action.ProtegeOWLAction;
import org.protege.editor.owl.ui.action.SelectedOWLEntityAction;
import org.protege.editor.owl.ui.frame.OWLFrameSectionRow;
import org.semanticweb.owlapi.model.*;
import org.semanticweb.owlapi.util.OWLEntityRenamer;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.swing.*;
import java.io.IOException;
import java.lang.reflect.Constructor;
import java.lang.reflect.Field;
import java.lang.reflect.InvocationTargetException;
import java.util.*;

/**
 * @author ralph
 */
public class AspectOWLEditorKitHook extends EditorKitHook implements WeavingHook {

	private static final Logger log = LoggerFactory.getLogger(AspectOWLEditorKitHook.class);

	private final HashSet<String> frameSectionRowClassesForAspectButtons = new HashSet<>();
	private final HashSet<String> propertyCharacteristicsViewComponentClassesForAspectButtons = new HashSet<>();
	private final HashSet<String> axiomTypeClasses = new HashSet<>();

	private final OWLOntologyAspectManager am = new OWLOntologyAspectManager();
	private final static Map<ModelManager, OWLOntologyAspectManager> aspectManagers = new HashMap<>();

	public static AxiomType<OWLAspectAssertionAxiom> OWL_ASPECT_ASSERTION_AXIOM_TYPE;

//    TODO weaving does not work (yet)
//    static {
//        Class axiomTypeClass = AxiomType.class;
//        try {
//            Field axiomTypeField = axiomTypeClass.getField("OWL_ASPECT_ASSERTION_AXIOM_TYPE");
//            OWL_ASPECT_ASSERTION_AXIOM_TYPE = (AxiomType<OWLAspectAssertionAxiom>) axiomTypeField.get(null);
//        } catch (NoSuchFieldException e) {
//            e.printStackTrace();
//        } catch (IllegalAccessException e) {
//            e.printStackTrace();
//        }
//    }

	static {
		// sneak in our aspectAssertionAxiom assertion axiom type (need to use brute force and a hammer, since the AxiomType class
		// is final and its constructor is private).
		Class<AxiomType> axiomTypeClass = AxiomType.class;
		try {
			Constructor<AxiomType> constr = axiomTypeClass.getDeclaredConstructor(Class.class, Integer.TYPE, String.class, Boolean.TYPE, Boolean.TYPE, Boolean.TYPE);
			constr.setAccessible(true);
			OWL_ASPECT_ASSERTION_AXIOM_TYPE = (AxiomType<OWLAspectAssertionAxiom>)constr.newInstance(OWLAspectAssertionAxiom.class, 38, "AspectAssertion", true, true, true);
			AxiomType.AXIOM_TYPES.add(OWL_ASPECT_ASSERTION_AXIOM_TYPE);
		} catch (NoSuchMethodException e) {
			e.printStackTrace();
		} catch (IllegalAccessException e) {
			e.printStackTrace();
		} catch (InstantiationException e) {
			e.printStackTrace();
		} catch (InvocationTargetException e) {
			e.printStackTrace();
		}
		AspectOWLVocabulary.ASPECT.getIRI(); // Workaround for https://github.com/protegeproject/protege/issues/336
	}

	/**
	 * 
	 */
	public AspectOWLEditorKitHook() {
		// Add classes that need to be woven here. Used to be more than one, hence the sets. Right now it's only one for
		// each type, but could change again. TODO remove sets for single woven classes, make them simple member instead.
		frameSectionRowClassesForAspectButtons.add("org.protege.editor.owl.ui.framelist.OWLFrameList");
		propertyCharacteristicsViewComponentClassesForAspectButtons.add("org.protege.editor.owl.ui.view.objectproperty.OWLObjectPropertyCharacteristicsViewComponent");
//		axiomTypeClasses.add("org.semanticweb.owlapi.model.AxiomType$1");
		axiomTypeClasses.add("org.semanticweb.owlapi.model.AxiomType$1");
		axiomTypeClasses.add("org.semanticweb.owlapi.model.AxiomType$2");
	}

	@Override
	protected void setup(EditorKit editorKit) {
		super.setup(editorKit);
		aspectManagers.put(editorKit.getModelManager(), am);
	}

	/* Sneaks in our preprocessor for aspect-oriented ontologies
	 * @see org.protege.editor.core.plugin.ProtegePluginInstance#initialise()
	 */
	@Override
	public void initialise() throws Exception {

		log.info("Initializing Aspect-Oriented OWL plug-in.");

		ClassPool.getDefault();
		NotFoundException.class.getClass();
		CannotCompileException.class.getClass();
		CtClass.class.getClass();
		CtMethod.class.getClass();
		ByteArrayClassPath.class.getClass();

		PluginUtilities.getInstance().getApplicationContext().registerService(WeavingHook.class.getName(), this, new Hashtable<>());

		OWLModelManager mm = ((OWLEditorKit)getEditorKit()).getOWLModelManager();

		mm.addOntologyChangeListener(am);

		OWLOntologyManager om = mm.getOWLOntologyManager();

		om.getOntologyStorers().add(new AspectOWLFunctionalSyntaxStorerFactory(am));

		mm.addIOListener(new AspectOrientedOntologyPreSaveChecker(om));

		// The following commented-in code does not work in Java 9 and upwards due to tighter security measurements
		// in the reflection API introduced with that version.
		// Instead we replace the OWLWorkspace instance with our AspectOWLWorkspace, which overrides the
		// getOWLIconProvider method
//		Class poorLittleClass = OWLWorkspace.class;
//		Field ipField = poorLittleClass.getDeclaredField("iconProvider");
//		ipField.setAccessible(true);
//		Field modifiersField = Field.class.getDeclaredField("modifiers");
//		modifiersField.setAccessible(true); // Ouch. This is EVIL.
//		modifiersField.setInt(ipField, ipField.getModifiers() & ~Modifier.FINAL);
//		ipField.set(((OWLEditorKit)getEditorKit()).getOWLWorkspace(), new AspectOWLIconProviderImpl(mm, am));

		AspectOWLWorkspace workspaceReplacement = new AspectOWLWorkspace(am, mm); // need to pass ModelManager here because the EditorKit's MM instance is set later.
		workspaceReplacement.setup(getEditorKit());
		Class poorLittleClass = OWLEditorKit.class;
		Field ipField = poorLittleClass.getDeclaredField("workspace");
		ipField.setAccessible(true);
		ipField.set(getEditorKit(), workspaceReplacement);
	}

	/* (non-Javadoc)
	 * @see org.protege.editor.core.Disposable#dispose()
	 */
	@Override
	public void dispose() throws Exception {
		OWLModelManager modelManager = ((OWLEditorKit)getEditorKit()).getOWLModelManager();
		modelManager.removeOntologyChangeListener(am);
		aspectManagers.remove(modelManager);
	}

	public static OWLOntologyAspectManager getAspectManager(EditorKit editorKit) {
		return aspectManagers.get(editorKit.getModelManager());
	}

	public static OWLOntologyAspectManager getAspectManager(OWLModelManager modelManager) {
		return aspectManagers.get(modelManager);
	}




	@Override
	public void weave(WovenClass wovenClass) {

		String className = wovenClass.getClassName();

		try {

			// add aspect buttons to frame section rows
			if (frameSectionRowClassesForAspectButtons.contains(className)) {

				CtClass ctClass = prepareClassForWeaving(wovenClass);

//				CtMethod ctMethod = ctClass.getMethod("getAdditionalButtons", "()Ljava/util/List;"); // throws NotFoundException if method does not exist
				CtMethod ctMethod = ctClass.getMethod("getButtons", "(Ljava/lang/Object;)Ljava/util/List;"); // throws NotFoundException if method does not exist

				CtClass declaringClass = ctMethod.getDeclaringClass();

				if (declaringClass != ctClass) {
					ctMethod = CtNewMethod.copy(ctMethod, ctClass, null);
					ctClass.addMethod(ctMethod);
				}

				ctMethod.insertAfter("if (value instanceof org.protege.editor.owl.ui.frame.OWLFrameSectionRow) return de.fuberlin.csw.aspectowl.owlapi.protege.AspectOWLEditorKitHook.getButtonsWithAspectButton($_, (org.protege.editor.owl.ui.frame.OWLFrameSectionRow)value, editorKit);");

				finalizeClassForWeaving(wovenClass, ctClass);

			} else if (className.equals("org.protege.editor.owl.model.io.OntologyLoader")) {

				// Each time an ontology is loaded, Protege creates a new OWLOntologyManager. This ontology manager is used
				// for the loading process. After the ontology (and potential imports) are loaded, the ontologies are copied
				// to the main ontology manager (the one stored in the single OWLModelManager instance). Then, the loading
				// OWLOntologyManger is discarded. Anyway, we need to add our ParserFactory to each loading ontology manager.

				CtClass ctClass = prepareClassForWeaving(wovenClass);

				CtMethod ctMethod = ctClass.getMethod("loadOntologyInternal", "(Ljava/net/URI;)Ljava/util/Optional;"); // throws NotFoundException if method does not exist

				CtClass declaringClass = ctMethod.getDeclaringClass();

				if (declaringClass != ctClass) {
					ctMethod = CtNewMethod.copy(ctMethod, ctClass, null);
					ctClass.addMethod(ctMethod);
				}

				ctMethod.insertAt(103, "de.fuberlin.csw.aspectowl.owlapi.protege.AspectOWLEditorKitHook.addAspectOWLParser(loadingManager, modelManager);");

				finalizeClassForWeaving(wovenClass, ctClass);

			} else if (className.equals("org.protege.editor.owl.model.OntologyReloader")) {

				// Each time an ontology is loaded, Protege creates a new OWLOntologyManager. This ontology manager is used
				// for the loading process. After the ontology (and potential imports) are loaded, the ontologies are copied
				// to the main ontology manager (the one stored in the single OWLModelManager instance). Then, the loading
				// OWLOntologyManger is discarded. Anyway, we need to add our ParserFactory to each loading ontology manager.

				CtClass ctClass = prepareClassForWeaving(wovenClass);

				CtMethod ctMethod = ctClass.getMethod("performReloadAndGetPatch", "()Ljava/util/List;"); // throws NotFoundException if method does not exist

				CtClass declaringClass = ctMethod.getDeclaringClass();

				if (declaringClass != ctClass) {
					ctMethod = CtNewMethod.copy(ctMethod, ctClass, null);
					ctClass.addMethod(ctMethod);
				}

				ctMethod.insertAt(99, "de.fuberlin.csw.aspectowl.owlapi.protege.AspectOWLEditorKitHook.addAspectOWLParser(reloadingManager, modelManager);");

				finalizeClassForWeaving(wovenClass, ctClass);

			} else if (className.equals("org.protege.editor.owl.ui.OntologyFormatPanel")) {

				CtClass ctClass = prepareClassForWeaving(wovenClass);

				CtConstructor ctConstructor = ctClass.getConstructor("()V");
				ctConstructor.insertAt(39, "de.fuberlin.csw.aspectowl.owlapi.protege.AspectOWLEditorKitHook.addOntologyFormat(formats);");

				CtMethod ctMethod = ctClass.getMethod("isFormatOk", "(Lorg/protege/editor/owl/OWLEditorKit;Lorg/semanticweb/owlapi/model/OWLDocumentFormat;)Z"); // throws NotFoundException if method does not exist
				ctMethod.insertBefore("if (!(de.fuberlin.csw.aspectowl.owlapi.protege.AspectOWLEditorKitHook.alternativeFormatIfAspectOriented(format, editorKit))) return false;");

				finalizeClassForWeaving(wovenClass, ctClass);

			} else if (className.equals("org.protege.editor.owl.ui.view.dataproperty.OWLDataPropertyCharacteristicsViewComponent")) {

				CtClass ctClass = prepareClassForWeaving(wovenClass);

				// TODO ?
				CtMethod ctMethod = ctClass.getDeclaredMethod("initialiseView");

//				ctMethod.insertAfter();

				finalizeClassForWeaving(wovenClass, ctClass);

			} else if (className.equals("org.protege.editor.owl.model.OWLWorkspace")) {

				CtClass ctClass = prepareClassForWeaving(wovenClass);

				CtField ctIconProviderField = ctClass.getField("iconProvider");
				int modifiers = ctIconProviderField.getModifiers();
				int notFinalModifier = Modifier.clear(modifiers, Modifier.FINAL);
				ctIconProviderField.setModifiers(notFinalModifier);

				finalizeClassForWeaving(wovenClass, ctClass);

			} else if (className.equals("org.protege.editor.owl.model.merge.MergeEntitiesChangeListGenerator")) {

				CtClass ctClass = prepareClassForWeaving(wovenClass);

				CtMethod ctMethod = ctClass.getMethod("replaceUsage", "(Lcom/google/common/collect/ImmutableList/Builder<Lorg/semanticweb/owlapi/model/OWLOntologyChange;>;)V");
				ctMethod.insertAt(96, "renamer = new de.fuberlin.csw.aspectowl.protege.util.AspectOWLEntityRenamer(rootOntology.getOWLOntologyManager(), rootOntology.getImportsClosure());");

				finalizeClassForWeaving(wovenClass, ctClass);

			} else if (className.equals("org.protege.editor.owl.ui.action.RenameEntityAction")) {

				CtClass ctClass = prepareClassForWeaving(wovenClass);

				CtMethod ctMethod = ctClass.getMethod("actionPerformed", "(Lorg/semanticweb/owlapi/model/OWLEntity;)V");
				ctMethod.insertAt(22, "owlEntityRenamer = new de.fuberlin.csw.aspectowl.protege.util.AspectOWLEntityRenamer(this.getOWLModelManager().getOWLOntologyManager(), this.getOWLModelManager().getOntologies());");

				finalizeClassForWeaving(wovenClass, ctClass);

			} else if (className.equals("org.protege.editor.owl.model.refactor.ontology.EntityIRIUpdaterOntologyChangeStrategy")) {

				CtClass ctClass = prepareClassForWeaving(wovenClass);

				CtMethod ctMethod = ctClass.getMethod("getChangesForRename", "(Lorg/semanticweb/owlapi/model/OWLOntology;Lorg/semanticweb/owlapi/model/OWLOntologyID;Lorg/semanticweb/owlapi/model/OWLOntologyID;)Ljava/util/List<Lorg/semanticweb/owlapi/model/OWLOntologyChange;>;");
				ctMethod.insertAt(23, "entityRenamer = new de.fuberlin.csw.aspectowl.protege.util.AspectOWLEntityRenamer(ontology.getOWLOntologyManager(), Collections.singleton(ontology));");

				finalizeClassForWeaving(wovenClass, ctClass);

			} else if (className.equals("org.protege.editor.owl.ui.rename.RenameEntitiesPanel")) {

				CtClass ctClass = prepareClassForWeaving(wovenClass);

				CtMethod ctMethod = ctClass.getMethod("getChanges", "()Ljava/util/List<Lorg/semanticweb/owlapi/model/OWLOntologyChange;>;");
				ctMethod.insertAt(179, "renamer = new de.fuberlin.csw.aspectowl.protege.util.AspectOWLEntityRenamer(mngr, getOntologies());");

				finalizeClassForWeaving(wovenClass, ctClass);

			} else if (className.equals("org.semanticweb.owlapi.util.OWLEntityRenamer")) {
				CtClass ctClass = prepareClassForWeaving(wovenClass);

				CtMethod getOMMethod = CtMethod.make("public org.semanticweb.owlapi.model.OWLOntologyManager getOWLOntologyManager() {return this.owlOntologyManager;}", ctClass);
				ctClass.addMethod(getOMMethod);
				CtMethod getOntologiesMethod = CtMethod.make("public java.util.Set<org.semanticweb.owlapi.model.OWLOntology> getOntologies() {return this.ontologies;}", ctClass);
				ctClass.addMethod(getOntologiesMethod);
				finalizeClassForWeaving(wovenClass, ctClass);

			}

//		} else if (className.equals("org.semanticweb.owlapi.model.AxiomType$1")) {
//			try {
//				BundleWiring bundleWiring = wovenClass.getBundleWiring();
//				byte[] axiomTypesClassBytes = IOUtils.toByteArray(bundleWiring.getClassLoader().getResourceAsStream("org/semanticweb/owlapi/model/AxiomType.class"));
//				AspectOWLPostClassLoadingWeavingHelper.reweaveAxiomTypeClass(this, "org.semanticweb.owlapi.model.AxiomType", bundleWiring, axiomTypesClassBytes);
//			} catch (ClassNotFoundException e) {
//				e.printStackTrace();
//			} catch (IOException e) {
//				e.printStackTrace();
//			} catch (Exception e) {
//				e.printStackTrace();
//			}

//		} else if (OWLObject.class.isAssignableFrom(wovenClass.getDefinedClass())) {
//			System.out.println("OWLObject subclass: " + className);
//			// do proxying
//		}
		} catch (Throwable t) {
			log.error("Weaving failed for class {}: {}.", className, t.getMessage());
			t.printStackTrace();
		}

	}

	private CtClass prepareClassForWeaving(WovenClass wovenClass) throws NotFoundException {
		String className = wovenClass.getClassName();
		log.debug("Weaving class: {}, Classloader: {}", className, wovenClass.getBundleWiring().getClassLoader().getClass().getName());
		ClassPool pool = ClassPool.getDefault();
		pool.appendSystemPath();
		pool.appendClassPath(new ClassClassPath(AspectOWLEditorKitHook.class));
		pool.insertClassPath(new ByteArrayClassPath(wovenClass.getClassName(), wovenClass.getBytes()));
		return pool.getCtClass(className);
	}

	private void finalizeClassForWeaving(WovenClass wovenClass, CtClass ctClass) throws IOException, CannotCompileException {
		byte[] bytes = ctClass.toBytecode();
		ctClass.detach();
		wovenClass.setBytes(bytes);
		wovenClass.getDynamicImports().add("de.fuberlin.csw.aspectowl.owlapi.protege");
	}

//	private static final OWLAspect testAspect = new OWLNamedAspectImpl(IRI.create("http://www.example.org/aspectowl/FunnyAspect"));

	/**
	 * @param original
	 * @return
	 */
	@SuppressWarnings("unused")
	public static List<MListButton> getButtonsWithAspectButton(List<MListButton> original, OWLFrameSectionRow frameSectionRow, OWLEditorKit editorKit) {

		for(MListButton button : original) {
			if (button instanceof AspectButton) {
				// sometimes classes get woven multiple times, make sure not to add another aspect button
				return original;
			}
		}

        List<MListButton> additionalButtons = new ArrayList<>(original); // original may be an immutable list, so we need to create a mutable clone

		OWLAxiom axiom = frameSectionRow.getAxiom();
		OWLOntology ontology = frameSectionRow.getOntology();

		OWLOntologyAspectManager aspectManager = getAspectManager(editorKit);

		AspectButton button = new AspectButton(axiom, ontology, aspectManager);
		button.setActionListener(e -> {

			//			if (!am.hasAssertedAspects(axiom)) {
//                am.addAspect(testAspect, axiom); // for testing
//            } else {
//            	am.removeAssertedAspect(axiom, testAspect);
//			}

			AspectAssertionPanel aspectAssertionPanel = new AspectAssertionPanel(editorKit);
			aspectAssertionPanel.setAxiom(new OWLAxiomInstance(axiom, ontology, aspectManager));
			new UIHelper(editorKit).showDialog("Aspects for " + axiom.getAxiomType().toString() + " axiom", aspectAssertionPanel, JOptionPane.CLOSED_OPTION);
			aspectAssertionPanel.dispose();


//			editorKit.getModelManager().fireEvent(EventType.ACTIVE_ONTOLOGY_CHANGED);
		});
		additionalButtons.add(button);

		return additionalButtons;
	}

	@SuppressWarnings("unused")
	public static void addAspectOWLParser(OWLOntologyManager man, OWLModelManager modelManager) {
		man.getOntologyParsers().add(new AspectOrientedOWLFunctionalSyntaxParserFactory(getAspectManager(modelManager)));
//		man.addOntologyChangeListener(aspectManagers.get(modelManager));
	}

	@SuppressWarnings("unused")
	public static void addOntologyFormat(List<OWLDocumentFormat> formats) {
		for (OWLDocumentFormat format : formats) {
			if (format instanceof AspectOrientedFunctionalSyntaxDocumentFormat) {
				return;
			}
		}
		formats.add(new AspectOrientedFunctionalSyntaxDocumentFormat());
	}

	@SuppressWarnings("unused")
	public static boolean alternativeFormatIfAspectOriented(OWLDocumentFormat format, OWLEditorKit editorKit) {
		if (format instanceof AspectOrientedFunctionalSyntaxDocumentFormat
				|| !(getAspectManager(editorKit).hasAspects(editorKit.getModelManager().getActiveOntology()))) {
			return true;
		}

		int userSaysOk = JOptionPane.showConfirmDialog(editorKit.getOWLWorkspace(),
				String.format("The ontology contains aspects. The '%s' format loses all information about aspects.  We highly recommend to use the 'OWL Functional Syntax with Aspect-Oriented Extensions' format.  Continue anyway (you will lose information)?", format),
				"Warning",
				JOptionPane.YES_NO_OPTION);
		return userSaysOk == JOptionPane.YES_OPTION;
	}

}
