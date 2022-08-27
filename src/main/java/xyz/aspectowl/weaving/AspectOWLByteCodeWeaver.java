package xyz.aspectowl.weaving;

import javassist.*;
import org.osgi.framework.hooks.weaving.WeavingHook;
import org.osgi.framework.hooks.weaving.WovenClass;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import xyz.aspectowl.protege.AspectOWLEditorKitHook;

import java.io.IOException;

/**
 * @author ralph
 */
public abstract class AspectOWLByteCodeWeaver {

    private static final Logger log = LoggerFactory.getLogger(AspectOWLByteCodeWeaver.class);

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
        wovenClass.getDynamicImports().add("xyz.aspectowl.protege");
    }


}
