package ru.craftlogic.common.entity.ai;

import net.minecraft.block.Block;
import net.minecraft.block.BlockCrops;
import net.minecraft.block.state.IBlockState;
import net.minecraft.entity.ai.EntityAIMoveToBlock;
import net.minecraft.entity.passive.EntityAnimal;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;
import net.minecraftforge.event.ForgeEventFactory;
import ru.craftlogic.api.entity.HebivorousAnimal;

import java.util.function.BiPredicate;
import java.util.function.BooleanSupplier;

public class EntityAIEatCrops<A extends EntityAnimal & HebivorousAnimal> extends EntityAIMoveToBlock {
    private A entity;
    private int eatingTimer;
    private int timeoutCounter;
    private int maxStayTicks;
    private boolean reachedCrop;
    private final float movementSpeed;
    private final BiPredicate<BlockPos, IBlockState> cropFilter;
    private final BooleanSupplier active;

    public EntityAIEatCrops(A entity, float movementSpeed, int searchDistance, BiPredicate<BlockPos, IBlockState> cropFilter, BooleanSupplier active) {
        super(entity, movementSpeed, searchDistance);
        this.entity = entity;
        this.movementSpeed = movementSpeed;
        this.cropFilter = cropFilter;
        this.active = active;
        this.setMutexBits(7);
    }

    private void onCropEaten(BlockPos pos, IBlockState state) {
        if (state.getBlock() instanceof BlockCrops) {
            this.entity.world.setBlockState(pos, state.withProperty(BlockCrops.AGE, 0));
        } else {
            this.entity.world.destroyBlock(pos, false);
        }
    }

    public int getEatingTimer() {
        return eatingTimer;
    }

    @Override
    public boolean shouldExecute() {
        boolean value = super.shouldExecute();
        boolean active = this.active.getAsBoolean();
        if (active && value) {
            this.runDelay = 100 + this.entity.getRNG().nextInt(100);
        }
        return active && value;
    }

    @Override
    public void startExecuting() {
        super.startExecuting();
        this.eatingTimer = this.entity.getMaxEatTimer();
        this.timeoutCounter = 0;
        this.maxStayTicks = this.entity.getRNG().nextInt(this.entity.getRNG().nextInt(1200) + 1200) + 1200;
    }

    @Override
    protected boolean getIsAboveDestination() {
        return this.reachedCrop;
    }

    @Override
    public boolean shouldContinueExecuting() {
        return this.timeoutCounter >= -this.maxStayTicks && this.timeoutCounter <= 1200 && this.shouldMoveTo(this.entity.world, this.destinationBlock);
    }

    protected double getReachDistance() {
        return 1.5;
    }

    @Override
    public void updateTask() {
        if (this.entity.getDistanceSqToCenter(this.destinationBlock.up()) > getReachDistance()) {
            this.reachedCrop = false;
            ++this.timeoutCounter;
            if (this.timeoutCounter % 40 == 0) {
                this.entity.getNavigator().tryMoveToXYZ((double)((float)this.destinationBlock.getX()) + 0.5D, (double)(this.destinationBlock.getY() + 1), (double)((float)this.destinationBlock.getZ()) + 0.5D, this.movementSpeed);
            }
        } else {
            this.reachedCrop = true;
            this.entity.getNavigator().clearPath();
            --this.timeoutCounter;
        }

        this.entity.getLookHelper().setLookPosition((double)this.destinationBlock.getX() + 0.5, (double)(this.destinationBlock.getY() + 1), (double)this.destinationBlock.getZ() + 0.5, 10F, (float)this.entity.getVerticalFaceSpeed());

        if (this.getIsAboveDestination()) {
            if (this.eatingTimer == this.entity.getMaxEatTimer()) {
                this.entity.world.setEntityState(this.entity, (byte)10);
            }
            this.eatingTimer = Math.max(0, this.eatingTimer - 1);
            if (this.eatingTimer == 4) {
                World world = this.entity.world;
                BlockPos pos = this.destinationBlock.up();
                IBlockState state = world.getBlockState(pos);
                if (this.cropFilter.test(pos, state)) {
                    if (ForgeEventFactory.getMobGriefingEvent(this.entity.world, this.entity)) {
                        if (state.getBlock() instanceof BlockCrops) {
                            this.entity.world.playEvent(2001, pos, Block.getIdFromBlock(state.getBlock()));
                        }
                        this.onCropEaten(pos, state);
                    }

                    this.entity.onPlantEaten(state);
                }
            }
        }
    }

    @Override
    protected boolean shouldMoveTo(World world, BlockPos pos) {
        if (!this.active.getAsBoolean()) {
            return false;
        }
        pos = pos.up();
        IBlockState state = world.getBlockState(pos);
        return this.cropFilter.test(pos, state);
    }
}
