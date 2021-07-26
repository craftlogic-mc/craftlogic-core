package ru.craftlogic.mixin.item;

import net.minecraft.block.Block;
import net.minecraft.block.BlockFlower;
import net.minecraft.init.Blocks;
import net.minecraft.item.Item;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.registry.RegistryNamespaced;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Overwrite;
import org.spongepowered.asm.mixin.Shadow;
import ru.craftlogic.CraftConfig;
import ru.craftlogic.common.item.*;

import java.util.Map;

@Mixin(Item.class)
public class MixinItem {
    @Shadow @Final
    public static RegistryNamespaced<ResourceLocation, Item> REGISTRY;
    @Shadow @Final
    private static Map<Block, Item> BLOCK_TO_ITEM;

    /**
     * @author Radviger
     * @reason Custom vanilla items
     */
    @Overwrite
    private static void registerItem(int id, ResourceLocation name, Item item) {
        if (name.getNamespace().equals("minecraft")) {
            switch (name.getPath()) {
                case "bowl": {
                    item = new ItemBowl();
                    break;
                }
                case "mushroom_stew":
                case "rabbit_stew":
                case "beetroot_soup": {
                    item.setCreativeTab(null);
                    break;
                }
                case "milk_bucket": {
                    if (CraftConfig.tweaks.enableMilkBucketTweaks) {
                        item = new ItemMilkBucket().setCreativeTab(null);
                    }
                    break;
                }
                case "iron_door": {
                    if (CraftConfig.tweaks.enableHangingDoors) {
                        item = new ItemHangingDoor(Blocks.IRON_DOOR);
                    }
                    break;
                }
                case "wooden_door": {
                    if (CraftConfig.tweaks.enableHangingDoors) {
                        item = new ItemHangingDoor(Blocks.OAK_DOOR);
                    }
                    break;
                }
                case "spruce_door": {
                    if (CraftConfig.tweaks.enableHangingDoors) {
                        item = new ItemHangingDoor(Blocks.SPRUCE_DOOR);
                    }
                    break;
                }
                case "birch_door": {
                    if (CraftConfig.tweaks.enableHangingDoors) {
                        item = new ItemHangingDoor(Blocks.BIRCH_DOOR);
                    }
                    break;
                }
                case "jungle_door": {
                    if (CraftConfig.tweaks.enableHangingDoors) {
                        item = new ItemHangingDoor(Blocks.JUNGLE_DOOR);
                    }
                    break;
                }
                case "acacia_door": {
                    if (CraftConfig.tweaks.enableHangingDoors) {
                        item = new ItemHangingDoor(Blocks.ACACIA_DOOR);
                    }
                    break;
                }
                case "dark_oak_door": {
                    if (CraftConfig.tweaks.enableHangingDoors) {
                        item = new ItemHangingDoor(Blocks.DARK_OAK_DOOR);
                    }
                    break;
                }
            }
        }
        if (item.getRegistryName() == null) {
            item.setRegistryName(name);
        }
        REGISTRY.register(id, name, item);
    }

    /**
     * @author Radviger
     * @reason Custom vanilla items
     */
    @Overwrite
    protected static void registerItemBlock(Block block, Item item) {
        ResourceLocation name = Block.REGISTRY.getNameForObject(block);
        if (name.getNamespace().equals("minecraft")) {
            switch (name.getPath()) {
                case "brown_mushroom":
                case "red_mushroom": {
                    item = new ItemMushroom(block);
                    break;
                }
                case "yellow_flower": {
                    item = new ItemFlower(BlockFlower.EnumFlowerColor.YELLOW);
                    break;
                }
                case "red_flower": {
                    item = new ItemFlower(BlockFlower.EnumFlowerColor.RED);
                    break;
                }
            }
        }
        registerItem(Block.getIdFromBlock(block), name, item);
        BLOCK_TO_ITEM.put(block, item);
    }
}
