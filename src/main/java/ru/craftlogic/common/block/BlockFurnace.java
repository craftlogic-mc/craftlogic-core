package ru.craftlogic.common.block;

import net.minecraft.block.BlockHorizontal;
import net.minecraft.block.material.Material;
import net.minecraft.block.properties.IProperty;
import net.minecraft.block.properties.PropertyBool;
import net.minecraft.block.properties.PropertyDirection;
import net.minecraft.block.state.BlockFaceShape;
import net.minecraft.block.state.BlockStateContainer;
import net.minecraft.block.state.IBlockState;
import net.minecraft.client.renderer.block.model.ModelResourceLocation;
import net.minecraft.creativetab.CreativeTabs;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.init.SoundEvents;
import net.minecraft.util.*;
import net.minecraft.util.math.AxisAlignedBB;
import net.minecraft.util.math.RayTraceResult;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;
import ru.craftlogic.CraftLogic;
import ru.craftlogic.api.block.BlockBase;
import ru.craftlogic.api.block.holders.TileEntityHolder;
import ru.craftlogic.api.model.ModelManager;
import ru.craftlogic.api.util.TileEntityInfo;
import ru.craftlogic.api.world.Location;
import ru.craftlogic.common.tileentity.TileEntityFurnace;

import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Random;

import static ru.craftlogic.CraftLogic.SOUND_FURNACE_VENT_CLOSE;
import static ru.craftlogic.CraftLogic.SOUND_FURNACE_VENT_OPEN;

public class BlockFurnace extends BlockBase implements TileEntityHolder<TileEntityFurnace> {
    public static final PropertyDirection FACING = BlockHorizontal.FACING;
    public static final PropertyBool OPEN = PropertyBool.create("open");
    public static final PropertyBool ACTIVE = PropertyBool.create("active");

    public static final AxisAlignedBB BOUNDING = new AxisAlignedBB(0.0625, 0, 0.0625, 0.9375, 1, 0.9375);

    public BlockFurnace() {
        super(Material.ROCK, "furnace", 4.5F, CreativeTabs.DECORATIONS);
        this.setDefaultState(this.blockState.getBaseState()
                .withProperty(FACING, EnumFacing.NORTH)
                .withProperty(OPEN, true)
                .withProperty(ACTIVE, false)
        );
    }

    @Override
    public int getLightValue(IBlockState state) {
        return state.getValue(ACTIVE) ? (state.getValue(OPEN) ? 13 : 5) : 0;
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
    @SideOnly(Side.CLIENT)
    public BlockRenderLayer getBlockLayer() {
        return BlockRenderLayer.CUTOUT;
    }

    @Override
    protected AxisAlignedBB getBoundingBox(Location location) {
        return BOUNDING;
    }

    @Override
    protected boolean onBlockActivated(Location location, EntityPlayer player, EnumHand hand, RayTraceResult target) {
        if (player.isSneaking()) {
            if (target.sideHit == location.getBlockProperty(FACING)) {
                if (!location.isWorldRemote()) {
                    boolean opened = location.getBlockProperty(OPEN);
                    location.playSound(opened ? SOUND_FURNACE_VENT_CLOSE : SOUND_FURNACE_VENT_OPEN, SoundCategory.BLOCKS, 1F, 1F);
                    location.cycleBlockProperty(OPEN);
                }
                return true;
            }
            return false;
        } else {
            return super.onBlockActivated(location, player, hand, target);
        }
    }

    @Override
    protected BlockFaceShape getBlockFaceShape(Location location, EnumFacing side) {
        return side.getAxis().isVertical() ? BlockFaceShape.CENTER_BIG : BlockFaceShape.UNDEFINED;
    }

    @Override
    public IBlockState getStateForPlacement(Location location, RayTraceResult target, int meta, EntityLivingBase placer) {
        return this.getDefaultState().withProperty(FACING, placer.getHorizontalFacing().getOpposite());
    }

    @Override
    public IBlockState getStateFromMeta(int meta) {
        return this.getDefaultState()
                .withProperty(FACING, EnumFacing.getHorizontal(meta & 3))
                .withProperty(OPEN, (meta & 4) > 0)
                .withProperty(ACTIVE, (meta & 8) > 0);
    }

    @Override
    public int getMetaFromState(IBlockState state) {
        int meta = state.getValue(FACING).getHorizontalIndex();
        if (state.getValue(OPEN)) {
            meta |= 4;
        }
        if (state.getValue(ACTIVE)) {
            meta |= 8;
        }
        return meta;
    }

    @Override
    public void breakBlock(Location location, IBlockState state) {
        TileEntityFurnace furnace = location.getTileEntity(TileEntityFurnace.class);
        if (furnace != null) {
            furnace.dropItems(true);
        }
        super.breakBlock(location, state);
    }

    @Override
    @SideOnly(Side.CLIENT)
    public void randomDisplayTick(Location location, Random rand) {
        if (location.getBlockProperty(ACTIVE)) {
            EnumFacing facing = location.getBlockProperty(FACING);
            double y = -0.5 + rand.nextDouble() * 6.0 / 16.0;
            double offsetX = 0.02;
            double offsetZ = rand.nextDouble() * 0.3 - 0.3;
            if (rand.nextDouble() < 0.1) {
                location.playSound(SoundEvents.BLOCK_FURNACE_FIRE_CRACKLE, SoundCategory.BLOCKS, 1F, 1F);
            }

            switch (facing) {
                case WEST:
                    location.spawnParticle(EnumParticleTypes.SMOKE_NORMAL, -offsetX, y, offsetZ, 0, 0, 0);
                    break;
                case EAST:
                    location.spawnParticle(EnumParticleTypes.SMOKE_NORMAL, offsetX, y, offsetZ, 0, 0, 0);
                    break;
                case NORTH:
                    location.spawnParticle(EnumParticleTypes.SMOKE_NORMAL, offsetZ, y, -offsetX, 0, 0, 0);
                    break;
                case SOUTH:
                    location.spawnParticle(EnumParticleTypes.SMOKE_NORMAL, offsetZ, y, offsetX, 0, 0, 0);
            }

            if (location.getBlockProperty(OPEN)) {
                int max = rand.nextInt(5) + 1;
                for (int i = 0; i < max; i++) {
                    double d = rand.nextDouble() * 0.4 - 0.25;
                    location.spawnParticle(EnumParticleTypes.FLAME, d, y - 0.45, d, 0, 0, 0);
                }
            }
        }
    }

    @Override
    protected BlockStateContainer createBlockState() {
        return new BlockStateContainer(this, FACING, OPEN, ACTIVE);
    }

    @Override
    public TileEntityInfo<TileEntityFurnace> getTileEntityInfo(IBlockState state) {
        return new TileEntityInfo<>(TileEntityFurnace.class, state, TileEntityFurnace::new);
    }

    @Override
    @SideOnly(Side.CLIENT)
    public void registerModel(ModelManager modelManager) {
        modelManager.registerStateMapper(this, (state, mapper) -> {
            Map<IProperty<?>, Comparable<?>> props = new LinkedHashMap<>(state.getProperties());
            props.remove(ACTIVE);
            String propString = mapper.getPropertyString(props);
            return new ModelResourceLocation(CraftLogic.MODID + ":furnace", propString);
        });
        modelManager.registerItemModel(this.asItem());
    }
}
