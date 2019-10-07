package ru.craftlogic.common.block;

import net.minecraft.block.SoundType;
import net.minecraft.block.material.MapColor;
import net.minecraft.block.material.Material;
import net.minecraft.block.properties.IProperty;
import net.minecraft.block.properties.PropertyEnum;
import net.minecraft.block.state.IBlockState;
import net.minecraft.client.renderer.block.statemap.StateMap;
import net.minecraft.creativetab.CreativeTabs;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.util.NonNullList;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.IBlockAccess;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;
import ru.craftlogic.api.block.BlockBase;
import ru.craftlogic.api.model.ModelManager;
import ru.craftlogic.api.util.Nameable;

public class BlockPlanks2 extends BlockBase {
    public static final PropertyEnum<PlanksType2> VARIANT = PropertyEnum.create("variant", PlanksType2.class);

    public BlockPlanks2() {
        super(Material.WOOD, "planks2", 2F, CreativeTabs.BUILDING_BLOCKS);
        setDefaultState(getBlockState().getBaseState().withProperty(VARIANT, PlanksType2.PINE));
        setResistance(5F);
        setHasSubtypes(true);
        setSoundType(SoundType.WOOD);
    }

    @Override
    public int damageDropped(IBlockState state) {
        return state.getValue(VARIANT).getMetadata();
    }

    @Override
    public void getSubBlocks(CreativeTabs tab, NonNullList<ItemStack> items) {}

    @Override
    public IBlockState getStateFromMeta(int meta) {
        return this.getDefaultState().withProperty(VARIANT, PlanksType2.byMetadata(meta));
    }

    @Override
    public int getMetaFromState(IBlockState state) {
        return state.getValue(VARIANT).getMetadata();
    }

    @Override
    public MapColor getMapColor(IBlockState state, IBlockAccess worldIn, BlockPos pos) {
        return state.getValue(VARIANT).getMapColor();
    }

    @Override
    public String getTranslationKey(ItemStack item) {
        return "tile.planks." + BlockPlanks2.PlanksType2.byMetadata(item.getMetadata() & 3).getName();
    }

    @Override
    @SideOnly(Side.CLIENT)
    public void registerModel(ModelManager modelManager) {
        modelManager.registerStateMapper(this, new StateMap.Builder().withName(VARIANT).withSuffix("_planks").build());
        Item item = Item.getItemFromBlock(this);
        modelManager.registerItemVariants(item, "pine_planks", "willow_planks");
        for (BlockPlanks2.PlanksType2 type : BlockPlanks2.PlanksType2.values()) {
            modelManager.registerItemModel(item, type.getMetadata(), type.getName() + "_planks");
        }
    }

    @Override
    protected IProperty[] getProperties() {
        return new IProperty[]{VARIANT};
    }

    public enum PlanksType2 implements Nameable {
        PINE(MapColor.DIRT),
        WILLOW(MapColor.GREEN);

        private final MapColor mapColor;

        PlanksType2(MapColor mapColor) {
            this.mapColor = mapColor;
        }

        public int getMetadata() {
            return ordinal();
        }

        public MapColor getMapColor() {
            return this.mapColor;
        }

        public String toString() {
            return getName();
        }

        public static PlanksType2 byMetadata(int meta) {
            return values()[meta % values().length];
        }
    }
}