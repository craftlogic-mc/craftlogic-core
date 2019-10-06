package ru.craftlogic.mixin.block;

import net.minecraft.block.BlockLog;
import net.minecraft.block.BlockNewLog;
import net.minecraft.creativetab.CreativeTabs;
import net.minecraft.item.ItemStack;
import net.minecraft.util.NonNullList;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Overwrite;

@Mixin(BlockNewLog.class)
public abstract class MixinBlockLogNew extends BlockLog {
    /**
     * @author Radviger
     * @reason Sorted CreativeTab items
     */
    @Overwrite
    public void getSubBlocks(CreativeTabs item, NonNullList<ItemStack> items) {}
}
