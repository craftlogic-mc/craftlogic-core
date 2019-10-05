package ru.craftlogic.common.block;

import net.minecraft.block.BlockHorizontal;
import net.minecraft.block.BlockPlanks;
import net.minecraft.block.SoundType;
import net.minecraft.block.material.Material;
import net.minecraft.block.properties.IProperty;
import net.minecraft.block.properties.PropertyBool;
import net.minecraft.block.properties.PropertyDirection;
import net.minecraft.block.state.IBlockState;
import net.minecraft.creativetab.CreativeTabs;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.Item;
import net.minecraft.util.*;
import net.minecraft.util.math.RayTraceResult;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;
import ru.craftlogic.api.block.BlockBase;
import ru.craftlogic.api.block.holders.TileEntityHolder;
import ru.craftlogic.api.model.ModelManager;
import ru.craftlogic.api.util.TileEntityInfo;
import ru.craftlogic.api.world.Location;
import ru.craftlogic.common.tileentity.TileEntityBeeHouse;

import javax.annotation.Nullable;

import static net.minecraft.init.SoundEvents.BLOCK_WOODEN_TRAPDOOR_CLOSE;
import static net.minecraft.init.SoundEvents.BLOCK_WOODEN_TRAPDOOR_OPEN;
import static ru.craftlogic.api.CraftAPI.MOD_ID;

public class BlockBeeHouse extends BlockBase implements TileEntityHolder<TileEntityBeeHouse> {
    public static final PropertyBool COVERED = PropertyBool.create("covered");
    public static final PropertyBool CLOSED = PropertyBool.create("closed");
    public static final PropertyDirection FACING = BlockHorizontal.FACING;

    private final BlockPlanks.EnumType plankType;

    public BlockBeeHouse(BlockPlanks.EnumType plankType) {
        super(Material.WOOD, plankType.getName() + "_bee_house", 2F, CreativeTabs.DECORATIONS);
        this.plankType = plankType;
        this.setSoundType(SoundType.WOOD);
        this.setTranslationKey("bee_house");
        this.setDefaultState(this.getBlockState().getBaseState()
            .withProperty(COVERED, true)
            .withProperty(CLOSED, true)
            .withProperty(FACING, EnumFacing.NORTH)
        );
    }

    public BlockPlanks.EnumType getPlankType() {
        return plankType;
    }

    @Override
    protected boolean onBlockActivated(Location location, EntityPlayer player, EnumHand hand, RayTraceResult target) {
        if (player.isSneaking()) {
            if (target.sideHit == EnumFacing.UP) {
                if (!location.isWorldRemote()) {
                    boolean covered = location.cycleBlockProperty(COVERED);
                    SoundEvent sound = covered ? BLOCK_WOODEN_TRAPDOOR_CLOSE : BLOCK_WOODEN_TRAPDOOR_OPEN;
                    location.playSound(sound, SoundCategory.BLOCKS,1F, 0.9F);
                }
                return true;
            } else if (target.sideHit == location.getBlockProperty(FACING)) {
                if (!location.isWorldRemote()) {
                    boolean closed = location.cycleBlockProperty(CLOSED);
                    SoundEvent sound = closed ? BLOCK_WOODEN_TRAPDOOR_CLOSE : BLOCK_WOODEN_TRAPDOOR_OPEN;
                    location.playSound(sound, SoundCategory.BLOCKS,1F, 1.5F);
                }
                return true;
            }
        } else if (!location.getBlockProperty(COVERED)) {
            return super.onBlockActivated(location, player, hand, target);
        }
        return false;
    }

    @Override
    public boolean isFullBlock(IBlockState state) {
        return false;
    }

    @Override
    public boolean isFullCube(IBlockState state) {
        return false;
    }

    @Override
    public boolean isOpaqueCube(IBlockState state) {
        return false;
    }

    @Override
    public boolean isTopSolid(IBlockState state) {
        return false;
    }

    @Override
    public IBlockState getStateForPlacement(Location location, RayTraceResult target, int meta, EntityLivingBase placer, @Nullable EnumHand hand) {
        return this.getDefaultState().withProperty(FACING, placer.getHorizontalFacing().getOpposite());
    }

    @Override
    public IBlockState getStateFromMeta(int meta) {
        return this.getDefaultState()
                .withProperty(FACING, EnumFacing.byHorizontalIndex(meta & 3))
                .withProperty(COVERED, (meta & 4) > 0)
                .withProperty(CLOSED, (meta & 8) > 0);
    }

    @Override
    public int getMetaFromState(IBlockState state) {
        int meta = state.getValue(FACING).getHorizontalIndex();
        if (state.getValue(COVERED)) meta |= 4;
        if (state.getValue(CLOSED)) meta |= 8;
        return meta;
    }

    @Override
    protected IProperty[] getProperties() {
        return new IProperty[] {COVERED, CLOSED, FACING};
    }

    @Override
    public ResourceLocation getTileEntityName(IBlockState state) {
        return new ResourceLocation(MOD_ID, "bee_house");
    }

    @Override
    public TileEntityInfo<TileEntityBeeHouse> getTileEntityInfo(IBlockState state) {
        return new TileEntityInfo<>(TileEntityBeeHouse.class, state, TileEntityBeeHouse::new);
    }

    @Override
    @SideOnly(Side.CLIENT)
    public void registerModel(ModelManager modelManager) {
        modelManager.registerItemModel(Item.getItemFromBlock(this), "bee_house/" + getPlankType().getName());
    }
}
