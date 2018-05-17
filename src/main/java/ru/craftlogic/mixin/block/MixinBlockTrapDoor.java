package ru.craftlogic.mixin.block;

import net.minecraft.block.Block;
import net.minecraft.block.BlockTrapDoor;
import net.minecraft.block.material.Material;
import net.minecraft.creativetab.CreativeTabs;
import net.minecraft.init.Blocks;
import net.minecraft.item.ItemStack;
import net.minecraft.util.NonNullList;
import org.spongepowered.asm.mixin.Mixin;

@Mixin(BlockTrapDoor.class)
public class MixinBlockTrapDoor extends Block  {
    public MixinBlockTrapDoor(Material material) {
        super(material);
    }

    @Override
    public void getSubBlocks(CreativeTabs tab, NonNullList<ItemStack> items) {
        String id = this.getRegistryName().toString();
        if (id.startsWith("minecraft:") && id.endsWith("trapdoor")) {
            if (this == Blocks.TRAPDOOR) {
                items.add(new ItemStack(Blocks.TRAPDOOR));
                items.add(new ItemStack(Blocks.IRON_TRAPDOOR));
            }
        } else {
            items.add(new ItemStack(this));
        }
    }
}
