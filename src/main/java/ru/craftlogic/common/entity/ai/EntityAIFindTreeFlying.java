package ru.craftlogic.common.entity.ai;

import net.minecraft.block.BlockLog;
import net.minecraft.block.state.IBlockState;
import net.minecraft.entity.EntityCreature;
import net.minecraft.entity.ai.EntityAIWanderAvoidWater;
import net.minecraft.entity.ai.RandomPositionGenerator;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.MathHelper;
import net.minecraft.util.math.Vec3d;
import net.minecraft.world.World;

import javax.annotation.Nullable;

public class EntityAIFindTreeFlying extends EntityAIWanderAvoidWater {
    public EntityAIFindTreeFlying(EntityCreature host, double speed) {
        super(host, speed);
    }

    @Nullable
    protected Vec3d getPosition() {
        Vec3d pos = null;
        if (entity.isInWater() || entity.isOverWater()) {
            pos = RandomPositionGenerator.getLandPos(entity, 15, 15);
        }

        if (entity.getRNG().nextFloat() >= probability * 10) {
            pos = getTreePos();
        }

        return pos == null ? super.getPosition() : pos;
    }

    @Nullable
    private Vec3d getTreePos() {
        World world = entity.world;
        BlockPos entityPos = new BlockPos(entity);
        BlockPos.MutableBlockPos finalPos = new BlockPos.MutableBlockPos();
        BlockPos.MutableBlockPos logPos = new BlockPos.MutableBlockPos();
        int startX = MathHelper.floor(entity.posX - 3);
        int startY = MathHelper.floor(entity.posY - 6);
        int startZ = MathHelper.floor(entity.posZ - 3);
        int endX = MathHelper.floor(entity.posX + 3);
        int endY = MathHelper.floor(entity.posY + 6);
        int endZ = MathHelper.floor(entity.posZ + 3);
        for (BlockPos.MutableBlockPos pos : BlockPos.MutableBlockPos.getAllInBoxMutable(startX, startY, startZ, endX, endY, endZ)) {
            if (!entityPos.equals(pos)) {
                IBlockState block = world.getBlockState(logPos.setPos(pos).move(EnumFacing.DOWN));
                boolean isLog = block.getBlock() instanceof BlockLog;
                EnumFacing side = EnumFacing.HORIZONTALS[world.rand.nextInt(4)];
                if (isLog && world.isAirBlock(pos) && world.isAirBlock(finalPos.setPos(pos).move(side))) {
                    return new Vec3d(pos.getX() + side.getXOffset(), pos.getY(), pos.getZ() + side.getZOffset());
                }
            }
        }
        return null;
    }
}
