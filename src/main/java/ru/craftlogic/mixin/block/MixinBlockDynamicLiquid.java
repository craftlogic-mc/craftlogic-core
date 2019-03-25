package ru.craftlogic.mixin.block;

import net.minecraft.block.BlockDynamicLiquid;
import net.minecraft.block.BlockLiquid;
import net.minecraft.block.material.Material;
import net.minecraft.block.state.IBlockState;
import net.minecraft.init.Blocks;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.EnumFacing.Plane;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.event.ForgeEventFactory;
import net.minecraftforge.fluids.Fluid;
import net.minecraftforge.fluids.FluidRegistry;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Overwrite;
import org.spongepowered.asm.mixin.Shadow;
import ru.craftlogic.api.event.block.FluidFlowEvent;

import java.util.Random;
import java.util.Set;

@Mixin(BlockDynamicLiquid.class)
public abstract class MixinBlockDynamicLiquid extends BlockLiquid {
    @Shadow
    private int adjacentSourceBlocks;

    protected MixinBlockDynamicLiquid(Material material) {
        super(material);
    }

    /**
     * @author Radviger
     * @reason Fluid events
     */
    @Overwrite
    public void updateTick(World world, BlockPos pos, IBlockState state, Random rand) {
        if (world.isAreaLoaded(pos, this.getSlopeFindDistance(world))) {
            int i = state.getValue(LEVEL);
            int j = 1;
            if (this.material == Material.LAVA && !world.provider.doesWaterVaporize()) {
                j = 2;
            }

            int k = this.tickRate(world);
            int k1;
            if (i > 0) {
                int l = -100;
                this.adjacentSourceBlocks = 0;


                for (EnumFacing h : Plane.HORIZONTAL) {
                    l = this.checkAdjacentBlock(world, pos.offset(h), l);
                }

                int i1 = l + j;
                if (i1 >= 8 || l < 0) {
                    i1 = -1;
                }

                k1 = this.getDepth(world.getBlockState(pos.up()));
                if (k1 >= 0) {
                    if (k1 >= 8) {
                        i1 = k1;
                    } else {
                        i1 = k1 + 8;
                    }
                }

                if (this.adjacentSourceBlocks >= 2 && ForgeEventFactory.canCreateFluidSource(world, pos, state, this.material == Material.WATER)) {
                    IBlockState downState = world.getBlockState(pos.down());
                    if (downState.getMaterial().isSolid()) {
                        i1 = 0;
                    } else if (downState.getMaterial() == this.material && downState.getValue(LEVEL) == 0) {
                        i1 = 0;
                    }
                }

                if (this.material == Material.LAVA && i < 8 && i1 < 8 && i1 > i && rand.nextInt(4) != 0) {
                    k *= 4;
                }

                if (i1 == i) {
                    this.placeStaticBlock(world, pos, state);
                } else {
                    i = i1;
                    if (i1 < 0) {
                        world.setBlockToAir(pos);
                    } else {
                        state = state.withProperty(LEVEL, i1);
                        world.setBlockState(pos, state, 2);
                        world.scheduleUpdate(pos, this, k);
                        world.notifyNeighborsOfStateChange(pos, this, false);
                    }
                }
            } else {
                this.placeStaticBlock(world, pos, state);
            }

            IBlockState downState = world.getBlockState(pos.down());
            if (this.canFlowInto(world, pos.down(), downState)) {
                if (this.material == Material.LAVA && world.getBlockState(pos.down()).getMaterial() == Material.WATER) {
                    world.setBlockState(pos.down(), Blocks.STONE.getDefaultState());
                    this.triggerMixEffects(world, pos.down());
                    return;
                }

                if (!MinecraftForge.EVENT_BUS.post(new FluidFlowEvent(getFluid(), world, pos, EnumFacing.DOWN))) {
                    if (i >= 8) {
                        this.tryFlowInto(world, pos.down(), downState, i);
                    } else {
                        this.tryFlowInto(world, pos.down(), downState, i + 8);
                    }
                }
            } else if (i >= 0 && (i == 0 || this.isBlocked(world, pos.down(), downState))) {
                Set<EnumFacing> dirs = this.getPossibleFlowDirections(world, pos);
                k1 = i + j;
                if (i >= 8) {
                    k1 = 1;
                }

                if (k1 >= 8) {
                    return;
                }

                for (EnumFacing dir : dirs) {
                    if (!MinecraftForge.EVENT_BUS.post(new FluidFlowEvent(getFluid(), world, pos, dir))) {
                        this.tryFlowInto(world, pos.offset(dir), world.getBlockState(pos.offset(dir)), k1);
                    }
                }
            }

        }
    }

    private Fluid getFluid() {
        if (this.material == Material.LAVA) {
            return FluidRegistry.LAVA;
        } else {
            return FluidRegistry.WATER;
        }
    }

    @Shadow
    protected abstract int getSlopeFindDistance(World world);

    @Shadow
    protected abstract void tryFlowInto(World world, BlockPos pos, IBlockState state, int meta);

    @Shadow
    protected abstract int checkAdjacentBlock(World world, BlockPos pos, int meta);

    @Shadow
    protected abstract void placeStaticBlock(World world, BlockPos pos, IBlockState state);

    @Shadow
    protected abstract boolean canFlowInto(World world, BlockPos pos, IBlockState state);

    @Shadow
    protected abstract boolean isBlocked(World world, BlockPos pos, IBlockState state);

    @Shadow
    protected abstract Set<EnumFacing> getPossibleFlowDirections(World world, BlockPos pos);
}
