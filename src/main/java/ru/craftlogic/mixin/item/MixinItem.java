package ru.craftlogic.mixin.item;

import net.minecraft.block.Block;
import net.minecraft.item.Item;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.registry.RegistryNamespaced;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Overwrite;
import org.spongepowered.asm.mixin.Shadow;
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
        switch (name.toString()) {
            case "minecraft:bowl": {
                item = new ItemBowl();
                break;
            }
            case "minecraft:mushroom_stew":
            case "minecraft:rabbit_stew":
            case "minecraft:beetroot_soup": {
                item.setCreativeTab(null);
                break;
            }
            case "minecraft:milk_bucket": {
                item = new ItemMilkBucket().setCreativeTab(null);
                break;
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
        switch (name.toString()) {
            case "minecraft:brown_mushroom":
            case "minecraft:red_mushroom": {
                item = new ItemMushroom(block);
                break;
            }
        }
        registerItem(Block.getIdFromBlock(block), name, item);
        BLOCK_TO_ITEM.put(block, item);
    }
}
