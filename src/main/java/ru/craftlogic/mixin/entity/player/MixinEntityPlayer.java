package ru.craftlogic.mixin.entity.player;

import net.minecraft.block.Block;
import net.minecraft.enchantment.EnchantmentHelper;
import net.minecraft.entity.*;
import net.minecraft.entity.monster.EntityMob;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.entity.player.EntityPlayerMP;
import net.minecraft.entity.player.PlayerCapabilities;
import net.minecraft.init.MobEffects;
import net.minecraft.init.SoundEvents;
import net.minecraft.item.ItemStack;
import net.minecraft.item.ItemSword;
import net.minecraft.network.play.server.SPacketEntityVelocity;
import net.minecraft.stats.StatList;
import net.minecraft.util.DamageSource;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.EnumHand;
import net.minecraft.util.EnumParticleTypes;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.MathHelper;
import net.minecraft.world.World;
import net.minecraft.world.WorldServer;
import net.minecraftforge.common.ForgeHooks;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.event.ForgeEventFactory;
import net.minecraftforge.event.entity.player.CriticalHitEvent;
import net.minecraftforge.items.IItemHandler;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Overwrite;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.*;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;
import ru.craftlogic.CraftConfig;
import ru.craftlogic.api.event.player.PlayerBlockDamageEvent;
import ru.craftlogic.api.event.player.PlayerCheckCanEditEvent;
import ru.craftlogic.api.event.player.PlayerEnterCombat;
import ru.craftlogic.api.event.player.PlayerSneakEvent;
import ru.craftlogic.api.world.Player;

import java.util.Iterator;
import java.util.Random;

@Mixin(EntityPlayer.class)
public abstract class MixinEntityPlayer extends EntityLivingBase {
    @Shadow
    public PlayerCapabilities capabilities;

    @Shadow @Final private IItemHandler playerEquipmentHandler;

    @Shadow public float experience;

    public MixinEntityPlayer(World world) {
        super(world);
    }

    /**
     * @author Radviger
     * @reason More events
     */
    @Overwrite
    public boolean canPlayerEdit(BlockPos pos, EnumFacing side, ItemStack item) {
        if (MinecraftForge.EVENT_BUS.post(new PlayerCheckCanEditEvent((EntityPlayer) (Object) this, pos, side, item))) {
            return false;
        } else if (this.capabilities.allowEdit) {
            return true;
        } else if (item.isEmpty()) {
            return false;
        } else {
            BlockPos offsetPos = pos.offset(side.getOpposite());
            Block block = this.world.getBlockState(offsetPos).getBlock();
            return item.canPlaceOn(block) || item.canEditBlocks();
        }
    }

    @Override
    public void setSneaking(boolean sneaking) {
        MinecraftForge.EVENT_BUS.post(new PlayerSneakEvent((EntityPlayer) (Object) this, sneaking));
        super.setSneaking(sneaking);
    }

    @Override
    public void spawnRunningParticles() {
        if (getActivePotionEffect(MobEffects.INVISIBILITY) == null) {
            super.spawnRunningParticles();
        }
    }

    @ModifyConstant(method = "dropItem(Lnet/minecraft/item/ItemStack;ZZ)Lnet/minecraft/entity/item/EntityItem;", constant = @Constant(floatValue = 0.5F))
    public float itemVelocity(float old) {
        return 0.1F;
    }

    @ModifyConstant(method = "dropItem(Lnet/minecraft/item/ItemStack;ZZ)Lnet/minecraft/entity/item/EntityItem;", constant = @Constant(intValue = 40))
    public int itemPickUpDelay(int old) {
        return 20;
    }

    @Redirect(method = "attackTargetEntityWithCurrentItem", at = @At(value = "INVOKE", target = "Lnet/minecraft/world/WorldServer;spawnParticle(Lnet/minecraft/util/EnumParticleTypes;DDDIDDDD[I)V"))
    public void onSpawnDamageIndicator(WorldServer world, EnumParticleTypes particle, double x, double y, double z, int count, double vx, double vy, double vz, double velocity, int[] args) {
        if (!CraftConfig.tweaks.disableDamageParticles) {
            world.spawnParticle(particle, x, y, z, count, vx, vy, vz, velocity, args);
        }
    }

    /**
     * @author
     * @reason
     */

    @Overwrite
    public void attackTargetEntityWithCurrentItem(Entity targetEntity) {
        EntityPlayer player = (EntityPlayer) (Object) this;
        if (ForgeHooks.onPlayerAttackTarget(player, targetEntity)) {
            if (targetEntity.canBeAttackedWithItem() && !targetEntity.hitByEntity(this)) {
                float damage = (float) player.getEntityAttribute(SharedMonsterAttributes.ATTACK_DAMAGE).getAttributeValue();
                float sharpness;
                if (targetEntity instanceof EntityLivingBase) {
                    sharpness = EnchantmentHelper.getModifierForCreature(player.getHeldItemMainhand(), ((EntityLivingBase) targetEntity).getCreatureAttribute());
                } else {
                    sharpness = EnchantmentHelper.getModifierForCreature(player.getHeldItemMainhand(), EnumCreatureAttribute.UNDEFINED);
                }

                float cooldown = player.getCooledAttackStrength(0.5F);
                damage *= 0.2F + cooldown * cooldown * 0.8F;
                sharpness *= cooldown;
                player.resetCooldown();
                if (damage > 0.0F || sharpness > 0.0F) {
                    if (targetEntity instanceof EntityLivingBase) {
                        EntityLivingBase target = (EntityLivingBase) targetEntity;
                        if (target.isActiveItemStackBlocking()) {
                            PlayerBlockDamageEvent event = new PlayerBlockDamageEvent(player, (int) damage, (1 + damage) / 2);
                            MinecraftForge.EVENT_BUS.post(event);
                            damage = event.getBlocked();
                        }
                    }
                    boolean flag = cooldown > 0.9F;
                    boolean flag1 = false;
                    int i = 0;
                    i += EnchantmentHelper.getKnockbackModifier(player);
                    if (this.isSprinting() && flag) {
                        this.world.playSound(null, this.posX, this.posY, this.posZ, SoundEvents.ENTITY_PLAYER_ATTACK_KNOCKBACK, this.getSoundCategory(), 1.0F, 1.0F);
                        ++i;
                        flag1 = true;
                    }

                    boolean isCrit = flag && this.fallDistance > 0.0F && !this.onGround && !player.isOnLadder() && !this.isInWater() && !player.isPotionActive(MobEffects.BLINDNESS) && !this.isRiding() && targetEntity instanceof EntityLivingBase;
                    CriticalHitEvent hitResult = ForgeHooks.getCriticalHit(player, targetEntity, isCrit, isCrit ? 1.5F : 1.0F);
                    isCrit = hitResult != null;
                    if (isCrit) {
                        damage *= hitResult.getDamageModifier();
                    }

                    damage += sharpness;
                    boolean isSword = false;
                    double d0 = this.distanceWalkedModified - this.prevDistanceWalkedModified;
                    if (flag && !isCrit && !flag1 && this.onGround && d0 < (double) player.getAIMoveSpeed()) {
                        ItemStack itemstack = player.getHeldItem(EnumHand.MAIN_HAND);
                        if (itemstack.getItem() instanceof ItemSword) {
                            isSword = true;
                        }
                    }

                    float targetHeal = 0.0F;
                    boolean burn = false;
                    int fire = EnchantmentHelper.getFireAspectModifier(player);
                    if (targetEntity instanceof EntityLivingBase) {
                        targetHeal = ((EntityLivingBase) targetEntity).getHealth();
                        if (fire > 0 && !targetEntity.isBurning()) {
                            burn = true;
                            targetEntity.setFire(1);
                        }
                    }

                    double targetX = targetEntity.motionX;
                    double targetY = targetEntity.motionY;
                    double targetZ = targetEntity.motionZ;
                    boolean canAttack = targetEntity.attackEntityFrom(DamageSource.causePlayerDamage(player), damage);
                    if (canAttack) {
                        if (i > 0) {
                            if (targetEntity instanceof EntityLivingBase) {
                                ((EntityLivingBase) targetEntity).knockBack(this, (float) i * 0.5F, MathHelper.sin(this.rotationYaw * 0.017453292F), -MathHelper.cos(this.rotationYaw * 0.017453292F));
                            } else {
                                targetEntity.addVelocity(-MathHelper.sin(this.rotationYaw * 0.017453292F) * (float) i * 0.5F, 0.1, MathHelper.cos(this.rotationYaw * 0.017453292F) * (float) i * 0.5F);
                            }

                            this.motionX *= 0.6;
                            this.motionZ *= 0.6;
                            this.setSprinting(false);
                        }

                        if (isSword) {
                            float f3 = 1.0F + EnchantmentHelper.getSweepingDamageRatio(player) * damage;

                            for (EntityLivingBase entitylivingbase : this.world.getEntitiesWithinAABB(EntityMob.class, targetEntity.getEntityBoundingBox().grow(1.0, 0.25, 1.0))) {
                                if (entitylivingbase != player && entitylivingbase != targetEntity && !player.isOnSameTeam(entitylivingbase) && player.getDistanceSq(entitylivingbase) < 9.0) {
                                    entitylivingbase.knockBack(player, 0.4F, MathHelper.sin(player.rotationYaw * 0.017453292F), -MathHelper.cos(player.rotationYaw * 0.017453292F));
                                    entitylivingbase.attackEntityFrom(DamageSource.causePlayerDamage(player), f3);
                                }
                            }
                            player.spawnSweepParticles();
                            player.world.playSound(null, player.posX, player.posY, player.posZ, SoundEvents.ENTITY_PLAYER_ATTACK_SWEEP, player.getSoundCategory(), 1.0F, 1.0F);
                        }


                        if (targetEntity instanceof EntityPlayerMP && targetEntity.velocityChanged) {
                            ((EntityPlayerMP) targetEntity).connection.sendPacket(new SPacketEntityVelocity(targetEntity));
                            targetEntity.velocityChanged = false;
                            targetEntity.motionX = targetX;
                            targetEntity.motionY = targetY;
                            targetEntity.motionZ = targetZ;
                        }

                        if (isCrit) {
                            player.world.playSound(null, player.posX, player.posY, player.posZ, SoundEvents.ENTITY_PLAYER_ATTACK_CRIT, player.getSoundCategory(), 1.0F, 1.0F);
                            player.onCriticalHit(targetEntity);
                        }

                        if (!isCrit && !isSword) {
                            if (flag) {
                                player.world.playSound(null, player.posX, player.posY, player.posZ, SoundEvents.ENTITY_PLAYER_ATTACK_STRONG, player.getSoundCategory(), 1.0F, 1.0F);
                            } else {
                                player.world.playSound(null, player.posX, player.posY, player.posZ, SoundEvents.ENTITY_PLAYER_ATTACK_WEAK, player.getSoundCategory(), 1.0F, 1.0F);
                            }
                        }

                        if (sharpness > 0.0F) {
                            player.onEnchantmentCritical(targetEntity);
                        }

                        player.setLastAttackedEntity(targetEntity);
                        if (targetEntity instanceof EntityLivingBase) {
                            EnchantmentHelper.applyThornEnchantments((EntityLivingBase) targetEntity, player);
                        }

                        EnchantmentHelper.applyArthropodEnchantments(player, targetEntity);
                        ItemStack itemstack1 = player.getHeldItemMainhand();
                        Entity entity = targetEntity;
                        if (targetEntity instanceof MultiPartEntityPart) {
                            IEntityMultiPart ientitymultipart = ((MultiPartEntityPart) targetEntity).parent;
                            if (ientitymultipart instanceof EntityLivingBase) {
                                entity = (EntityLivingBase) ientitymultipart;
                            }
                        }

                        if (!itemstack1.isEmpty() && entity instanceof EntityLivingBase) {
                            ItemStack beforeHitCopy = itemstack1.copy();
                            itemstack1.hitEntity((EntityLivingBase) entity, player);
                            if (itemstack1.isEmpty()) {
                                ForgeEventFactory.onPlayerDestroyItem(player, beforeHitCopy, EnumHand.MAIN_HAND);
                                player.setHeldItem(EnumHand.MAIN_HAND, ItemStack.EMPTY);
                            }
                        }

                        if (targetEntity instanceof EntityLivingBase) {
                            float f5 = targetHeal - ((EntityLivingBase) targetEntity).getHealth();
                            player.addStat(StatList.DAMAGE_DEALT, Math.round(f5 * 10.0F));
                            if (fire > 0) {
                                targetEntity.setFire(fire * 4);
                            }

                            if (player.world instanceof WorldServer && f5 > 2.0F) {
                                int k = (int) ((double) f5 * 0.5);
                                ((WorldServer) player.world).spawnParticle(EnumParticleTypes.DAMAGE_INDICATOR, targetEntity.posX, targetEntity.posY + (double) (targetEntity.height * 0.5F), targetEntity.posZ, k, 0.1, 0.0, 0.1, 0.2, new int[0]);
                            }
                        }

                        player.addExhaustion(0.1F);
                    } else {
                        if (!world.isRemote) {
                            Player pl = Player.from((EntityPlayerMP) player);
                            pl.playSound(SoundEvents.ENTITY_PLAYER_ATTACK_NODAMAGE, 1.0f, 1.0f);
                        }
//                        player.world.playSound(null, player.posX, player.posY, player.posZ, SoundEvents.ENTITY_PLAYER_ATTACK_NODAMAGE, player.getSoundCategory(), 1.0F, 1.0F);
                        if (burn) {
                            targetEntity.extinguish();
                        }
                    }
                }
            }

        }
    }


    /**
     * @author Pudo
     * @reason Removable attack cooldown
     */
    @Inject(method = "getCooldownPeriod", at = @At("HEAD"), cancellable = true)
    public void getCooldownPeriod(CallbackInfoReturnable<Float> cir) {
        if (!CraftConfig.tweaks.enableAttackCooldown) {
            cir.setReturnValue(1F);
        }
    }
}
