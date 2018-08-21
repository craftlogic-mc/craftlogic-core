package ru.craftlogic.mixin.block;

import net.minecraft.block.Block;
import net.minecraft.block.BlockFurnace;
import net.minecraft.block.material.Material;
import net.minecraft.creativetab.CreativeTabs;
import net.minecraft.item.ItemStack;
import net.minecraft.util.NonNullList;
import org.spongepowered.asm.mixin.Mixin;
import ru.craftlogic.api.CraftBlocks;

@Mixin(BlockFurnace.class)
public class MixinBlockFurnace extends Block {
    public MixinBlockFurnace(Material material) {
        super(material);
    }

    @Override
    public void getSubBlocks(CreativeTabs tab, NonNullList<ItemStack> items) {
        items.add(new ItemStack(CraftBlocks.FURNACE));
    }
}
