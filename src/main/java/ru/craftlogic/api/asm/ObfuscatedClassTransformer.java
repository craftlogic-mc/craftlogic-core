package ru.craftlogic.api.asm;

import net.minecraft.launchwrapper.IClassTransformer;
import net.minecraftforge.fml.common.asm.transformers.deobf.FMLDeobfuscatingRemapper;
import org.objectweb.asm.Opcodes;
import org.objectweb.asm.Type;
import org.objectweb.asm.tree.AnnotationNode;

import java.lang.annotation.Annotation;
import java.lang.annotation.Repeatable;
import java.util.*;

public interface ObfuscatedClassTransformer extends IClassTransformer, Opcodes {
    static String deobfClass(String name) {
        return FMLDeobfuscatingRemapper.INSTANCE.map(name).replace(".", "/");
    }

    static String obfClass(String name) {
        return FMLDeobfuscatingRemapper.INSTANCE.unmap(name).replace(".", "/");
    }

    static String deobfField(String owner, String name, String desc) {
        return FMLDeobfuscatingRemapper.INSTANCE.mapFieldName(owner, name, desc);
    }

    static String deobfMethod(String owner, String name, String desc) {
        return FMLDeobfuscatingRemapper.INSTANCE.mapMethodName(owner, name, desc);
    }

    static Map<String, Object> getAnnotationParameters(AnnotationNode annotation) {
        Map<String, Object> parameters = new HashMap<>();
        List<Object> values = annotation.values;
        for (int i = 0; i < values.size() / 2; i++) {
            parameters.put((String) values.get(i), values.get(i+1));
        }
        return parameters;
    }

    static List<AnnotationNode> getAnnotationsByType(List<AnnotationNode> annotations, Class<? extends Annotation> annotation) {
        Repeatable repeatable = annotation.getAnnotation(Repeatable.class);
        return getAnnotationsByType(annotations, Type.getInternalName(annotation), repeatable == null ? null : Type.getInternalName(repeatable.value()));
    }

    static List<AnnotationNode> getAnnotationsByType(List<AnnotationNode> annotations,
                                                               String annotationTypeInternalName, String repeatedTypeInternalName) {
        if (annotations == null)
            return Collections.emptyList();
        List<AnnotationNode> result = new ArrayList<>();
        for (AnnotationNode node : annotations) {
            String nodeInternalName = Type.getType(node.desc).getInternalName();
            if (Objects.equals(nodeInternalName, annotationTypeInternalName)) {
                result.add(node);
            }
            if (Objects.equals(nodeInternalName, repeatedTypeInternalName)) {
                if (node.values != null)
                    for (int i = 0; i < node.values.size() - 1; i += 2) {
                        if (Objects.equals("value", node.values.get(i)))
                            result.add((AnnotationNode) node.values.get(i + 1));
                    }
            }
        }
        return result;
    }
}
