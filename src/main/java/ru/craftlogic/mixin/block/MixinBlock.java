package ru.craftlogic.mixin.block;

import net.minecraft.block.Block;
import net.minecraft.block.SoundType;
import net.minecraft.block.material.MapColor;
import net.minecraft.block.material.Material;
import net.minecraft.init.Blocks;
import net.minecraft.item.Item;
import net.minecraft.item.ItemBlock;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.registry.RegistryNamespacedDefaultedByKey;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Overwrite;
import org.spongepowered.asm.mixin.Shadow;
import ru.craftlogic.CraftConfig;
import ru.craftlogic.common.block.*;

import javax.annotation.Nullable;

@Mixin(Block.class)
public class MixinBlock {
    @Shadow @Final
    public static RegistryNamespacedDefaultedByKey<ResourceLocation, Block> REGISTRY;

    @Shadow @Final protected Material material;

    @Shadow @Final protected MapColor blockMapColor;

    @Shadow protected float blockHardness;

    @Shadow protected float blockResistance;

    @Shadow protected SoundType blockSoundType;

    @Shadow private String translationKey;

    /**
     * @author Radviger
     * @reason Custom block features
     */
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
                if (CraftConfig.tweaks.enableFancyGourd) {
                    block = new BlockGourdStem(BlockGourd.GourdVariant.MELON);
                }
                break;
            }
            case "minecraft:pumpkin_stem": {
                if (CraftConfig.tweaks.enableFancyGourd) {
                    block = new BlockGourdStem(BlockGourd.GourdVariant.PUMPKIN);
                }
                break;
            }
            case "minecraft:torch": {
                if (CraftConfig.tweaks.enableTorchBurning) {
                    block = new BlockBurningTorch();
                }
                break;
            }
            case "minecraft:lit_pumpkin": {
                if (CraftConfig.tweaks.enableTorchBurning) {
                    block = new BlockLitPumpkin();
                }
                break;
            }
            case "minecraft:web": {
                block.setHardness(2F);
                break;
            }
            case "minecraft:nether_brick_fence":
            case "minecraft:fence":
            case "minecraft:spruce_fence":
            case "minecraft:birch_fence":
            case "minecraft:jungle_fence":
            case "minecraft:acacia_fence":
            case "minecraft:dark_oak_fence": {
                if (CraftConfig.tweaks.enableDiagonalFences) {
                    MixinBlock old = (MixinBlock) (Object) block;
                    block = new BlockDiagonalFence(old.material, old.blockMapColor);
                    MixinBlock b = (MixinBlock) (Object) block;
                    b.blockHardness = old.blockHardness;
                    b.blockResistance = old.blockResistance;
                    b.blockSoundType = old.blockSoundType;
                    b.translationKey = old.translationKey;
                }
                break;
            }
            case "minecraft:dirt": {
                block.setHardness(3F);
                break;
            }
        }
        block.setRegistryName(name);
        REGISTRY.register(id, name, block);
    }

    /**
     * @author Radviger
     * @reason Custom block features
     */
    @Overwrite
    public static Block getBlockFromItem(@Nullable Item item) {
        if (item instanceof ItemBlock) {
            return ((ItemBlock) item).getBlock();
        } else if (item != null) {
            return Block.REGISTRY.getObject(item.getRegistryName());
        } else {
            return Blocks.AIR;
        }
    }
}
