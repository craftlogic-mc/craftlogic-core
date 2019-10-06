package ru.craftlogic.mixin.block;

import net.minecraft.block.Block;
import net.minecraft.block.BlockLog;
import net.minecraft.block.BlockOldLog;
import net.minecraft.block.BlockPlanks;
import net.minecraft.creativetab.CreativeTabs;
import net.minecraft.init.Blocks;
import net.minecraft.item.ItemStack;
import net.minecraft.util.NonNullList;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Overwrite;
import ru.craftlogic.api.CraftBlocks;
import ru.craftlogic.common.block.BlockPlanks2;

@Mixin(BlockOldLog.class)
public abstract class MixinBlockLogOld extends BlockLog {
    /**
     * @author Radviger
     * @reason Sorted CreativeTab items
     */
    @Overwrite
    public void getSubBlocks(CreativeTabs item, NonNullList<ItemStack> items) {
        items.add(new ItemStack(this, 1, BlockPlanks.EnumType.OAK.getMetadata()));
        items.add(new ItemStack(this, 1, BlockPlanks.EnumType.SPRUCE.getMetadata()));
        items.add(new ItemStack(this, 1, BlockPlanks.EnumType.BIRCH.getMetadata()));
        items.add(new ItemStack(this, 1, BlockPlanks.EnumType.JUNGLE.getMetadata()));
        Block log2 = Blocks.LOG2;
        items.add(new ItemStack(log2, 1, BlockPlanks.EnumType.ACACIA.getMetadata() - 4));
        items.add(new ItemStack(log2, 1, BlockPlanks.EnumType.DARK_OAK.getMetadata() - 4));
        Block log3 = CraftBlocks.LOG3;
        items.add(new ItemStack(log3, 1, BlockPlanks2.PlanksType2.PINE.getMetadata()));
        items.add(new ItemStack(log3, 1, BlockPlanks2.PlanksType2.WILLOW.getMetadata()));
    }
}
