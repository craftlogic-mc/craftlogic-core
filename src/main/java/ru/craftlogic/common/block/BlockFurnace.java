package ru.craftlogic.common.block;

import net.minecraft.block.BlockHorizontal;
import net.minecraft.block.material.Material;
import net.minecraft.block.properties.PropertyBool;
import net.minecraft.block.properties.PropertyDirection;
import net.minecraft.block.state.BlockStateContainer;
import net.minecraft.block.state.IBlockState;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.init.SoundEvents;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.EnumParticleTypes;
import net.minecraft.util.SoundCategory;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.RayTraceResult;
import net.minecraft.world.World;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;
import ru.craftlogic.api.block.BlockBase;
import ru.craftlogic.api.block.holders.TileEntityHolder;
import ru.craftlogic.api.util.TileEntityInfo;
import ru.craftlogic.api.world.Location;
import ru.craftlogic.common.tileentity.TileEntityFurnace;

import java.util.Random;

public class BlockFurnace extends BlockBase implements TileEntityHolder<TileEntityFurnace> {
    public static final PropertyDirection FACING = BlockHorizontal.FACING;
    public static final PropertyBool OPEN = PropertyBool.create("open");
    public static final PropertyBool ACTIVE = PropertyBool.create("active");

    public BlockFurnace() {
        super(Material.ROCK, "furnace", 4.5F, null);
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
/*

    @Override
    protected boolean onBlockActivated(Location location, EntityPlayer player, EnumHand hand, RayTraceResult target) {
        if (player.isSneaking()) {
            if (target.sideHit == location.getBlockProperty(FACING)) {
                if (!location.isWorldRemote()) {
                    boolean opened = location.getBlockProperty(OPEN);
                    location.playSound(opened ? FURNACE_VENT_CLOSE : FURNACE_VENT_OPEN, SoundCategory.BLOCKS, 1F, 1F);
                    location.cycleBlockProperty(OPEN);
                }
                return true;
            }
            return false;
        } else {
            return super.onBlockActivated(location, player, hand, target);
        }
    }
*/

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
            boolean open = location.getBlockProperty(OPEN);
            EnumFacing facing = location.getBlockProperty(FACING);
            World world = location.getWorld();
            BlockPos pos = location.getPos();
            double x = (double)pos.getX() + 0.5D;
            double y = (double)pos.getY() + rand.nextDouble() * 6.0D / 16.0D;
            double z = (double)pos.getZ() + 0.5D;
            double o = rand.nextDouble() * 0.6D - 0.3D;
            if (rand.nextDouble() < 0.1D) {
                location.playSound(SoundEvents.BLOCK_FURNACE_FIRE_CRACKLE, SoundCategory.BLOCKS, 1F, 1F);
            }

            switch(facing) {
                case WEST:
                    if (open) {
                        world.spawnParticle(EnumParticleTypes.SMOKE_NORMAL, x - 0.52D, y, z + o, 0, 0, 0);
                    }
                    world.spawnParticle(EnumParticleTypes.FLAME, x - 0.52D, y, z + o, 0, 0, 0);
                    break;
                case EAST:
                    if (open) {
                        world.spawnParticle(EnumParticleTypes.SMOKE_NORMAL, x + 0.52D, y, z + o, 0, 0, 0);
                    }
                    world.spawnParticle(EnumParticleTypes.FLAME, x + 0.52D, y, z + o, 0, 0, 0);
                    break;
                case NORTH:
                    if (open) {
                        world.spawnParticle(EnumParticleTypes.SMOKE_NORMAL, x + o, y, z - 0.52D, 0, 0, 0);
                    }
                    world.spawnParticle(EnumParticleTypes.FLAME, x + o, y, z - 0.52D, 0, 0, 0);
                    break;
                case SOUTH:
                    if (open) {
                        world.spawnParticle(EnumParticleTypes.SMOKE_NORMAL, x + o, y, z + 0.52D, 0, 0, 0);
                    }
                    world.spawnParticle(EnumParticleTypes.FLAME, x + o, y, z + 0.52D, 0, 0, 0);
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
}
