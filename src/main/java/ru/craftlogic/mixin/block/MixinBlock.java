package ru.craftlogic.mixin.block;

import net.minecraft.block.Block;
import net.minecraft.block.SoundType;
import net.minecraft.block.material.MapColor;
import net.minecraft.block.material.Material;
import net.minecraft.block.state.IBlockState;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.init.Blocks;
import net.minecraft.init.MobEffects;
import net.minecraft.item.Item;
import net.minecraft.item.ItemBlock;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.math.BlockPos;
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
            case "minecraft:cobblestone_wall": {
                if (CraftConfig.tweaks.enableDiagonalFences) {
                    block = new BlockDiagonalWall(Block.REGISTRY.getObject(new ResourceLocation("minecraft:cobblestone")));
                }
                break;
            }
            case "minecraft:netherrack":
            case "minecraft:grass":
            case "minecraft:dirt": {
                block.setHardness(3F);
                break;
            }
            case "minecraft:wooden_door": {
                if (CraftConfig.tweaks.enableHangingDoors) {
                    block = new BlockHangingDoor(Material.WOOD)
                        .setHardness(3)
                        .setTranslationKey("doorOak");
                }
                break;
            }
            case "minecraft:spruce_door": {
                if (CraftConfig.tweaks.enableHangingDoors) {
                    block = new BlockHangingDoor(Material.WOOD)
                        .setHardness(3)
                        .setTranslationKey("doorSpruce");
                }
                break;
            }
            case "minecraft:birch_door": {
                if (CraftConfig.tweaks.enableHangingDoors) {
                    block = new BlockHangingDoor(Material.WOOD)
                        .setHardness(3)
                        .setTranslationKey("doorBirch");
                }
                break;
            }
            case "minecraft:jungle_door": {
                if (CraftConfig.tweaks.enableHangingDoors) {
                    block = new BlockHangingDoor(Material.WOOD)
                        .setHardness(3)
                        .setTranslationKey("doorJungle");
                }
                break;
            }
            case "minecraft:acacia_door": {
                if (CraftConfig.tweaks.enableHangingDoors) {
                    block = new BlockHangingDoor(Material.WOOD)
                        .setHardness(3)
                        .setTranslationKey("doorAcacia");
                }
                break;
            }
            case "minecraft:dark_oak_door": {
                if (CraftConfig.tweaks.enableHangingDoors) {
                    block = new BlockHangingDoor(Material.WOOD)
                        .setHardness(3)
                        .setTranslationKey("doorDarkOak");
                }
                break;
            }
            case "minecraft:iron_door": {
                if (CraftConfig.tweaks.enableHangingDoors) {
                    block = new BlockHangingDoor(Material.IRON)
                        .setHardness(5)
                        .setTranslationKey("doorIron");
                }
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

    /**
     * @author Pudo
     * @reason Hide player particle when invisible
     */
    @Overwrite(remap = false)
    public boolean addLandingEffects(IBlockState state, net.minecraft.world.WorldServer worldObj, BlockPos blockPosition, IBlockState iblockstate, EntityLivingBase entity, int numberOfParticles ) {
        return entity instanceof EntityPlayer && entity.getActivePotionEffect(MobEffects.INVISIBILITY) != null;
    }
}
