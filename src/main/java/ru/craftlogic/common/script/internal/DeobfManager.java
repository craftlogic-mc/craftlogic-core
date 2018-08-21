package ru.craftlogic.common.script.internal;

import net.minecraftforge.fml.common.asm.transformers.deobf.FMLDeobfuscatingRemapper;
import org.objectweb.asm.Type;
import ru.craftlogic.api.util.Pair;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.util.HashMap;
import java.util.Map;

public class DeobfManager {

    private static boolean loaded;

    public static Map<String, String> fieldMaps = new HashMap<>();
    public static Map<String, String> methodMaps = new HashMap<>();

    private static Map<String, Map<String, String>> cachedFieldMaps = new HashMap<>();
    private static Map<String, Map<Pair<String, Integer>, String>> cachedMethodMaps = new HashMap<>();

    private static Map<String, Map<String, String>> rawFieldMaps;
    private static Map<String, Map<String, String>> rawMethodMaps;


    private static void checkLoaded() {
        if (!loaded) {
            try {
                Field rawFieldMapsField = FMLDeobfuscatingRemapper.class.getDeclaredField("rawFieldMaps");
                Field rawMethodMapsField = FMLDeobfuscatingRemapper.class.getDeclaredField("rawMethodMaps");
                rawFieldMapsField.setAccessible(true);
                rawMethodMapsField.setAccessible(true);
                rawFieldMaps = (Map<String, Map<String, String>>) rawFieldMapsField.get(FMLDeobfuscatingRemapper.INSTANCE);
                rawMethodMaps = (Map<String, Map<String, String>>) rawMethodMapsField.get(FMLDeobfuscatingRemapper.INSTANCE);

                loadMappings("fields.csv", "field_", fieldMaps);
                loadMappings("methods.csv", "func_", methodMaps);

                loaded = true;
            } catch (Exception e) {
                throw new RuntimeException(e);
            }
        }
    }

    private static void loadMappings(String from, String prefix, Map<String, String> target) throws IOException {
        try (InputStream is = DeobfManager.class.getResourceAsStream("/assets/craftlogic/mappings/" + from);
             BufferedReader reader = new BufferedReader(new InputStreamReader(is))) {

            String line;
            while ((line = reader.readLine()) != null) {
                String[] data = line.split(",");
                if (data.length >= 2) {
                    String key = data[0];
                    String value = data[1];
                    if (key.startsWith(prefix)) {
                        target.put(key, value);
                    }
                }
            }
        }
    }

    public static String obfMethodName(Class<?> cls, String name, int argCount) {
        checkLoaded();
        if (cls == Object.class || cls == null)
            return null;
        String internalClassName = Type.getInternalName(cls);
        Map<Pair<String, Integer>, String> map = cachedMethodMaps.get(internalClassName);
        String ret = map == null ? null : map.get(Pair.of(name, argCount));
        if (ret != null)
            return ret;
        ret = obfMethodName(cls.getSuperclass(), name, argCount);
        if (ret != null)
            return ret;
        for (Class<?> iface : cls.getInterfaces()) {
            ret = obfMethodName(iface, name, argCount);
            if (ret != null)
                return ret;
        }
        for (Method m : cls.getDeclaredMethods()) {
            if (m.getName().startsWith("func_") && m.getParameterCount() == argCount) { //FIXME: OPTIMIZE THIS
                String deobfName = methodMaps.get(m.getName());
                if (deobfName.equals(name)) {
                    cachedMethodMaps.computeIfAbsent(internalClassName, k -> new HashMap<>())
                                    .put(Pair.of(deobfName, argCount), m.getName());
                    return m.getName();
                }
             }
        }
        return null;
    }

    public static String notchMethodName(Class<?> cls, String name, int argCount) {
        checkLoaded();
        if (cls == Object.class || cls == null)
            return null;
        Map<String, String> map = rawMethodMaps.get(Type.getInternalName(cls));
        String ret = map == null ? null : map.get(name);
        if (ret != null)
            return ret;
        ret = notchMethodName(cls.getSuperclass(), name, argCount);
        if (ret != null)
            return ret;
        for (Class<?> iface : cls.getInterfaces()) {
            ret = notchMethodName(iface, name, argCount);
            if (ret != null)
                return ret;
        }
        return null;
    }

    public static String obfFieldName(Class<?> cls, String name) {
        checkLoaded();
        String ret = null;
        while (ret == null && cls != Object.class) {
            String internalClassName = Type.getInternalName(cls);
            Map<String, String> map = cachedFieldMaps.get(internalClassName);
            ret = map == null ? null : map.get(name);
            if (ret == null) {
                for (Field f : cls.getDeclaredFields()) {
                    if (f.getName().startsWith("field_")) { //FIXME: OPTIMIZE THIS
                        String deobfName = fieldMaps.get(f.getName());
                        if (deobfName.equals(name)) {
                            cachedFieldMaps.computeIfAbsent(internalClassName, k -> new HashMap<>())
                                           .put(deobfName, f.getName());
                            ret = f.getName();
                        }
                    }
                }
            }
            cls = cls.getSuperclass();
        }
        return ret;
    }

    public static String notchFieldName(Class<?> cls, String name) {
        checkLoaded();
        String ret = null;
        while (ret == null && cls != Object.class) {
            Map<String, String> map = rawFieldMaps.get(Type.getInternalName(cls));
            ret = map == null ? null : map.get(name);
            cls = cls.getSuperclass();
        }
        return ret;
    }
}
