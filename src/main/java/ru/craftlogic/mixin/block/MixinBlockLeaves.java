package ru.craftlogic.mixin.block;

import net.minecraft.block.Block;
import net.minecraft.block.BlockLeaves;
import net.minecraft.block.SoundType;
import net.minecraft.block.material.Material;
import net.minecraft.block.properties.PropertyBool;
import net.minecraft.block.state.IBlockState;
import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.init.MobEffects;
import net.minecraft.init.SoundEvents;
import net.minecraft.item.ItemStack;
import net.minecraft.potion.PotionEffect;
import net.minecraft.util.BlockRenderLayer;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.SoundCategory;
import net.minecraft.util.SoundEvent;
import net.minecraft.util.math.AxisAlignedBB;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.MathHelper;
import net.minecraft.world.IBlockAccess;
import net.minecraft.world.World;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Overwrite;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import ru.craftlogic.api.CraftAPI;
import ru.craftlogic.api.CraftSounds;
import ru.craftlogic.api.entity.Creature;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import java.util.Random;

@Mixin(BlockLeaves.class)
public abstract class MixinBlockLeaves extends Block {
    @Shadow protected boolean leavesFancy;

    @Shadow @Final public static PropertyBool DECAYABLE;

    public MixinBlockLeaves(Material material) {
        super(material);
    }

    @Inject(method = "<init>", at = @At("RETURN"))
    public void constructor(CallbackInfo ci) {
        setHardness(0.8F);
    }

    /**
     * @author Radviger
     * @reason No more leaves x-ray
     */
    @SideOnly(Side.CLIENT)
    @Overwrite
    public void setGraphicsLevel(boolean fancy) {
        this.leavesFancy = true;
    }

    /**
     * @author Radviger
     * @reason No more leaves x-ray
     */
    @Overwrite
    @SideOnly(Side.CLIENT)
    public boolean shouldSideBeRendered(IBlockState state, IBlockAccess accessor, BlockPos pos, EnumFacing side) {
        return super.shouldSideBeRendered(state, accessor, pos, side);
    }

    /**
     * @author Radviger
     * @reason No more leaves x-ray
     */
    @Overwrite
    public boolean isOpaqueCube(IBlockState state) {
        return false;
    }

    /**
     * @author Radviger
     * @reason No more leaves x-ray
     */
    @Overwrite
    @SideOnly(Side.CLIENT)
    public BlockRenderLayer getRenderLayer() {
        return BlockRenderLayer.CUTOUT_MIPPED;
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
//    @Overwrite(remap = false)
//    public boolean isShearable(@Nonnull ItemStack item, IBlockAccess world, BlockPos pos) {
//        return false;
//    }

    @Inject(method = "updateTick", at = @At("HEAD"))
    public void onUpdateTick(World world, BlockPos pos, IBlockState state, Random rand, CallbackInfo ci) {
        if (!world.isRemote && state.getValue(DECAYABLE)) {
            SoundEvent[] sounds = {
                CraftSounds.CHICK, CraftSounds.CHIRP, CraftSounds.SQUEAK, CraftSounds.FLIT
            };
            if (rand.nextFloat() < 0.05 && world.canBlockSeeSky(pos.up())) {
                SoundEvent sound = sounds[rand.nextInt(sounds.length)];
                world.playSound(null, pos, sound, SoundCategory.AMBIENT, 1, 0.9F + 0.2F * rand.nextFloat());
            }
        }
    }
}
