package ru.craftlogic.mixin.block;

import net.minecraft.block.Block;
import net.minecraft.block.BlockBasePressurePlate;
import net.minecraft.block.material.Material;
import net.minecraft.creativetab.CreativeTabs;
import net.minecraft.init.Blocks;
import net.minecraft.item.ItemStack;
import net.minecraft.util.NonNullList;
import org.spongepowered.asm.mixin.Mixin;

@Mixin(BlockBasePressurePlate.class)
public class MixinBlockBasePressurePlate extends Block  {
    public MixinBlockBasePressurePlate(Material material) {
        super(material);
    }

    @Override
    public void getSubBlocks(CreativeTabs tab, NonNullList<ItemStack> items) {
        String id = this.getRegistryName().toString();
        if (id.startsWith("minecraft:") && id.endsWith("pressure_plate")) {
            if (this == Blocks.WOODEN_PRESSURE_PLATE) {
                items.add(new ItemStack(Blocks.LIGHT_WEIGHTED_PRESSURE_PLATE));
                items.add(new ItemStack(Blocks.HEAVY_WEIGHTED_PRESSURE_PLATE));
                items.add(new ItemStack(Blocks.STONE_PRESSURE_PLATE));
                items.add(new ItemStack(Blocks.WOODEN_PRESSURE_PLATE));
            }
        } else {
            items.add(new ItemStack(this));
        }
    }
}
