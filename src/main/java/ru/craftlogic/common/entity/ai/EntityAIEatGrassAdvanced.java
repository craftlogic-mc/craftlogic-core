package ru.craftlogic.common.entity.ai;

import net.minecraft.block.Block;
import net.minecraft.block.BlockCrops;
import net.minecraft.block.state.IBlockState;
import net.minecraft.entity.ai.EntityAIEatGrass;
import net.minecraft.entity.passive.EntityAnimal;
import net.minecraft.init.Blocks;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;
import net.minecraftforge.event.ForgeEventFactory;
import ru.craftlogic.api.entity.HebivorousAnimal;

import java.util.function.BooleanSupplier;

public class EntityAIEatGrassAdvanced<A extends EntityAnimal & HebivorousAnimal> extends EntityAIEatGrass {
    private final boolean eatSward;
    private final BooleanSupplier active;
    private final A entity;
    private final World world;
    private int eatingTimer;

    public EntityAIEatGrassAdvanced(A entity, boolean eatSward, BooleanSupplier active) {
        super(entity);
        this.entity = entity;
        this.world = entity.world;
        this.eatSward = eatSward;
        this.active = active;
    }

    private void onCropEaten(BlockPos pos, IBlockState state) {
        if (state.getBlock() instanceof BlockCrops) {
            this.world.setBlockState(pos, state.withProperty(BlockCrops.AGE, 0));
        } else {
            this.world.destroyBlock(pos, false);
        }
    }

    @Override
    public boolean shouldExecute() {
        if (this.active.getAsBoolean() && this.entity.getRNG().nextInt(this.entity.isChild() ? 50 : 250) == 0) {
            BlockPos pos = new BlockPos(this.entity.posX, this.entity.posY, this.entity.posZ);
            IBlockState state = this.world.getBlockState(pos);
            if (state.getBlock() == Blocks.TALLGRASS) {
                return true;
            } else if (this.eatSward) {
                return this.world.getBlockState(pos.down()).getBlock() == Blocks.GRASS;
            }
        }
        return false;
    }

    @Override
    public void startExecuting() {
        this.eatingTimer = 40;
        this.world.setEntityState(this.entity, (byte)10);
        this.entity.getNavigator().clearPath();
    }

    @Override
    public void resetTask() {
        this.eatingTimer = 0;
    }

    @Override
    public boolean shouldContinueExecuting() {
        return this.eatingTimer > 0;
    }

    @Override
    public int getEatingGrassTimer() {
        return this.eatingTimer;
    }

    @Override
    public void updateTask() {
        this.eatingTimer = Math.max(0, this.eatingTimer - 1);
        if (this.eatingTimer == 4) {
            BlockPos pos = new BlockPos(this.entity.posX, this.entity.posY, this.entity.posZ);
            IBlockState state = this.world.getBlockState(pos);
            if (state.getBlock() == Blocks.TALLGRASS) {
                if (ForgeEventFactory.getMobGriefingEvent(this.world, this.entity)) {
                    if (state.getBlock() instanceof BlockCrops) {
                        this.world.playEvent(2001, pos, Block.getIdFromBlock(state.getBlock()));
                    }
                    this.onCropEaten(pos, state);
                }

                this.entity.onPlantEaten(state);
            } else if (this.eatSward) {
                BlockPos downPos = pos.down();
                state = this.world.getBlockState(downPos);
                if (state.getBlock() == Blocks.GRASS) {
                    this.world.playEvent(2001, downPos, Block.getIdFromBlock(Blocks.GRASS));
                    if (ForgeEventFactory.getMobGriefingEvent(this.world, this.entity)) {
                        this.world.setBlockState(downPos, Blocks.DIRT.getDefaultState(), 2);
                    }

                    this.entity.onPlantEaten(state);
                }
            }
        }
    }
}
