package ru.craftlogic.mixin.block;

import net.minecraft.block.BlockFenceGate;
import net.minecraft.block.BlockHorizontal;
import net.minecraft.block.material.Material;
import net.minecraft.creativetab.CreativeTabs;
import net.minecraft.init.Blocks;
import net.minecraft.item.ItemStack;
import net.minecraft.util.NonNullList;
import org.spongepowered.asm.mixin.Mixin;

@Mixin(BlockFenceGate.class)
public abstract class MixinBlockFenceGate extends BlockHorizontal {
    protected MixinBlockFenceGate(Material material) {
        super(material);
    }

    @Override
    public void getSubBlocks(CreativeTabs tab, NonNullList<ItemStack> items) {
        String id = this.getRegistryName().toString();
        if (id.startsWith("minecraft:") && id.endsWith("fence_gate")) {
            if (this == Blocks.OAK_FENCE_GATE) {
                items.add(new ItemStack(Blocks.OAK_FENCE_GATE));
                items.add(new ItemStack(Blocks.SPRUCE_FENCE_GATE));
                items.add(new ItemStack(Blocks.BIRCH_FENCE_GATE));
                items.add(new ItemStack(Blocks.JUNGLE_FENCE_GATE));
                items.add(new ItemStack(Blocks.ACACIA_FENCE_GATE));
                items.add(new ItemStack(Blocks.DARK_OAK_FENCE_GATE));
            }
        } else {
            items.add(new ItemStack(this));
        }
    }
}
