package ru.craftlogic.mixin.block;

import net.minecraft.block.Block;
import net.minecraft.block.BlockLeaves;
import net.minecraft.block.SoundType;
import net.minecraft.block.material.Material;
import net.minecraft.block.state.IBlockState;
import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.init.MobEffects;
import net.minecraft.init.SoundEvents;
import net.minecraft.item.ItemStack;
import net.minecraft.potion.PotionEffect;
import net.minecraft.util.math.AxisAlignedBB;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.MathHelper;
import net.minecraft.world.IBlockAccess;
import net.minecraft.world.World;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Overwrite;
import ru.craftlogic.api.CraftAPI;
import ru.craftlogic.api.entity.Creature;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;

@Mixin(BlockLeaves.class)
public abstract class MixinBlockLeaves extends Block {
    public MixinBlockLeaves(Material material) {
        super(material);
    }

    @Override
    public boolean isPassable(IBlockAccess world, BlockPos pos) {
        return true;
    }

    @Nullable
    @Override
    public AxisAlignedBB getCollisionBoundingBox(IBlockState state, IBlockAccess blockAccessor, BlockPos pos) {
        return null;
    }

    @Override
    public void onEntityCollision(World world, BlockPos pos, IBlockState state, Entity entity) {
        if (entity instanceof EntityLivingBase) {
            EntityLivingBase livingEntity = (EntityLivingBase)entity;

            if (livingEntity.fallDistance > 3F) {
                entity.playSound(SoundEvents.BLOCK_GRASS_BREAK, SoundType.PLANT.getVolume() * 0.6f, SoundType.PLANT.getPitch() * 0.65f);
            } else if (world.getTotalWorldTime() % 6 == 0 && (entity.posX != entity.prevPosX || entity.posY != entity.prevPosY || entity.posZ != entity.prevPosZ)) {
                entity.playSound(SoundEvents.BLOCK_GRASS_HIT, SoundType.PLANT.getVolume() * 0.5f, SoundType.PLANT.getPitch() * 0.45f);
            }

            if (!((Creature)livingEntity).isJumping()) {
                float speedReductionHorizontal = 0.9F;
                float speedReductionVertical = 0.9F;
                entity.motionX *= speedReductionHorizontal;
                entity.motionY *= speedReductionVertical;
                entity.motionZ *= speedReductionHorizontal;
            }

            int fallDamageThreshold = 20;
            if (livingEntity.fallDistance > fallDamageThreshold) {
                livingEntity.fallDistance -= fallDamageThreshold;
                PotionEffect pe = livingEntity.getActivePotionEffect(MobEffects.JUMP_BOOST);
                float fallDamageReduction = 0.5F;
                int amount = MathHelper.ceil(livingEntity.fallDistance * fallDamageReduction * ((pe == null) ? 1F : 0.9F));
                livingEntity.attackEntityFrom(CraftAPI.DAMAGE_SOURCE_FALL_INTO_LEAVES, amount);
            }

            if (livingEntity.fallDistance > 1F) { livingEntity.fallDistance = 1F; }

            if (entity.isBeingRidden()) {
                for (Entity ent : entity.getPassengers()) {
                    onEntityCollision(world, pos, state, ent);
                }
            }
        }
    }

    /**
     * @author Radviger
     * @reason No shearable leaves
     */
    @Overwrite(remap = false)
    public boolean isShearable(@Nonnull ItemStack item, IBlockAccess world, BlockPos pos) {
        return false;
    }
}
