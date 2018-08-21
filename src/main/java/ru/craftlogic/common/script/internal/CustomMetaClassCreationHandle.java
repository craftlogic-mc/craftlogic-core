package ru.craftlogic.common.script.internal;

import groovy.lang.*;
import org.codehaus.groovy.reflection.CachedClass;
import org.codehaus.groovy.reflection.CachedField;
import org.codehaus.groovy.reflection.CachedMethod;
import org.codehaus.groovy.runtime.GeneratedClosure;
import org.codehaus.groovy.runtime.MetaClassHelper;
import org.codehaus.groovy.runtime.StringGroovyMethods;
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

    private final boolean obfuscated;

    public CustomMetaClassCreationHandle(boolean obfuscated) {
        this.obfuscated = obfuscated;
    }

    @Override
    protected MetaClass createNormalMetaClass(Class theClass, MetaClassRegistry registry) {
        if (GeneratedClosure.class.isAssignableFrom(theClass)) {
            return super.createNormalMetaClass(theClass, registry);
        } else {
            return new MetaClassImpl(registry, theClass) {
                Index classPropertyIndex = getClassPropertyIndex(this);

                @Override
                public MetaMethod getMethodWithCaching(Class sender, String name, Object[] args, boolean isCallToSuper) {
                    MetaMethod mm = super.getMethodWithCaching(sender, name, args, isCallToSuper);
                    if (mm == null && obfuscated) {
                        Class targetClass = getTheClass();
                        int argCount = args.length;
                        String obfName = DeobfManager.obfMethodName(targetClass, name, argCount);
                        if (obfName != null && !obfName.equals(name)) {
                            mm = super.getMethodWithCaching(sender, obfName, args, isCallToSuper);
                            if (mm == null) {
                                String notchName = DeobfManager.notchMethodName(targetClass, obfName, argCount);
                                if (notchName != null && !notchName.equals(obfName)) {
                                    mm = super.getMethodWithCaching(sender, notchName, args, isCallToSuper);
                                }
                            }
                        }
                    }
                    return mm;
                }

                @Override
                public CallSite createPojoCallSite(CallSite site, Object receiver, Object[] args) { //TODO
                    CallSite cs = super.createPojoCallSite(site, receiver, args);
                    if (cs instanceof PojoMetaClassSite && obfuscated) {
                        Class targetClass = getTheClass();
                        int argCount = args.length;
                        String name = site.getName();
                        String obfName = DeobfManager.obfMethodName(targetClass, name, argCount);
                        if (obfName != null && !obfName.equals(name)) {
                            Class[] params = MetaClassHelper.convertToTypeArray(args);
                            CachedMethod cm = (CachedMethod) getMetaMethod(obfName, params);
                            if (cm != null) {
                                return PojoMetaMethodSite.createCachedMethodSite(site, this, cm, params, args);
                            } else {
                                String notchName = DeobfManager.notchMethodName(targetClass, obfName, argCount);
                                if (notchName != null && !notchName.equals(obfName)) {
                                    cm = (CachedMethod) getMetaMethod(notchName, params);
                                    if (cm != null) {
                                        return PojoMetaMethodSite.createCachedMethodSite(site, this, cm, params, args);
                                    }
                                }
                            }
                        }
                    }

                    return cs;
                }

                @Override
                public CallSite createStaticSite(CallSite site, Object[] args) {
                    CallSite cs = super.createStaticSite(site, args);
                    if (cs instanceof StaticMetaClassSite && obfuscated) {
                        Class targetClass = getTheClass();
                        int argCount = args.length;
                        String name = site.getName();
                        String obfName = DeobfManager.obfMethodName(targetClass, name, argCount);
                        if (obfName != null && !obfName.equals(name)) {
                            Class[] params = MetaClassHelper.convertToTypeArray(args);
                            CachedMethod cm = (CachedMethod) getMetaMethod(obfName, params);
                            if (cm != null) {
                                return StaticMetaMethodSite.createStaticMetaMethodSite(site, this, cm, params, args);
                            } else {
                                String notchName = DeobfManager.notchMethodName(targetClass, obfName, argCount);
                                if (notchName != null && !notchName.equals(obfName)) {
                                    cm = (CachedMethod) getMetaMethod(notchName, params);
                                    if (cm != null) {
                                        return StaticMetaMethodSite.createStaticMetaMethodSite(site, this, cm, params, args);
                                    }
                                }
                            }
                        }
                    }

                    return cs;
                }

                @Override
                public Object getProperty(Class sender, Object object, String name, boolean useSuper, boolean fromInsideClass) {
                    if (obfuscated) {
                        Class targetClass = object instanceof Class ? (Class) object : object.getClass();
                        String obfName = DeobfManager.obfFieldName(targetClass, name);
                        if (obfName != null && !obfName.equals(name)) {
                            try {
                                return super.getProperty(sender, object, obfName, useSuper, fromInsideClass);
                            } catch (MissingPropertyException e) {
                                String notchName = DeobfManager.notchFieldName(targetClass, obfName);
                                if (notchName != null && !notchName.equals(obfName)) {
                                    try {
                                        return super.getProperty(sender, object, notchName, useSuper, fromInsideClass);
                                    } catch (MissingPropertyException ignored) {
                                        String cap = StringGroovyMethods.capitalize((CharSequence)name);
                                        MetaMethod getter = getMethodWithCaching(sender, "get" + cap, EMPTY_ARGUMENTS, useSuper);
                                        if (getter == null) {
                                            getter = getMethodWithCaching(sender, "is" + cap, EMPTY_ARGUMENTS, useSuper);
                                            System.out.println("Getter get" + cap + " not found");
                                        }
                                        if (getter != null) {
                                            MetaMethod g = getter;
                                            Class t = getter.getName().startsWith("is") ? Boolean.class : Object.class;
                                            return new MetaProperty(name, t) {
                                                @Override
                                                public Object getProperty(Object object) {
                                                    return g.doMethodInvoke(object, EMPTY_ARGUMENTS);
                                                }

                                                @Override
                                                public void setProperty(Object object, Object newValue) {
                                                    throw new UnsupportedOperationException();
                                                }
                                            };
                                        } else {
                                            System.out.println("Getter is" + cap + " not found");
                                        }
                                    }
                                }
                            }
                        }
                    }
                    return super.getProperty(sender, object, name, useSuper, fromInsideClass);
                }

                @Override
                public MetaProperty getEffectiveGetMetaProperty(Class sender, Object object, String name, boolean useSuper) {
                    if (obfuscated) {
                        Class targetClass = object instanceof Class ? (Class) object : object.getClass();
                        String obfName = DeobfManager.obfFieldName(targetClass, name);
                        if (obfName != null && !obfName.equals(name)) {
                            try {
                                return super.getEffectiveGetMetaProperty(sender, object, obfName, useSuper);
                            } catch (MissingPropertyException e) {
                                String notchName = DeobfManager.notchFieldName(targetClass, obfName);
                                if (notchName != null && !notchName.equals(obfName)) {
                                    try {
                                        return super.getEffectiveGetMetaProperty(sender, object, notchName, useSuper);
                                    } catch (MissingPropertyException ignored) {
                                        String cap = StringGroovyMethods.capitalize((CharSequence)name);
                                        MetaMethod getter = getMethodWithCaching(sender, "get" + cap, EMPTY_ARGUMENTS, useSuper);
                                        if (getter == null) {
                                            getter = getMethodWithCaching(sender, "is" + cap, EMPTY_ARGUMENTS, useSuper);
                                            System.out.println("Getter get" + cap + " not found (eff)");
                                        }
                                        if (getter != null) {
                                            MetaMethod g = getter;
                                            Class t = getter.getName().startsWith("is") ? Boolean.class : Object.class;
                                            return new MetaProperty(name, t) {
                                                @Override
                                                public Object getProperty(Object object) {
                                                    return g.doMethodInvoke(object, EMPTY_ARGUMENTS);
                                                }

                                                @Override
                                                public void setProperty(Object object, Object newValue) {
                                                    throw new UnsupportedOperationException();
                                                }
                                            };
                                        } else {
                                            System.out.println("Getter is" + cap + " not found (eff)");
                                        }
                                    }
                                }
                            }
                        }
                    }
                    return super.getEffectiveGetMetaProperty(sender, object, name, useSuper);
                }

                @Override
                public void setProperty(Class sender, Object object, String name, Object newValue, boolean useSuper, boolean fromInsideClass) {
                    if (obfuscated) {
                        Class targetClass = object instanceof Class ? (Class) object : object.getClass();
                        String obfName = DeobfManager.obfFieldName(targetClass, name);
                        if (obfName != null && !obfName.equals(name)) {
                            try {
                                super.setProperty(sender, object, obfName, newValue, useSuper, fromInsideClass);
                                return;
                            } catch (MissingPropertyException e) {
                                String notchName = DeobfManager.notchFieldName(targetClass, obfName);
                                if (notchName != null && !notchName.equals(obfName)) {
                                    try {
                                        super.setProperty(sender, object, notchName, newValue, useSuper, fromInsideClass);
                                        return;
                                    } catch (MissingPropertyException ignored) {
                                        String cap = StringGroovyMethods.capitalize((CharSequence)name);
                                        MetaMethod setter = getMethodWithCaching(sender, "set" + cap, new Object[]{newValue}, useSuper);
                                        if (setter != null) {
                                            setter.doMethodInvoke(object, new Object[]{newValue});
                                            return;
                                        }
                                    }
                                }
                            }
                        }
                    }
                    super.setProperty(sender, object, name, newValue, useSuper, fromInsideClass);
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
        }
    }
}
