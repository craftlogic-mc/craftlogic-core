package ru.craftlogic.mixin.block;

import net.minecraft.block.Block;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.registry.RegistryNamespacedDefaultedByKey;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Overwrite;
import org.spongepowered.asm.mixin.Shadow;
import ru.craftlogic.common.block.BlockCobblestone;

@Mixin(Block.class)
public class MixinBlock {
    @Shadow @Final
    public static RegistryNamespacedDefaultedByKey<ResourceLocation, Block> REGISTRY;

    @Overwrite
    private static void registerBlock(int id, ResourceLocation name, Block block) {
        switch (name.toString()) {
            case "minecraft:cobblestone": {
                block = new BlockCobblestone();
                break;
            }
        }
        REGISTRY.register(id, name, block);
    }
}
