package ru.craftlogic.mixin.entity.passive;

import net.minecraft.block.BlockCrops;
import net.minecraft.entity.Entity;
import net.minecraft.entity.SharedMonsterAttributes;
import net.minecraft.entity.ai.*;
import net.minecraft.entity.passive.EntityAnimal;
import net.minecraft.entity.passive.EntityPig;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.init.Blocks;
import net.minecraft.init.Items;
import net.minecraft.init.SoundEvents;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.network.datasync.DataParameter;
import net.minecraft.network.datasync.DataSerializers;
import net.minecraft.network.datasync.EntityDataManager;
import net.minecraft.util.DamageSource;
import net.minecraft.util.EnumHand;
import net.minecraft.util.EnumParticleTypes;
import net.minecraft.util.SoundEvent;
import net.minecraft.world.World;
import net.minecraftforge.event.ForgeEventFactory;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Overwrite;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import ru.craftlogic.api.entity.Pig;
import ru.craftlogic.common.entity.ai.EntityAIEatCrops;

import javax.annotation.Nullable;

@Mixin(EntityPig.class)
public abstract class MixinEntityPig extends EntityAnimal implements Pig {
    @Shadow public abstract boolean getSaddled();

    private static final DataParameter<Boolean> TAMED = EntityDataManager.createKey(EntityPig.class, DataSerializers.BOOLEAN);

    private long lastScaredSound;
    private int eatTimer;
    private EntityAIEatCrops cropsEatAI;
    private EntityAITempt aiTempt;
    private EntityAIAvoidEntity<EntityPlayer> aiAvoidPlayer;

    public MixinEntityPig(World world) {
        super(world);
    }

    @Inject(method = "<init>", at = @At("RETURN"))
    protected void constructor(CallbackInfo info) {
        if (aiAvoidPlayer == null) {
            aiAvoidPlayer = new EntityAIAvoidEntity<>(this, EntityPlayer.class, 16F, 0.8, 1.33);
        }

        tasks.removeTask(aiAvoidPlayer);
        if (!isTamed()) {
            tasks.addTask(9, aiAvoidPlayer);
        }
    }

    @Inject(method = "entityInit", at = @At("RETURN"))
    protected void onEntityInit(CallbackInfo ci) {
        dataManager.register(TAMED, false);
    }

    @Inject(method = "initEntityAI", at = @At("RETURN"))
    protected void onAiInit(CallbackInfo info) {
        cropsEatAI = new EntityAIEatCrops<>(this, 1.2F, 16, (pos, state) ->
            state.getBlock() == Blocks.POTATOES && ((BlockCrops)state.getBlock()).isMaxAge(state)
            || state.getBlock() == Blocks.CARROTS && ((BlockCrops)state.getBlock()).isMaxAge(state)
            || state.getBlock() == Blocks.BEETROOTS && ((BlockCrops)state.getBlock()).isMaxAge(state)
            || state.getBlock() == Blocks.RED_FLOWER
            || state.getBlock() == Blocks.YELLOW_FLOWER
            || state.getBlock() == Blocks.RED_MUSHROOM
            || state.getBlock() == Blocks.BROWN_MUSHROOM
        , () -> true);
        tasks.taskEntries.removeIf(entry -> entry.action instanceof EntityAIPanic);
        tasks.addTask(9, cropsEatAI);
        for (EntityAITasks.EntityAITaskEntry taskEntry : tasks.taskEntries) {
            if (taskEntry.action instanceof EntityAITempt && taskEntry.priority == 3) {
                aiTempt = (EntityAITempt) taskEntry.action;
                break;
            }
        }
    }

    @Inject(method = "applyEntityAttributes", at = @At("TAIL"))
    protected void onApplyAttributes(CallbackInfo ci) {
        getAttributeMap().registerAttribute(SharedMonsterAttributes.ATTACK_DAMAGE).setBaseValue(1.8);
    }

    @Override
    public boolean attackEntityAsMob(Entity victim) {
        boolean attack = victim.attackEntityFrom(DamageSource.causeMobDamage(this), (float)((int)getEntityAttribute(SharedMonsterAttributes.ATTACK_DAMAGE).getAttributeValue()));
        if (attack) {
            applyEnchantments(this, victim);
        }

        return attack;
    }

    /**
     * @author Radviger
     * @reason Custom pigs
     */
    @Overwrite
    public boolean processInteract(EntityPlayer player, EnumHand hand) {
        ItemStack heldItem = player.getHeldItem(hand);
        boolean tamed = isTamed();
        boolean fed = isInLove();
        if ((!tamed || !fed) && (aiTempt == null || aiTempt.isRunning()) && heldItem.getItem() == Items.WHEAT && player.getDistanceSq(this) < 9.0) {
            if (!player.capabilities.isCreativeMode) {
                heldItem.shrink(1);
            }

            if (!world.isRemote) {
                if (rand.nextInt(3) == 0) {
                    if (!tamed && !ForgeEventFactory.onAnimalTame(this, player)) {
                        setTamed(true);
                    } else {
                        setInLove(player);
                    }
                    playTameEffect(true);
                } else {
                    playTameEffect(false);
                }
            }

            return true;
        } else {
            if (heldItem.getItem() == Items.NAME_TAG) {
                heldItem.interactWithEntity(player, this, hand);
                return true;
            } else if (getSaddled() && !this.isBeingRidden()) {
                if (!this.world.isRemote) {
                    player.startRiding(this);
                }

                return true;
            } else if (heldItem.getItem() == Items.SADDLE) {
                heldItem.interactWithEntity(player, this, hand);
                return true;
            } else {
                return super.processInteract(player, hand);
            }
        }
    }

    private void playTameEffect(boolean success) {
        EnumParticleTypes particle = EnumParticleTypes.HEART;
        if (!success) {
            particle = EnumParticleTypes.SMOKE_NORMAL;
        }

        for(int i = 0; i < 7; ++i) {
            double dx = rand.nextGaussian() * 0.02;
            double dy = rand.nextGaussian() * 0.02;
            double dz = rand.nextGaussian() * 0.02;
            world.spawnParticle(particle, posX + (double)(rand.nextFloat() * width * 2F) - (double)width, posY + 0.5D + (double)(rand.nextFloat() * height), posZ + (double)(rand.nextFloat() * width * 2F) - (double)width, dx, dy, dz);
        }
    }

    /**
     * @author Radviger
     * @reason Custom pigs
     */
    @Nullable
    @Overwrite
    protected SoundEvent getAmbientSound() {
        return getAttackTarget() != null ? SoundEvents.ENTITY_PIG_HURT : SoundEvents.ENTITY_PIG_AMBIENT;
    }

    @Override
    protected void updateAITasks() {
        this.eatTimer = this.cropsEatAI.getEatingTimer();
        super.updateAITasks();
        if (getMoveHelper().isUpdating()) {
            double speed = getMoveHelper().getSpeed();
            if (speed == 1.33) {
                if (!isSprinting() && world.getTotalWorldTime() - lastScaredSound > 20L) {
                    lastScaredSound = world.getTotalWorldTime();
                    playSound(SoundEvents.ENTITY_PIG_HURT, getSoundVolume(), getSoundPitch());
                }
                setSneaking(false);
                setSprinting(true);
            } else {
                setSneaking(false);
                setSprinting(false);
            }
        } else {
            setSneaking(false);
            setSprinting(false);
        }
    }

    @Override
    public void onLivingUpdate() {
        if (this.world.isRemote) {
            this.eatTimer = Math.max(0, this.eatTimer - 1);
        }

        super.onLivingUpdate();
    }

    @Override
    @SideOnly(Side.CLIENT)
    public void handleStatusUpdate(byte status) {
        if (status == 10) {
            this.eatTimer = getMaxEatTimer();
        } else {
            super.handleStatusUpdate(status);
        }
    }

    @Override
    public int getEatTimer() {
        return eatTimer;
    }

    @Override
    public int getMaxEatTimer() {
        return 40;
    }

    @Override
    public float getRotationPitch() {
        return rotationPitch;
    }

    @Inject(method = "writeEntityToNBT", at = @At("TAIL"))
    public void onNBTWrite(NBTTagCompound compound, CallbackInfo info) {
        compound.setBoolean("tamed", isTamed());
    }

    @Inject(method = "readEntityFromNBT", at = @At("TAIL"))
    public void onNBTRead(NBTTagCompound compound, CallbackInfo info) {
        setTamed(compound.getBoolean("tamed"));
        if (!isChild()) {
            tasks.addTask(8, new EntityAIAttackMelee(this, 1.5, true));
        }
        targetTasks.addTask(1, new EntityAIHurtByTarget(this, isChild()));
    }

    @Override
    public boolean isTamed() {
        return dataManager.get(TAMED);
    }

    @Override
    public void setTamed(boolean tamed) {
        dataManager.set(TAMED, tamed);
        tasks.removeTask(aiAvoidPlayer);
        if (!isTamed()) {
            tasks.addTask(9, aiAvoidPlayer);
        }
    }
}
