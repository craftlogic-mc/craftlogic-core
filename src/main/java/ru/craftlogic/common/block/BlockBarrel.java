package ru.craftlogic.common.block;

import net.minecraft.block.material.Material;
import net.minecraft.block.properties.PropertyBool;
import net.minecraft.block.state.BlockFaceShape;
import net.minecraft.block.state.IBlockState;
import net.minecraft.creativetab.CreativeTabs;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.util.*;
import net.minecraft.util.math.AxisAlignedBB;
import net.minecraft.util.math.RayTraceResult;
import net.minecraftforge.fluids.FluidStack;
import ru.craftlogic.api.barrel.BarrelMode;
import ru.craftlogic.api.barrel.BarrelModeFluid;
import ru.craftlogic.api.block.BlockBase;
import ru.craftlogic.api.block.holders.TileEntityHolder;
import ru.craftlogic.api.model.ModelAutoReg;
import ru.craftlogic.api.util.TileEntityInfo;
import ru.craftlogic.api.world.Location;
import ru.craftlogic.common.tileentity.TileEntityBarrel;

import static net.minecraft.init.SoundEvents.BLOCK_WOODEN_TRAPDOOR_CLOSE;
import static net.minecraft.init.SoundEvents.BLOCK_WOODEN_TRAPDOOR_OPEN;
import static ru.craftlogic.api.CraftAPI.MOD_ID;

public abstract class BlockBarrel extends BlockBase implements TileEntityHolder<TileEntityBarrel>, ModelAutoReg {
    public static final PropertyBool CLOSED = PropertyBool.create("closed");
    public static final AxisAlignedBB BOUNDING = new AxisAlignedBB(0.0625, 0, 0.0625, 0.9375, 1, 0.9375);

    public BlockBarrel(Material material, String name, float hardness) {
        super(material, name, hardness, CreativeTabs.DECORATIONS);
        this.setTickRandomly(true);
        this.setDefaultState(this.blockState.getBaseState().withProperty(CLOSED, false));
    }

    @Override
    protected boolean onBlockActivated(Location location, EntityPlayer player, EnumHand hand, RayTraceResult target) {
        if (player.isSneaking()) {
            if (!location.isWorldRemote()) {
                location.cycleBlockProperty(CLOSED);
                SoundEvent sound = location.getBlockProperty(CLOSED) ? BLOCK_WOODEN_TRAPDOOR_CLOSE : BLOCK_WOODEN_TRAPDOOR_OPEN;
                location.playSound(sound, SoundCategory.BLOCKS,1F, 1F);
            }
            return true;
        } else {
            return super.onBlockActivated(location, player, hand, target);
        }
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
    protected AxisAlignedBB getBoundingBox(Location location) {
        return BOUNDING;
    }

    @Override
    protected BlockFaceShape getBlockFaceShape(Location location, EnumFacing side) {
        switch (side) {
            case DOWN:
                return BlockFaceShape.CENTER_BIG;
            case UP:
                return BlockFaceShape.BOWL;
            default:
                return BlockFaceShape.UNDEFINED;
        }
    }

    @Override
    protected int getLightValue(Location location) {
        TileEntityBarrel barrel = location.getTileEntity(TileEntityBarrel.class);
        if (barrel != null) {
            BarrelMode mode = barrel.getMode();
            if (mode instanceof BarrelModeFluid) {
                FluidStack fluid = ((BarrelModeFluid) mode).getFluid();
                if (fluid != null) {
                    return fluid.getFluid().getLuminosity(fluid) % 16;
                }
            }
        }
        return 0;
    }

    @Override
    public ResourceLocation getTileEntityName(IBlockState state) {
        return new ResourceLocation(MOD_ID, "barrel");
    }

    @Override
    public TileEntityInfo<TileEntityBarrel> getTileEntityInfo(IBlockState state) {
        return new TileEntityInfo<>(TileEntityBarrel.class, state, TileEntityBarrel::new);
    }
}
