package ru.craftlogic.coremod.asm;

import net.minecraft.launchwrapper.IClassTransformer;
import org.objectweb.asm.ClassReader;
import org.objectweb.asm.ClassWriter;
import org.objectweb.asm.Opcodes;
import org.objectweb.asm.tree.ClassNode;
import org.objectweb.asm.tree.MethodNode;
import ru.craftlogic.util.ReflectiveUsage;

@ReflectiveUsage
public class TransformerEntityPlayerMP implements IClassTransformer, Opcodes {
    @Override
    public byte[] transform(String name, String transformedName, byte[] basicClass) {
        try {
            switch (name) {
                case "net.minecraft.entity.player.EntityPlayerMP": {
                    ClassNode classNode = new ClassNode();
                    ClassReader classReader = new ClassReader(basicClass);
                    classReader.accept(classNode, 0);
                    for (MethodNode method : classNode.methods) {
                        if (method.name.equals("sendWindowProperty") && method.desc.equals("(Lnet/minecraft/inventory/Container;II)V")) {
                            method.instructions.clear();
                            method.visitVarInsn(ALOAD, 0);
                            method.visitVarInsn(ALOAD, 1);
                            method.visitVarInsn(ILOAD, 2);
                            method.visitVarInsn(ILOAD, 3);
                            method.visitMethodInsn(INVOKESTATIC, "ru/craftlogic/util/CraftLogicHooks", "sendWindowProperty", "(Lnet/minecraft/entity/player/EntityPlayerMP;Lnet/minecraft/inventory/Container;II)V", false);
                            method.visitInsn(RETURN);
                        } else if (method.name.equals("sendAllWindowProperties") && method.desc.equals("(Lnet/minecraft/inventory/Container;Lnet/minecraft/inventory/IInventory;)V")) {
                            method.instructions.clear();
                            method.visitVarInsn(ALOAD, 0);
                            method.visitVarInsn(ALOAD, 1);
                            method.visitVarInsn(ALOAD, 2);
                            method.visitMethodInsn(INVOKESTATIC, "ru/craftlogic/util/CraftLogicHooks", "sendAllWindowProperties", "(Lnet/minecraft/entity/player/EntityPlayerMP;Lnet/minecraft/inventory/Container;Lnet/minecraft/inventory/IInventory;)V", false);
                            method.visitInsn(RETURN);
                            break;
                        }
                    }
                    ClassWriter classWriter = new ClassWriter(ClassWriter.COMPUTE_MAXS | ClassWriter.COMPUTE_FRAMES);
                    classNode.accept(classWriter);
                    return classWriter.toByteArray();
                }
            }
        } catch (Throwable thr) {
            thr.printStackTrace();
        }
        return basicClass;
    }
}
