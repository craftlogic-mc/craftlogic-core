package ru.craftlogic.common.block;

import net.minecraft.block.SoundType;
import net.minecraft.block.material.Material;
import net.minecraft.creativetab.CreativeTabs;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.util.NonNullList;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;
import ru.craftlogic.api.model.ModelManager;
import ru.craftlogic.api.model.ModelRegistrar;

public class BlockDiagonalFence2 extends BlockDiagonalFence implements ModelRegistrar {
    public BlockDiagonalFence2(BlockPlanks2.PlanksType2 type) {
        super(Material.WOOD, type.getMapColor());
        setRegistryName(type.getName() + "_fence");
        setHardness(2.0F);
        setResistance(5.0F);
        setSoundType(SoundType.WOOD);
        setTranslationKey("fence." + type.getName());
    }

    @Override
    public void getSubBlocks(CreativeTabs tab, NonNullList<ItemStack> items) {}

    @Override
    @SideOnly(Side.CLIENT)
    public void registerModel(ModelManager modelManager) {
        Item item = Item.getItemFromBlock(this);
        modelManager.registerItemModel(item);
    }
}
