package org.apache.felix.framework;

import xyz.aspectowl.owlapi.model.OWLAspectAssertionAxiom;
import xyz.aspectowl.owlapi.protege.AspectOWLEditorKitHook;
import javassist.*;
import org.osgi.framework.hooks.weaving.WeavingHook;
import org.osgi.framework.wiring.BundleWiring;

/**
 * Lives in package org.apache.felix.framework because we need access to the WovenClassImpl constructor, which is
 * package private.
 */
public class AspectOWLPostClassLoadingWeavingHelper {

    public static void reweaveAxiomTypeClass(WeavingHook hook, String className, BundleWiring wiring, byte[] originalBytes) throws Exception {
        WovenClassImpl wovenClass = new WovenClassImpl(className, wiring, originalBytes);

        ClassPool pool = ClassPool.getDefault();
        pool.appendSystemPath();
        pool.appendClassPath(new ClassClassPath(AspectOWLEditorKitHook.class));

        pool.insertClassPath(new ByteArrayClassPath(className, originalBytes));

        try {
            CtClass ctClass = pool.getCtClass(className);
//				CtClass ctClass = pool.get(AxiomType.class.getName());
//				CtClass axiomTypeClass = pool.get( AxiomType.class.getName() );

            CtField defField = new CtField(ctClass, "OWL_ASPECT_ASSERTION_AXIOM_TYPE", ctClass);
            defField.setModifiers( Modifier.STATIC + Modifier.PUBLIC + Modifier.FINAL);

            ctClass.addField( defField, CtField.Initializer.byCall(ctClass, "getInstance", new String[]{OWLAspectAssertionAxiom.class.getName() + ".class", "38", "\"AspectAssertion\"", "true", "true", "true"}) );

            byte[] bytes = ctClass.toBytecode();
            ctClass.detach();
            wovenClass.setBytes(bytes);

            wovenClass.getDynamicImports().add("xyz.aspectowl.owlapi.protege");

            wovenClass.complete(wovenClass.getDefinedClass(), null, null);

        } catch (Throwable t) {
//				System.out.format("Weaving failed for class %s: %s.\n", className, t.getMessage());
        }

    }
}
