package ru.craftlogic.mixin.block;

import net.minecraft.block.Block;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.registry.RegistryNamespacedDefaultedByKey;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Overwrite;
import org.spongepowered.asm.mixin.Shadow;
import ru.craftlogic.common.block.BlockCobblestone;
import ru.craftlogic.common.block.BlockGourd;
import ru.craftlogic.common.block.BlockGourdStem;
import ru.craftlogic.common.block.BlockStoneBrick;

@Mixin(Block.class)
public class MixinBlock {
    @Shadow @Final
    public static RegistryNamespacedDefaultedByKey<ResourceLocation, Block> REGISTRY;

    @Overwrite
    private static void registerBlock(int id, ResourceLocation name, Block block) {
        switch (name.toString()) {
            case "minecraft:cobblestone": {
                block = new BlockCobblestone(false);
                break;
            }
            case "minecraft:mossy_cobblestone": {
                block = new BlockCobblestone(true);
                break;
            }
            case "minecraft:stonebrick": {
                block = new BlockStoneBrick();
                break;
            }
            case "minecraft:melon_stem": {
                block = new BlockGourdStem(BlockGourd.GourdVariant.MELON);
                break;
            }
            case "minecraft:pumpkin_stem": {
                block = new BlockGourdStem(BlockGourd.GourdVariant.PUMPKIN);
                break;
            }
        }
        block.setRegistryName(name);
        REGISTRY.register(id, name, block);
    }
}
