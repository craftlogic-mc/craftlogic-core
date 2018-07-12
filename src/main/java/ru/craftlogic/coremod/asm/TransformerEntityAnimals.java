package ru.craftlogic.coremod.asm;

import org.objectweb.asm.ClassReader;
import org.objectweb.asm.ClassWriter;
import org.objectweb.asm.tree.AbstractInsnNode;
import org.objectweb.asm.tree.ClassNode;
import org.objectweb.asm.tree.MethodInsnNode;
import org.objectweb.asm.tree.MethodNode;
import ru.craftlogic.api.ObfuscatedClassTransformer;
import ru.craftlogic.util.ReflectiveUsage;

import java.util.ListIterator;

import static ru.craftlogic.api.ObfuscatedClassTransformer.deobfClass;
import static ru.craftlogic.api.ObfuscatedClassTransformer.obfClass;

@ReflectiveUsage
public class TransformerEntityAnimals implements ObfuscatedClassTransformer {
    @Override
    public byte[] transform(String name, String transformedName, byte[] basicClass) {
        if (basicClass == null) {
            return null;
        }
        switch (transformedName) {
            case "net.minecraft.entity.passive.EntityChicken": {
                System.out.println("Patching EntityChicken");

                ClassReader reader = new ClassReader(basicClass);
                ClassNode classNode = new ClassNode();
                reader.accept(classNode, 0);

                classNode.superName = obfClass("net/minecraft/entity/passive/EntityTameable");

                for (MethodNode method : classNode.methods) {
                    if (method.name.equals("<init>")) {
                        ListIterator<AbstractInsnNode> iterator = method.instructions.iterator();
                        while (iterator.hasNext()) {
                            AbstractInsnNode node = iterator.next();
                            if (node instanceof MethodInsnNode) {
                                MethodInsnNode m = ((MethodInsnNode) node);
                                if (deobfClass(m.owner).equals("net/minecraft/entity/passive/EntityAnimal")
                                        && m.name.equals("<init>") && m.getOpcode() == INVOKESPECIAL) {

                                    m.owner = obfClass("net/minecraft/entity/passive/EntityTameable");
                                    break;
                                }
                            }
                        }
                    }
                }

                ClassWriter writer = new ClassWriter(ClassWriter.COMPUTE_MAXS);
                classNode.accept(writer);

                return writer.toByteArray();
            }
        }
        return basicClass;
    }
}
