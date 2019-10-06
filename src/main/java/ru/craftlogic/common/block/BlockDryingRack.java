package ru.craftlogic.common.block;

import net.minecraft.block.BlockPlanks;
import net.minecraft.block.SoundType;
import net.minecraft.block.material.Material;
import net.minecraft.block.properties.IProperty;
import net.minecraft.block.properties.PropertyEnum;
import net.minecraft.block.state.IBlockState;
import net.minecraft.client.renderer.block.statemap.DefaultStateMapper;
import net.minecraft.creativetab.CreativeTabs;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.EnumHand;
import net.minecraft.util.NonNullList;
import net.minecraft.util.math.AxisAlignedBB;
import net.minecraft.util.math.RayTraceResult;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;
import ru.craftlogic.api.block.BlockNarrow;
import ru.craftlogic.api.block.holders.TileEntityHolder;
import ru.craftlogic.api.model.ModelManager;
import ru.craftlogic.api.util.TileEntityInfo;
import ru.craftlogic.api.world.Location;
import ru.craftlogic.common.tileentity.TileEntityDryingRack;

import javax.annotation.Nullable;

public class BlockDryingRack extends BlockNarrow implements TileEntityHolder<TileEntityDryingRack> {
    public static final PropertyEnum<EnumFacing.Axis> AXIS = PropertyEnum.create("axis", EnumFacing.Axis.class, a -> a != EnumFacing.Axis.Y);
    public static final PropertyEnum<BlockPlanks.EnumType> VARIANT = BlockPlanks.VARIANT;

    public static final AxisAlignedBB BOUNDING = new AxisAlignedBB(0, 0, 0, 1, 0.75, 1);

    public BlockDryingRack() {
        super(Material.WOOD, "drying_rack", 2F, CreativeTabs.DECORATIONS);
        this.setSoundType(SoundType.WOOD);
        this.setHasSubtypes(true);
    }

    @Override
    protected AxisAlignedBB getBoundingBox(Location location) {
        return BOUNDING;
    }

    @Override
    public IBlockState getStateForPlacement(Location location, RayTraceResult target, int meta, EntityLivingBase placer, @Nullable EnumHand hand) {
        return this.getDefaultState()
                .withProperty(VARIANT, BlockPlanks.EnumType.byMetadata(meta & 7))
                .withProperty(AXIS, placer.getHorizontalFacing().getAxis());
    }

    @Override
    public IBlockState getStateFromMeta(int meta) {
        return this.getDefaultState()
                .withProperty(VARIANT, BlockPlanks.EnumType.byMetadata(meta & 7))
                .withProperty(AXIS, (meta & 8) > 0 ? EnumFacing.Axis.Z : EnumFacing.Axis.X);
    }

    @Override
    public int getMetaFromState(IBlockState state) {
        int meta = state.getValue(VARIANT).getMetadata();
        if (state.getValue(AXIS) == EnumFacing.Axis.Z) meta |= 8;
        return meta;
    }

    @Override
    protected IProperty[] getProperties() {
        return new IProperty[] {AXIS, VARIANT};
    }

    @Override
    public TileEntityInfo<TileEntityDryingRack> getTileEntityInfo(IBlockState state) {
        return new TileEntityInfo<>(TileEntityDryingRack.class, state, TileEntityDryingRack::new);
    }

    @Override
    @SideOnly(Side.CLIENT)
    public void registerModel(ModelManager modelManager) {
        modelManager.registerStateMapper(this, new DefaultStateMapper());
        for (BlockPlanks.EnumType planksType : BlockPlanks.EnumType.values()) {
            String model = "drying_rack/" + planksType.getName();
            modelManager.registerItemModel(Item.getItemFromBlock(this), planksType.ordinal(), model);
        }
    }

    @Override
    public void getSubBlocks(CreativeTabs tab, NonNullList<ItemStack> items) {
        for (BlockPlanks.EnumType planksType : BlockPlanks.EnumType.values()) {
            items.add(new ItemStack(this, 1, planksType.getMetadata()));
        }
    }

    @Override
    public int damageDropped(IBlockState state) {
        return state.getValue(VARIANT).getMetadata();
    }
}
