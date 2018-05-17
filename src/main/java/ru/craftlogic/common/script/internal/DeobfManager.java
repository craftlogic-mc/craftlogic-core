package ru.craftlogic.common.script.internal;

import net.minecraftforge.fml.common.asm.transformers.deobf.FMLDeobfuscatingRemapper;
import org.objectweb.asm.Type;

import java.lang.reflect.Field;
import java.util.HashMap;
import java.util.Map;

public class DeobfManager {

    private static boolean loaded;

    private static Map<String, Map<String, String>> rawFieldMaps = new HashMap<>();
    private static Map<String, Map<String, String>> rawMethodMaps = new HashMap<>();

    private static void checkLoaded() {
        if (!loaded) {
            try {
                Field rawFieldMapsField = FMLDeobfuscatingRemapper.class.getDeclaredField("rawFieldMaps");
                Field rawMethodMapsField = FMLDeobfuscatingRemapper.class.getDeclaredField("rawMethodMaps");
                rawFieldMapsField.setAccessible(true);
                rawMethodMapsField.setAccessible(true);
                rawFieldMaps = (Map<String, Map<String, String>>) rawFieldMapsField.get(FMLDeobfuscatingRemapper.INSTANCE);
                rawMethodMaps = (Map<String, Map<String, String>>) rawMethodMapsField.get(FMLDeobfuscatingRemapper.INSTANCE);

                loaded = true;
            } catch (Exception e) {
                throw new RuntimeException(e);
            }
        }
    }

    public static String mapMethodName(Class<?> cls, String name) {
        checkLoaded();
        if (cls == Object.class || cls == null)
            return null;
        Map<String, String> map = rawMethodMaps.get(Type.getInternalName(cls));
        String ret = map == null ? null : map.get(name);
        if (ret != null)
            return ret;
        ret = mapMethodName(cls.getSuperclass(), name);
        if (ret != null)
            return ret;
        for (Class<?> iface : cls.getInterfaces()) {
            ret = mapMethodName(iface, name);
            if (ret != null)
                return ret;
        }
        return null;
    }

    public static String mapFieldName(Class<?> cls, String name) {
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
