package ru.craftlogic.common.block;

import net.minecraft.block.BlockFenceGate;
import net.minecraft.block.BlockPlanks;
import net.minecraft.block.SoundType;
import net.minecraft.client.renderer.block.statemap.StateMap;
import net.minecraft.creativetab.CreativeTabs;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.util.NonNullList;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;
import ru.craftlogic.api.model.ModelManager;
import ru.craftlogic.api.model.ModelRegistrar;

public class BlockFenceGate2 extends BlockFenceGate implements ModelRegistrar {
    public BlockFenceGate2(BlockPlanks2.PlanksType2 type) {
        super(BlockPlanks.EnumType.OAK);
        setRegistryName(type.getName() + "_fence_gate");
        setHardness(2.0F);
        setResistance(5.0F);
        setSoundType(SoundType.WOOD);
        setTranslationKey("fence_gate." + type.getName());
    }

    @Override
    @SideOnly(Side.CLIENT)
    public void registerModel(ModelManager modelManager) {
        modelManager.registerStateMapper(this, new StateMap.Builder().ignore(POWERED).build());
        Item item = Item.getItemFromBlock(this);
        modelManager.registerItemModel(item);
    }

    @Override
    public void getSubBlocks(CreativeTabs tab, NonNullList<ItemStack> items) {}
}
