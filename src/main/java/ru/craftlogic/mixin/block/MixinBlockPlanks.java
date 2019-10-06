package ru.craftlogic.mixin.block;

import net.minecraft.block.Block;
import net.minecraft.block.BlockPlanks;
import net.minecraft.block.material.MapColor;
import net.minecraft.block.material.Material;
import net.minecraft.creativetab.CreativeTabs;
import net.minecraft.item.ItemStack;
import net.minecraft.util.NonNullList;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Overwrite;
import ru.craftlogic.api.CraftBlocks;
import ru.craftlogic.common.block.BlockPlanks2;

@Mixin(BlockPlanks.class)
public abstract class MixinBlockPlanks extends Block {
    public MixinBlockPlanks(Material material, MapColor mapColor) {
        super(material, mapColor);
    }

    /**
     * @author Radviger
     * @reason Sorted CreativeTab items
     */
    @Overwrite
    public void getSubBlocks(CreativeTabs tab, NonNullList<ItemStack> items) {
        for (BlockPlanks.EnumType type : BlockPlanks.EnumType.values()) {
            items.add(new ItemStack(this, 1, type.getMetadata()));
        }
        for (BlockPlanks2.PlanksType2 type : BlockPlanks2.PlanksType2.values()) {
            items.add(new ItemStack(CraftBlocks.PLANKS2, 1, type.getMetadata()));
        }
    }
}
