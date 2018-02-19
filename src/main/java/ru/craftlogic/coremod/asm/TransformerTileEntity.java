package ru.craftlogic.coremod.asm;

import net.minecraft.launchwrapper.IClassTransformer;
import org.objectweb.asm.ClassReader;
import org.objectweb.asm.ClassWriter;
import org.objectweb.asm.Opcodes;
import org.objectweb.asm.tree.*;
import ru.craftlogic.util.ReflectiveUsage;

import java.util.ListIterator;

@ReflectiveUsage
public class TransformerTileEntity implements IClassTransformer, Opcodes {
    @Override
    public byte[] transform(String name, String transformedName, byte[] basicClass) {
        try {
            switch (name) {
                case "net.minecraft.tileentity.TileEntity": {
                    ClassNode classNode = new ClassNode();
                    ClassReader reader = new ClassReader(basicClass);
                    reader.accept(classNode, 0);

                    for (MethodNode method : classNode.methods) {
                        if (method.name.equals("create") && method.desc.equals("(Lnet/minecraft/world/World;Lnet/minecraft/nbt/NBTTagCompound;)Lnet/minecraft/tileentity/TileEntity;")) {
                            ListIterator<AbstractInsnNode> iterator = method.instructions.iterator();
                            while (iterator.hasNext()) {
                                AbstractInsnNode node = iterator.next();
                                if (node.getType() == AbstractInsnNode.METHOD_INSN) {
                                    MethodInsnNode mn = (MethodInsnNode)node;
                                    if (mn.getOpcode() == INVOKEVIRTUAL && mn.owner.equals("java/lang/Class") && mn.name.equals("newInstance") && mn.desc.equals("()Ljava/lang/Object;")) {
                                        iterator.remove();
                                        MethodInsnNode replacement = new MethodInsnNode(INVOKESTATIC, "ru/craftlogic/util/CraftLogicHooks", "createTileEntity", "(Ljava/lang/Class;Lnet/minecraft/world/World;Lnet/minecraft/nbt/NBTTagCompound;)Lnet/minecraft/tileentity/TileEntity;", false);
                                        iterator.add(new VarInsnNode(ALOAD, 0));
                                        iterator.add(new VarInsnNode(ALOAD, 1));
                                        iterator.add(replacement);
                                        break;
                                    }
                                }
                            }
                            break;
                        }
                    }

                    ClassWriter writer = new ClassWriter(ClassWriter.COMPUTE_MAXS | ClassWriter.COMPUTE_FRAMES);
                    classNode.accept(writer);
                    return writer.toByteArray();
                }
            }
        } catch (Throwable thr) {
            thr.printStackTrace();
        }
        return basicClass;
    }
}
