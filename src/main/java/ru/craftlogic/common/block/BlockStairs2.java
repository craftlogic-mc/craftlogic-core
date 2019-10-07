package ru.craftlogic.common.block;

import net.minecraft.block.BlockStairs;
import net.minecraft.item.Item;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;
import ru.craftlogic.api.CraftBlocks;
import ru.craftlogic.api.model.ModelManager;
import ru.craftlogic.api.model.ModelRegistrar;

public class BlockStairs2 extends BlockStairs implements ModelRegistrar {
    public BlockStairs2(BlockPlanks2.PlanksType2 type) {
        super(CraftBlocks.PLANKS2.getDefaultState().withProperty(BlockPlanks2.VARIANT, type));
        setRegistryName(type.getName() + "_stairs");
        setTranslationKey("wooden_stairs." + type.getName());
    }

    @Override
    @SideOnly(Side.CLIENT)
    public void registerModel(ModelManager modelManager) {
        Item item = Item.getItemFromBlock(this);
        modelManager.registerItemModel(item);
    }
}
