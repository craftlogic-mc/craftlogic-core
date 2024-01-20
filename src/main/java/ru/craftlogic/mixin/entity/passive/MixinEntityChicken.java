package ru.craftlogic.mixin.entity.passive;

import net.minecraft.entity.Entity;
import net.minecraft.entity.SharedMonsterAttributes;
import net.minecraft.entity.ai.*;
import net.minecraft.entity.passive.EntityAnimal;
import net.minecraft.entity.passive.EntityChicken;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.init.Items;
import net.minecraft.init.SoundEvents;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.network.datasync.DataParameter;
import net.minecraft.network.datasync.DataSerializers;
import net.minecraft.network.datasync.EntityDataManager;
import net.minecraft.util.DamageSource;
import net.minecraft.util.EnumHand;
import net.minecraft.util.EnumParticleTypes;
import net.minecraft.util.SoundEvent;
import net.minecraft.util.math.MathHelper;
import net.minecraft.world.World;
import net.minecraftforge.event.ForgeEventFactory;
import net.minecraftforge.fml.common.registry.EntityRegistry;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Overwrite;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import ru.craftlogic.CraftConfig;
import ru.craftlogic.api.entity.Chicken;
import ru.craftlogic.api.entity.ai.EntityAIMateBird;

import javax.annotation.Nullable;
import java.util.Set;

@Mixin(EntityChicken.class)
public abstract class MixinEntityChicken extends EntityAnimal implements Chicken {
    private static final DataParameter<Integer> VARIANT = EntityDataManager.createKey(EntityChicken.class, DataSerializers.VARINT);
    private static final DataParameter<Boolean> TAMED = EntityDataManager.createKey(EntityChicken.class, DataSerializers.BOOLEAN);
    @Shadow @Final
    private static Set<Item> TEMPTATION_ITEMS;
    private EntityAITempt aiTempt;
    private EntityAIAvoidEntity<EntityPlayer> aiAvoidPlayer;
    @Shadow
    public int timeUntilNextEgg;
    private int possibleEggs;
    private long lastScaredSound;
    @Shadow
    public float wingRotation, destPos, oFlapSpeed, oFlap, wingRotDelta;

    public MixinEntityChicken(World world) {
        super(world);
    }

    @Inject(method = "<init>", at = @At("RETURN"))
    protected void constructor(CallbackInfo info) {
        timeUntilNextEgg = -1;
        if (aiAvoidPlayer == null) {
            aiAvoidPlayer = new EntityAIAvoidEntity<>(this, EntityPlayer.class, 16F, 0.8, 1.33);
        }

        tasks.removeTask(aiAvoidPlayer);
        if (!isTamed()) {
            tasks.addTask(9, aiAvoidPlayer);
        }
        dataManager.set(VARIANT, rand.nextInt(ChickenVariant.values().length));
        setSize(0.6F, 0.7F);
    }

    @Override
    protected void entityInit() {
        super.entityInit();
        dataManager.register(VARIANT, 0);
        dataManager.register(TAMED, false);
    }

    @Inject(method = "initEntityAI", at = @At("TAIL"))
    protected void onAiInit(CallbackInfo info) {
        for (EntityAITasks.EntityAITaskEntry taskEntry : tasks.taskEntries) {
            if (taskEntry.action instanceof EntityAITempt && taskEntry.priority == 3) {
                aiTempt = (EntityAITempt) taskEntry.action;
                break;
            }
        }
        tasks.taskEntries.removeIf(entry -> entry.action instanceof EntityAIMate || entry.action instanceof EntityAIPanic);
        tasks.addTask(2, new EntityAIMateBird<>(this, 1));
        if (!isChild()) {
            tasks.addTask(8, new EntityAIAttackMelee(this, 1.5, false));
        }
        targetTasks.addTask(1, new EntityAIHurtByTarget(this, true));
    }

    @Inject(method = "applyEntityAttributes", at = @At("TAIL"))
    protected void onApplyAttributes(CallbackInfo ci) {
        getAttributeMap().registerAttribute(SharedMonsterAttributes.ATTACK_DAMAGE).setBaseValue(1);
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
     * @reason Egg laying only when fed
     */
    @Override
    @Overwrite
    public void onLivingUpdate() {
        super.onLivingUpdate();
        oFlap = wingRotation;
        oFlapSpeed = destPos;
        destPos = (float)((double)destPos + (double)(onGround ? -1 : 4) * 0.3);
        destPos = MathHelper.clamp(destPos, 0F, 1F);
        if (!onGround && wingRotDelta < 1F) {
            wingRotDelta = 1F;
        }

        wingRotDelta = (float)((double)wingRotDelta * 0.9);
        if (!onGround && motionY < 0.0) {
            motionY *= 0.6;
        }

        wingRotation += wingRotDelta * 2F;
        if (!world.isRemote && !isChild() && !isRooster() && !isChickenJockey() && timeUntilNextEgg > 0) {
            if (--timeUntilNextEgg == 0) {
                if (possibleEggs > 0) {
                    --possibleEggs;
                    int delay = CraftConfig.tweaks.chickenEggLayDelay;
                    timeUntilNextEgg = world.rand.nextInt(delay) + delay;
                } else {
                    timeUntilNextEgg = -1;
                }
                playSound(SoundEvents.ENTITY_CHICKEN_EGG, 1F, (rand.nextFloat() - rand.nextFloat()) * 0.2F + 1F);
                ItemStack egg = new ItemStack(Items.EGG);
                NBTTagCompound compound = new NBTTagCompound();
                NBTTagCompound data = egg.getOrCreateSubCompound("BirdData");
                data.setString("id", EntityRegistry.getEntry(getClass()).getRegistryName().toString());
                data.setInteger("variant", getVariant().ordinal());
                compound.setTag("BirdData", data);
                egg.setTagCompound(compound);
                entityDropItem(egg, 0F);
            }
        }
    }

    @Shadow
    public abstract boolean isChickenJockey();

    @Inject(method = "writeEntityToNBT", at = @At("TAIL"))
    public void onNBTWrite(NBTTagCompound compound, CallbackInfo info) {
        compound.setInteger("possibleEggs", possibleEggs);
        compound.setInteger("variant", getVariant().ordinal());
        compound.setBoolean("tamed", isTamed());
    }

    @Inject(method = "readEntityFromNBT", at = @At("TAIL"))
    public void onNBTRead(NBTTagCompound compound, CallbackInfo info) {
        possibleEggs = compound.getInteger("possibleEggs");
        dataManager.set(VARIANT, compound.getInteger("variant"));
        setTamed(compound.getBoolean("tamed"));
    }

    @Override
    public boolean processInteract(EntityPlayer player, EnumHand hand) {
        ItemStack heldItem = player.getHeldItem(hand);
        boolean tamed = isTamed();
        boolean fed = isInLove();
        if ((!tamed || !fed) && (aiTempt == null || aiTempt.isRunning()) && TEMPTATION_ITEMS.contains(heldItem.getItem()) && player.getDistanceSq(this) < 9.0) {
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
                if (possibleEggs > 0 && timeUntilNextEgg > 0 && world.rand.nextInt(3) == 0) {
                    timeUntilNextEgg = Math.max(0, timeUntilNextEgg - world.rand.nextInt(500) + 500);
                }
            }

            return true;
        }

        return super.processInteract(player, hand);
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
     * @reason angry chickens
     */
    @Nullable
    @Overwrite
    protected SoundEvent getAmbientSound() {
        return getAttackTarget() != null ? SoundEvents.ENTITY_CHICKEN_HURT : SoundEvents.ENTITY_CHICKEN_AMBIENT;
    }

    @Override
    protected float getSoundPitch() {
        return isRooster() && !isChild() ? super.getSoundPitch() * 0.7F : super.getSoundPitch();
    }

    @Override
    public void updateAITasks() {
        if (getMoveHelper().isUpdating()) {
            double speed = getMoveHelper().getSpeed();
            if (speed == 1.33) {
                if (!isSprinting() && world.getTotalWorldTime() - lastScaredSound > 20L) {
                    lastScaredSound = world.getTotalWorldTime();
                    playSound(SoundEvents.ENTITY_CHICKEN_HURT, getSoundVolume(), getSoundPitch());
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
    public ChickenVariant getVariant() {
        return ChickenVariant.values()[dataManager.get(VARIANT)];
    }

    @Override
    public boolean isRooster() {
        return getVariant() == ChickenVariant.ROOSTER;
    }

    @Override
    public boolean canMateWith(EntityAnimal other) {
        return super.canMateWith(other) && isRooster() != ((MixinEntityChicken) other).isRooster();
    }

    @Override
    public void setEggLayingDelay(int delay) {
        timeUntilNextEgg = delay;
    }

    @Override
    public int getEggLayingDelay() {
        return timeUntilNextEgg;
    }

    @Override
    public void setPossibleEggsCount(int possibleEggs) {
        this.possibleEggs = possibleEggs;
    }

    @Override
    public int getPossibleEggsCount() {
        return possibleEggs;
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
