package xyz.aspectowl.owlapi.model.impl;

import xyz.aspectowl.owlapi.model.OWLAnonymousAspect;
import xyz.aspectowl.owlapi.model.OWLAspect;
import org.semanticweb.owlapi.model.*;
import org.semanticweb.owlapi.util.OWLObjectTypeIndexProvider;
import uk.ac.manchester.cs.owl.owlapi.OWLAnonymousClassExpressionImpl;

import javax.annotation.Nonnull;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.Arrays;
import java.util.Collections;
import java.util.Objects;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import java.util.stream.Stream;

/**
 * <strike>Is in package uk.ac.manchester.cs.owl.owlapi because it needs to access the index and compareObjectOfSameType
 * methods, which are protected.</strike> Nope. See below
 * 
 * @author ralph
 */
public class OWLAnonymousAspectImpl extends OWLAnonymousClassExpressionImpl implements OWLAnonymousAspect {

    static final OWLObjectTypeIndexProvider OWLOBJECT_TYPEINDEX_PROVIDER = new OWLObjectTypeIndexProvider();

    private static final ConcurrentHashMap<NameParameterTypesTuple, Method> methodCache = new ConcurrentHashMap<>();

    private Method indexMethod;
    private Method compareObjectOfSameTypeMethod;

    private OWLAnonymousClassExpressionImpl ceDelegate;
    private OWLAspectImplDelegate aspectDelegate;

    public OWLAnonymousAspectImpl(OWLAnonymousClassExpression classExpression, Set<OWLAnnotation> annotations, Set<OWLAspect> aspects) {
        this.ceDelegate = (OWLAnonymousClassExpressionImpl) classExpression;
        this.aspectDelegate = new OWLAspectImplDelegate(this, annotations, aspects);


        // We are using the crowbar (aka Java Reflection) in order to make it possible to use the delegate pattern on the
        // OWLAnonymousClassExpressionImpl class, some of whose methods are protected.
        // We look up the methods, make them accessible and call them using the reflection API.
        // We could put this class into the package of its superclass in order to be allowed to call its protected methods,
        // but this runs in an OSGI environment, and the superclass is part of a different bundle, so no luck here
        // (keyword: split package).
        // Note to future self: Please forgive me if you ever need to touch this code again
        Class poorInnocentClass = classExpression.getClass();

        indexMethod = getMethod(poorInnocentClass,"index");
        compareObjectOfSameTypeMethod = getMethod(poorInnocentClass,"compareObjectOfSameType", OWLObject.class);

        Stream.of(indexMethod, compareObjectOfSameTypeMethod).forEach(method -> method.setAccessible(true));

    }

    @Override
    public OWLClassExpression getClassExpression() {
        return ceDelegate;
    }

    @Override
    public Set<OWLObjectProperty> getAccessibilityRelations() {
        return aspectDelegate.getAccessibilityRelations();
    }

    @Override
    public OWLAspect getAspectWithoutAnnotations() {
        return new OWLAnonymousAspectImpl(ceDelegate, Collections.EMPTY_SET, aspectDelegate.getAspects());
    }
    
    /**
     * @see OWLAspect#getAspects()
     */
    @Override
    public Set<OWLAspect> getAspects() {
    	return aspectDelegate.getAspects();
    }

    @Override
    public OWLClassExpression asClassExpression() {
        return ceDelegate;
    }

    @Nonnull
    @Override
    public ClassExpressionType getClassExpressionType() {
        return ceDelegate.getClassExpressionType();
    }

    @Override
    public boolean isClassExpressionLiteral() {
        return ceDelegate.isClassExpressionLiteral();
    }

    @Override
    public void accept(@Nonnull OWLClassExpressionVisitor visitor) {
        ceDelegate.accept(visitor);
    }

    @Nonnull
    @Override
    public <O> O accept(@Nonnull OWLClassExpressionVisitorEx<O> visitor) {
        return ceDelegate.accept(visitor);
    }

    @Override
    protected int index() {
        try {
            return (Integer) indexMethod.invoke(ceDelegate);
        } catch (IllegalAccessException | InvocationTargetException e)  {
            e.printStackTrace();
            return 0;
        }
//        return ceDelegate.index() + 17; // there are 17 types of anonymous class expressions in OWL 2 (index 1 to 17), we assign aspect anonymous CEs indexes from 18 to 35.
    }

    @Override
    protected int compareObjectOfSameType(@Nonnull OWLObject object) {
        try {
            return (Integer)compareObjectOfSameTypeMethod.invoke(ceDelegate, object);
        } catch (IllegalAccessException | InvocationTargetException e)  {
            e.printStackTrace();
            return 0;
        }
    }

    @Override
    public void accept(@Nonnull OWLObjectVisitor visitor) {
        ceDelegate.accept(visitor);
    }

    @Nonnull
    @Override
    public <O> O accept(@Nonnull OWLObjectVisitorEx<O> visitor) {
        return ceDelegate.accept(visitor);
    }

    @Override
    public void addSignatureEntitiesToSet(@Nonnull Set<OWLEntity> entities) {
        ceDelegate.addSignatureEntitiesToSet(entities);
    }

    @Override
    public void addAnonymousIndividualsToSet(@Nonnull Set<OWLAnonymousIndividual> anons) {
        ceDelegate.addAnonymousIndividualsToSet(anons);
    }

    private Method getMethod(Class clazz, String name, Class<?>... parameterTypes) {
        NameParameterTypesTuple key = new NameParameterTypesTuple(clazz, name, parameterTypes);
        Method result = methodCache.get(key);
        if (result == null) {
            result = lookupMethod(clazz, name, parameterTypes);
            if (result != null) {
                methodCache.put(key, result);
                result.setAccessible(true);
            }
        }
        return result;
    }

    private Method lookupMethod(Class clazz, String name, Class<?>... parameterTypes) {
        if (clazz == Object.class)
            return null;

        try {
            Method result = clazz.getDeclaredMethod(name, parameterTypes);
            if (result == null) {
                return lookupMethod(clazz.getSuperclass(), name, parameterTypes);
            }
            return result;
        } catch (NoSuchMethodException e) {
            return lookupMethod(clazz.getSuperclass(), name, parameterTypes);
        } catch (SecurityException e) {
            e.printStackTrace();
            return null;
        }
    }

    @Nonnull
    @Override
    public Set<OWLAnnotation> getAnnotations() {
        return aspectDelegate.getAnnotations();
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof OWLAnonymousAspectImpl)) return false;
        if (!super.equals(o)) return false;
        OWLAnonymousAspectImpl that = (OWLAnonymousAspectImpl) o;
        return Objects.equals(ceDelegate, that.ceDelegate) &&
                Objects.equals(aspectDelegate, that.aspectDelegate);
    }

    @Override
    public int hashCode() {
        return Objects.hash(super.hashCode(), ceDelegate, aspectDelegate);
    }

    private class NameParameterTypesTuple {

        private Class clazz;
        private String name;
        private Class<?>[] parameterTypes;

        public NameParameterTypesTuple(@Nonnull Class<?> clazz, @Nonnull String name, @Nonnull Class<?>[] parameterTypes) {
            this.clazz = clazz;
            this.name = name;
            this.parameterTypes = parameterTypes;
        }

        @Override
        public boolean equals(Object o) {
            if (this == o) return true;
            if (!(o instanceof NameParameterTypesTuple)) return false;
            NameParameterTypesTuple that = (NameParameterTypesTuple) o;
            return Objects.equals(clazz, that.clazz) &&
                    Objects.equals(name, that.name) &&
                    Arrays.equals(parameterTypes, that.parameterTypes);
        }

        @Override
        public int hashCode() {
            int result = Objects.hash(clazz, name);
            result = 31 * result + Arrays.hashCode(parameterTypes);
            return result;
        }
    }
}
