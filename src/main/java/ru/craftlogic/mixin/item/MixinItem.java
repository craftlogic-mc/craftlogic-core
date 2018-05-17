package ru.craftlogic.mixin.item;

import net.minecraft.block.Block;
import net.minecraft.item.Item;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.registry.RegistryNamespaced;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Overwrite;
import org.spongepowered.asm.mixin.Shadow;
import ru.craftlogic.common.item.ItemBowl;
import ru.craftlogic.common.item.ItemBrick;
import ru.craftlogic.common.item.ItemString;
import ru.craftlogic.common.item.ItemWool;

import java.util.Map;

@Mixin(Item.class)
public class MixinItem {
    @Shadow @Final
    public static RegistryNamespaced<ResourceLocation, Item> REGISTRY;
    @Shadow @Final
    private static Map<Block, Item> BLOCK_TO_ITEM;

    @Overwrite
    private static void registerItem(int id, ResourceLocation name, Item item) {
        switch (name.toString()) {
            case "minecraft:string": {
                item = new ItemString();
                break;
            }
            case "minecraft:brick": {
                item = new ItemBrick(false);
                break;
            }
            case "minecraft:netherbrick": {
                item = new ItemBrick(true);
                break;
            }
            case "minecraft:bowl": {
                item = new ItemBowl();
                break;
            }
            case "minecraft:milk_bucket":
            case "minecraft:mushroom_stew":
            case "minecraft:rabbit_stew":
            case "minecraft:beetroot_soup": {
                item.setCreativeTab(null);
                break;
            }
        }
        if (item.getRegistryName() == null) {
            item.setRegistryName(name);
        }
        REGISTRY.register(id, name, item);
    }

    @Overwrite
    protected static void registerItemBlock(Block block, Item item) {
        ResourceLocation name = Block.REGISTRY.getNameForObject(block);
        switch (name.toString()) {
            case "minecraft:carpet": {
                item = new ItemWool(block);
                break;
            }
        }
        registerItem(Block.getIdFromBlock(block), name, item);
        BLOCK_TO_ITEM.put(block, item);
    }
}
