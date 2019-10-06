package ru.craftlogic.common.block;

import net.minecraft.block.BlockLog;
import net.minecraft.block.properties.PropertyEnum;
import net.minecraft.block.state.BlockStateContainer;
import net.minecraft.block.state.IBlockState;
import net.minecraft.client.renderer.block.statemap.StateMap;
import net.minecraft.creativetab.CreativeTabs;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.util.NonNullList;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;
import ru.craftlogic.api.model.ModelManager;
import ru.craftlogic.api.model.ModelRegistrar;
import ru.craftlogic.common.block.BlockPlanks2.PlanksType2;

public class BlockLog3 extends net.minecraft.block.BlockLog implements ModelRegistrar {
    public static final PropertyEnum<PlanksType2> VARIANT = PropertyEnum.create("variant", PlanksType2.class);

    public BlockLog3() {
        setRegistryName("log3");
        setDefaultState(getBlockState().getBaseState().withProperty(VARIANT, PlanksType2.PINE).withProperty(LOG_AXIS, EnumAxis.Y));
    }

    @Override
    public void getSubBlocks(CreativeTabs tab, NonNullList<ItemStack> items) {}

    @Override
    public IBlockState getStateFromMeta(int meta) {
        IBlockState state = getDefaultState().withProperty(VARIANT, PlanksType2.byMetadata((meta & 3)));

        switch (meta & 12) {
            case 0:
                state = state.withProperty(LOG_AXIS, BlockLog.EnumAxis.Y);
                break;
            case 4:
                state = state.withProperty(LOG_AXIS, BlockLog.EnumAxis.X);
                break;
            case 8:
                state = state.withProperty(LOG_AXIS, BlockLog.EnumAxis.Z);
                break;
            default:
                state = state.withProperty(LOG_AXIS, BlockLog.EnumAxis.NONE);
        }

        return state;
    }

    public String getTranslationKey(ItemStack item) {
        return BlockPlanks2.PlanksType2.byMetadata(item.getMetadata() & 3).getTranslationKey();
    }

    @Override
    public int getMetaFromState(IBlockState state) {
        int meta = state.getValue(VARIANT).getMetadata();

        switch (state.getValue(LOG_AXIS)) {
            case X:
                meta |= 4;
                break;
            case Z:
                meta |= 8;
                break;
            case NONE:
                meta |= 12;
        }

        return meta;
    }

    @Override
    protected BlockStateContainer createBlockState() {
        return new BlockStateContainer(this, VARIANT, LOG_AXIS);
    }

    @Override
    protected ItemStack getSilkTouchDrop(IBlockState state) {
        return new ItemStack(Item.getItemFromBlock(this), 1, state.getValue(VARIANT).getMetadata());
    }

    @Override
    public int damageDropped(IBlockState state) {
        return state.getValue(VARIANT).getMetadata();
    }

    @Override
    @SideOnly(Side.CLIENT)
    public void registerModel(ModelManager modelManager) {
        modelManager.registerStateMapper(this, new StateMap.Builder().withName(VARIANT).withSuffix("_log").build());
        Item item = Item.getItemFromBlock(this);
        modelManager.registerItemVariants(item, "pine_log", "willow_log");
        for (BlockPlanks2.PlanksType2 type : BlockPlanks2.PlanksType2.values()) {
            modelManager.registerItemModel(item, type.getMetadata(), type.getName() + "_log");
        }
    }
}
