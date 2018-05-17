package ru.craftlogic.common.script.internal;

import groovy.lang.*;
import org.codehaus.groovy.reflection.CachedClass;
import org.codehaus.groovy.reflection.CachedField;
import org.codehaus.groovy.reflection.CachedMethod;
import org.codehaus.groovy.runtime.GeneratedClosure;
import org.codehaus.groovy.runtime.MetaClassHelper;
import org.codehaus.groovy.runtime.callsite.*;
import org.codehaus.groovy.runtime.metaclass.MetaMethodIndex;
import org.codehaus.groovy.util.ComplexKeyHashMap;
import org.codehaus.groovy.util.SingleKeyHashMap;

import java.lang.reflect.Field;
import java.lang.reflect.Modifier;
import java.util.LinkedList;

public class CustomMetaClassCreationHandle extends MetaClassRegistry.MetaClassCreationHandle {
    private static final Field F_classPropertyIndex;

    static {
        try {
            F_classPropertyIndex = MetaClassImpl.class.getDeclaredField("classPropertyIndex");
            F_classPropertyIndex.setAccessible(true);
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    private static MetaClassImpl.Index getClassPropertyIndex(MetaClassImpl mc) {
        try {
            return (MetaClassImpl.Index) F_classPropertyIndex.get(mc);
        } catch (IllegalAccessException e) {
            throw new RuntimeException(e);
        }
    }

    private final boolean obfenv;

    public CustomMetaClassCreationHandle(boolean obfenv) {
        this.obfenv = obfenv;
    }

    @Override
    protected MetaClass createNormalMetaClass(Class theClass, MetaClassRegistry registry) {
        if (GeneratedClosure.class.isAssignableFrom(theClass)) {
            return super.createNormalMetaClass(theClass, registry);
        } else {
            MetaClassImpl mc = new MetaClassImpl(registry, theClass) {
                MetaClassImpl.Index classPropertyIndex = getClassPropertyIndex(this);

                @Override
                public MetaMethod getMethodWithCaching(Class sender, String methodName, Object[] arguments, boolean isCallToSuper) {
                    MetaMethod mm = super.getMethodWithCaching(sender, methodName, arguments, isCallToSuper);
                    if (mm == null && obfenv) {
                        String newName = DeobfManager.mapMethodName(getTheClass(), methodName);
                        if (newName != null) {
                            mm = super.getMethodWithCaching(sender, newName, arguments, isCallToSuper);
                        }
                    }
                    return mm;
                }

                @Override
                public CallSite createPojoCallSite(CallSite site, Object receiver, Object[] args) {
                    CallSite cs = super.createPojoCallSite(site, receiver, args);
                    if (cs instanceof PojoMetaClassSite && obfenv) {
                        String newName = DeobfManager.mapMethodName(getTheClass(), site.getName());
                        if (newName != null) {
                            Class[] params = MetaClassHelper.convertToTypeArray(args);
                            CachedMethod cm = (CachedMethod) getMetaMethod(newName, params);
                            if (cm != null)
                                return PojoMetaMethodSite.createCachedMethodSite(site, this, cm, params, args);
                        }
                    }

                    return cs;
                }

                @Override
                public CallSite createStaticSite(CallSite site, Object[] args) {
                    CallSite cs = super.createStaticSite(site, args);
                    if (cs instanceof StaticMetaClassSite && obfenv) {
                        String newName = DeobfManager.mapMethodName(getTheClass(), site.getName());
                        if (newName != null) {
                            Class[] params = MetaClassHelper.convertToTypeArray(args);
                            CachedMethod cm = (CachedMethod) getMetaMethod(newName, params);
                            if (cm != null)
                                return StaticMetaMethodSite.createStaticMetaMethodSite(site, this, cm, params, args);
                        }
                    }

                    return cs;
                }

                @Override
                public Object getProperty(Class sender, Object object, String name, boolean useSuper, boolean fromInsideClass) {
                    String newName = obfenv ? DeobfManager.mapFieldName(object instanceof Class ? (Class) object : object.getClass(), name) : null;
                    return super.getProperty(sender, object, newName != null ? newName : name, useSuper, fromInsideClass);
                }

                @Override
                public MetaProperty getEffectiveGetMetaProperty(Class sender, Object object, String name, boolean useSuper) {
                    String newName = obfenv ? DeobfManager.mapFieldName(object instanceof Class ? (Class) object : object.getClass(), name) : null;
                    return super.getEffectiveGetMetaProperty(sender, object, newName != null ? newName : name, useSuper);
                }

                @Override
                public void setProperty(Class sender, Object object, String name, Object newValue, boolean useSuper, boolean fromInsideClass) {
                    String newName = obfenv ? DeobfManager.mapFieldName(object instanceof Class ? (Class) object : object.getClass(), name) : null;
                    super.setProperty(sender, object, newName != null ? newName : name, newValue, useSuper, fromInsideClass);
                }

                /* Allow access super private methods and fields */
                @Override
                public synchronized void initialize() {
                    if (!isInitialized()) {
                        super.initialize();

                        LinkedList<CachedClass> superc = getSuperClasses();
                        MetaMethodIndex.Header header = metaMethodIndex.getHeader(theClass);
                        for (CachedClass c : superc)
                            for (CachedMethod metaMethod : c.getMethods())
                                if (metaMethod.isPrivate())
                                    addMetaMethodToIndex(metaMethod, header);
                        inheritPrivateFields(superc);
                    }
                }

                private void inheritPrivateFields(LinkedList<CachedClass> superClasses) {
                    SingleKeyHashMap last = null;
                    for (CachedClass klass : superClasses) {
                        SingleKeyHashMap propertyIndex = classPropertyIndex.getNotNull(klass);
                        if (last != null) {
                            copyPrivateFields(last, propertyIndex);
                        }
                        last = propertyIndex;
                    }
                }

                private void copyPrivateFields(SingleKeyHashMap from, SingleKeyHashMap to) {
                    for (ComplexKeyHashMap.EntryIterator iter = from.getEntrySetIterator(); iter.hasNext(); ) {
                        SingleKeyHashMap.Entry entry = (SingleKeyHashMap.Entry) iter.next();
                        MetaProperty mfp = (MetaProperty) entry.getValue();
                        if (mfp instanceof CachedField && Modifier.isPrivate(mfp.getModifiers()))
                            to.put(entry.getKey(), mfp);
                    }
                }
            };

            return mc;
        }
    }
}
