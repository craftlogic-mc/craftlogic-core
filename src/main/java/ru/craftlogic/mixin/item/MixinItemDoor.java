package ru.craftlogic.mixin.item;

import net.minecraft.block.Block;
import net.minecraft.creativetab.CreativeTabs;
import net.minecraft.init.Items;
import net.minecraft.item.Item;
import net.minecraft.item.ItemDoor;
import net.minecraft.item.ItemStack;
import net.minecraft.util.NonNullList;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;

@Mixin(ItemDoor.class)
public class MixinItemDoor extends Item {
    @Shadow @Final private Block block;

    @Override
    public void getSubItems(CreativeTabs tab, NonNullList<ItemStack> items) {
        if (this.isInCreativeTab(tab)) {
            String id = this.getRegistryName().toString();
            if (id.startsWith("minecraft:") && id.endsWith("door")) {
                if (this == Items.OAK_DOOR) {
                    items.add(new ItemStack(Items.IRON_DOOR));
                    items.add(new ItemStack(Items.OAK_DOOR));
                    items.add(new ItemStack(Items.SPRUCE_DOOR));
                    items.add(new ItemStack(Items.BIRCH_DOOR));
                    items.add(new ItemStack(Items.JUNGLE_DOOR));
                    items.add(new ItemStack(Items.ACACIA_DOOR));
                    items.add(new ItemStack(Items.DARK_OAK_DOOR));
                }
            } else {
                items.add(new ItemStack(this));
            }
        }
    }
}
