package ru.craftlogic.mixin.block;

import net.minecraft.block.BlockBush;
import net.minecraft.block.BlockDoublePlant;
import net.minecraft.block.SoundType;
import net.minecraft.block.properties.PropertyEnum;
import net.minecraft.block.state.IBlockState;
import net.minecraft.client.particle.ParticleManager;
import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.init.SoundEvents;
import net.minecraft.util.math.AxisAlignedBB;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.IBlockAccess;
import net.minecraft.world.World;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import ru.craftlogic.api.entity.Creature;

@Mixin(BlockDoublePlant.class)
public abstract class MixinBlockDoublePlant extends BlockBush {
    @Shadow @Final public static PropertyEnum<BlockDoublePlant.EnumBlockHalf> HALF;
    private static final AxisAlignedBB DOUBLE_UP_AABB = new AxisAlignedBB(0.1, 0.0, 0.1, 0.9, 2.0, 0.9);
    private static final AxisAlignedBB DOUBLE_DOWN_AABB = new AxisAlignedBB(0.1, -1.0, 0.1, 0.9, 1.0, 0.9);

    /**
     * @author Radviger
     * @reason Proper plant selection boxes
     */
    @Override
    public AxisAlignedBB getBoundingBox(IBlockState state, IBlockAccess blockAccessor, BlockPos pos) {
        IBlockState lower = blockAccessor.getBlockState(pos.down());
        if (lower.getBlock() == state.getBlock() && lower.getValue(HALF) == BlockDoublePlant.EnumBlockHalf.LOWER) {
            return DOUBLE_DOWN_AABB.offset(state.getOffset(blockAccessor, pos));
        } else {
            IBlockState upper = blockAccessor.getBlockState(pos.up());
            if (upper.getBlock() == state.getBlock() && upper.getValue(HALF) == BlockDoublePlant.EnumBlockHalf.UPPER) {
                return DOUBLE_UP_AABB.offset(state.getOffset(blockAccessor, pos));
            } else {
                return super.getBoundingBox(state, blockAccessor, pos);
            }
        }
    }

    /**
     * @author Radviger
     * @reason Plant passthrough sounds and entity slowdown
     */
    @Override
    public void onEntityCollision(World world, BlockPos pos, IBlockState state, Entity entity) {
        if (entity instanceof EntityLivingBase) {
            EntityLivingBase livingEntity = (EntityLivingBase)entity;

            if (world.getTotalWorldTime() % 12 == 0 && (entity.posX != entity.prevPosX || entity.posY != entity.prevPosY || entity.posZ != entity.prevPosZ)) {
                entity.playSound(SoundEvents.BLOCK_GRASS_HIT, SoundType.PLANT.getVolume() * 0.5f, SoundType.PLANT.getPitch() * 0.45f);
            }

            if (!((Creature)livingEntity).isJumping()) {
                float speedReductionHorizontal = 0.9F;
                float speedReductionVertical = 0.9F;
                entity.motionX *= speedReductionHorizontal;
                entity.motionY *= speedReductionVertical;
                entity.motionZ *= speedReductionHorizontal;
            }

            if (entity.isBeingRidden()) {
                for (Entity ent : entity.getPassengers()) {
                    onEntityCollision(world, pos, state, ent);
                }
            }
        }
    }

    @Override
    public boolean addDestroyEffects(World world, BlockPos pos, ParticleManager manager) {
        IBlockState upper = world.getBlockState(pos.up());
        if (upper.getBlock() == this && upper.getValue(HALF) == BlockDoublePlant.EnumBlockHalf.UPPER) {
            manager.addBlockDestroyEffects(pos.up(), upper);
        } else {
            IBlockState lower = world.getBlockState(pos.down());
            if (lower.getBlock() == this && lower.getValue(HALF) == BlockDoublePlant.EnumBlockHalf.LOWER) {
                manager.addBlockDestroyEffects(pos.down(), lower);
            }
        }
        return false;
    }
}
