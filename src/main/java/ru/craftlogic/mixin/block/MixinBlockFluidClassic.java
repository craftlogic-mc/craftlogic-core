package ru.craftlogic.mixin.block;

import net.minecraft.block.material.Material;
import net.minecraft.block.state.IBlockState;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.IBlockAccess;
import net.minecraft.world.World;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.event.ForgeEventFactory;
import net.minecraftforge.fluids.BlockFluidBase;
import net.minecraftforge.fluids.BlockFluidClassic;
import net.minecraftforge.fluids.Fluid;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Overwrite;
import org.spongepowered.asm.mixin.Shadow;
import ru.craftlogic.api.event.block.FluidFlowEvent;

import javax.annotation.Nonnull;
import java.util.Random;

@Mixin(value = BlockFluidClassic.class)
public abstract class MixinBlockFluidClassic extends BlockFluidBase {
    @Shadow(remap = false) protected abstract int getLargerQuanta(IBlockAccess world, BlockPos pos, int compare);

    @Shadow(remap = false) public abstract boolean isSourceBlock(IBlockAccess world, BlockPos pos);

    @Shadow(remap = false) protected abstract boolean[] getOptimalFlowDirections(World world, BlockPos pos);

    @Shadow(remap = false) public abstract boolean isFlowingVertically(IBlockAccess world, BlockPos pos);

    @Shadow(remap = false) protected abstract void flowIntoBlock(World world, BlockPos pos, int meta);

    public MixinBlockFluidClassic(Fluid fluid, Material material) {
        super(fluid, material);
    }

    /**
     * @author Radviger
     * @reason Fluid flow events
     */
    @Overwrite
    public void updateTick(@Nonnull World world, @Nonnull BlockPos pos, @Nonnull IBlockState state, @Nonnull Random rand) {
        int quantaRemaining;
        if (!this.isSourceBlock(world, pos) && ForgeEventFactory.canCreateFluidSource(world, pos, state, false)) {
            quantaRemaining = (this.isSourceBlock(world, pos.north()) ? 1 : 0) + (this.isSourceBlock(world, pos.south()) ? 1 : 0) + (this.isSourceBlock(world, pos.east()) ? 1 : 0) + (this.isSourceBlock(world, pos.west()) ? 1 : 0);
            if (quantaRemaining >= 2 && (world.getBlockState(pos.up(this.densityDir)).getMaterial().isSolid() || this.isSourceBlock(world, pos.up(this.densityDir)))) {
                world.setBlockState(pos, state.withProperty(LEVEL, 0));
            }
        }

        quantaRemaining = this.quantaPerBlock - state.getValue(LEVEL);
        int flowMeta;
        if (quantaRemaining < this.quantaPerBlock) {
            int expQuanta;
            if (world.getBlockState(pos.add(0, -this.densityDir, 0)).getBlock() != this && world.getBlockState(pos.add(-1, -this.densityDir, 0)).getBlock() != this && world.getBlockState(pos.add(1, -this.densityDir, 0)).getBlock() != this && world.getBlockState(pos.add(0, -this.densityDir, -1)).getBlock() != this && world.getBlockState(pos.add(0, -this.densityDir, 1)).getBlock() != this) {
                int maxQuanta = -100;
                flowMeta = this.getLargerQuanta(world, pos.add(-1, 0, 0), maxQuanta);
                flowMeta = this.getLargerQuanta(world, pos.add(1, 0, 0), flowMeta);
                flowMeta = this.getLargerQuanta(world, pos.add(0, 0, -1), flowMeta);
                flowMeta = this.getLargerQuanta(world, pos.add(0, 0, 1), flowMeta);
                expQuanta = flowMeta - 1;
            } else {
                expQuanta = this.quantaPerBlock - 1;
            }

            if (expQuanta != quantaRemaining) {
                quantaRemaining = expQuanta;
                if (expQuanta <= 0) {
                    world.setBlockToAir(pos);
                } else {
                    world.setBlockState(pos, state.withProperty(LEVEL, this.quantaPerBlock - expQuanta), 2);
                    world.scheduleUpdate(pos, this, this.tickRate);
                    world.notifyNeighborsOfStateChange(pos, this, false);
                }
            }
        } else {
            world.setBlockState(pos, this.getDefaultState(), 2);
        }

        if (this.canDisplace(world, pos.up(this.densityDir))) {
            if (!MinecraftForge.EVENT_BUS.post(new FluidFlowEvent(this.getFluid(), world, pos, this.densityDir > 0 ? EnumFacing.UP : EnumFacing.DOWN))) {
                this.flowIntoBlock(world, pos.up(this.densityDir), 1);
            }
        } else {
            flowMeta = this.quantaPerBlock - quantaRemaining + 1;
            if (flowMeta < this.quantaPerBlock) {
                if (this.isSourceBlock(world, pos) || !this.isFlowingVertically(world, pos)) {
                    if (world.getBlockState(pos.down(this.densityDir)).getBlock() == this) {
                        flowMeta = 1;
                    }

                    boolean[] flowTo = this.getOptimalFlowDirections(world, pos);
                    if (flowTo[0]) {
                        if (!MinecraftForge.EVENT_BUS.post(new FluidFlowEvent(this.getFluid(), world, pos, EnumFacing.WEST))) {
                            this.flowIntoBlock(world, pos.west(), flowMeta);
                        }
                    }

                    if (flowTo[1]) {
                        if (!MinecraftForge.EVENT_BUS.post(new FluidFlowEvent(this.getFluid(), world, pos, EnumFacing.EAST))) {
                            this.flowIntoBlock(world, pos.east(), flowMeta);
                        }
                    }

                    if (flowTo[2]) {
                        if (!MinecraftForge.EVENT_BUS.post(new FluidFlowEvent(this.getFluid(), world, pos, EnumFacing.NORTH))) {
                            this.flowIntoBlock(world, pos.north(), flowMeta);
                        }
                    }

                    if (flowTo[3]) {
                        if (!MinecraftForge.EVENT_BUS.post(new FluidFlowEvent(this.getFluid(), world, pos, EnumFacing.SOUTH))) {
                            this.flowIntoBlock(world, pos.south(), flowMeta);
                        }
                    }
                }
            }
        }
    }
}
