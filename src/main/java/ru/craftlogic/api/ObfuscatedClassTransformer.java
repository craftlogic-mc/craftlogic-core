package ru.craftlogic.api;

import net.minecraft.launchwrapper.IClassTransformer;
import net.minecraftforge.fml.common.asm.transformers.deobf.FMLDeobfuscatingRemapper;
import org.objectweb.asm.Opcodes;

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
}
