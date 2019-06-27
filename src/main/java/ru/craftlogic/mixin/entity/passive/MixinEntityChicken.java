package ru.craftlogic.mixin.entity.passive;

import net.minecraft.entity.ai.EntityAIAvoidEntity;
import net.minecraft.entity.ai.EntityAIMate;
import net.minecraft.entity.ai.EntityAITasks;
import net.minecraft.entity.ai.EntityAITempt;
import net.minecraft.entity.passive.EntityAnimal;
import net.minecraft.entity.passive.EntityChicken;
import net.minecraft.entity.passive.EntityOcelot;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.init.Items;
import net.minecraft.init.SoundEvents;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.network.datasync.DataParameter;
import net.minecraft.network.datasync.DataSerializers;
import net.minecraft.network.datasync.EntityDataManager;
import net.minecraft.util.EnumHand;
import net.minecraft.util.EnumParticleTypes;
import net.minecraft.util.math.MathHelper;
import net.minecraft.world.World;
import net.minecraftforge.event.ForgeEventFactory;
import net.minecraftforge.fml.common.registry.EntityRegistry;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Overwrite;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import ru.craftlogic.api.entity.Chicken;
import ru.craftlogic.api.entity.ai.EntityAIMateBird;
import ru.craftlogic.util.ReflectiveUsage;

import javax.annotation.Nullable;
import java.util.List;
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

    @ReflectiveUsage
    @SideOnly(Side.CLIENT)
    private static void addEggInfo(NBTTagCompound compound, List<String> info, @Nullable World world) {
        info.add("Variant: " + compound.getInteger("variant"));
    }

    @Inject(method = "<init>", at = @At("RETURN"))
    protected void constructor(CallbackInfo info) {
        this.timeUntilNextEgg = -1;
        if (this.aiAvoidPlayer == null) {
            this.aiAvoidPlayer = new EntityAIAvoidEntity<>(this, EntityPlayer.class, 16F, 0.8, 1.33);
        }

        this.tasks.removeTask(this.aiAvoidPlayer);
        if (!this.isTamed()) {
            this.tasks.addTask(9, this.aiAvoidPlayer);
        }
        this.dataManager.set(VARIANT, this.rand.nextInt(ChickenVariant.values().length));
        this.setSize(0.6F, 0.7F);
    }

    @Override
    protected void entityInit() {
        super.entityInit();
        this.dataManager.register(VARIANT, 0);
        this.dataManager.register(TAMED, false);
    }

    @Inject(method = "initEntityAI", at = @At("TAIL"))
    protected void onAiInit(CallbackInfo info) {
        for (EntityAITasks.EntityAITaskEntry taskEntry : this.tasks.taskEntries) {
            if (taskEntry.action instanceof EntityAITempt && taskEntry.priority == 3) {
                this.aiTempt = (EntityAITempt) taskEntry.action;
                break;
            }
        }
        this.tasks.taskEntries.removeIf(entry -> entry.action instanceof EntityAIMate);
        this.tasks.addTask(2, new EntityAIMateBird<>(this, 1.0));
        this.tasks.addTask(8, new EntityAIAvoidEntity<>(this, EntityOcelot.class, 16F, 0.6, 1.33));
    }

    /**
     * @author Radviger
     * @reason Egg laying only when fed
     */
    @Override
    @Overwrite
    public void onLivingUpdate() {
        super.onLivingUpdate();
        this.oFlap = this.wingRotation;
        this.oFlapSpeed = this.destPos;
        this.destPos = (float)((double)this.destPos + (double)(this.onGround ? -1 : 4) * 0.3);
        this.destPos = MathHelper.clamp(this.destPos, 0F, 1F);
        if (!this.onGround && this.wingRotDelta < 1F) {
            this.wingRotDelta = 1F;
        }

        this.wingRotDelta = (float)((double)this.wingRotDelta * 0.9);
        if (!this.onGround && this.motionY < 0.0) {
            this.motionY *= 0.6;
        }

        this.wingRotation += this.wingRotDelta * 2F;
        if (!this.world.isRemote && !this.isChild() && !this.isRooster() && !this.isChickenJockey() && this.timeUntilNextEgg > 0) {
            if (--this.timeUntilNextEgg == 0) {
                if (this.possibleEggs > 0) {
                    --this.possibleEggs;
                    this.timeUntilNextEgg = this.world.rand.nextInt(6000) + 6000;
                } else {
                    this.timeUntilNextEgg = -1;
                }
                this.playSound(SoundEvents.ENTITY_CHICKEN_EGG, 1F, (this.rand.nextFloat() - this.rand.nextFloat()) * 0.2F + 1F);
                ItemStack egg = new ItemStack(Items.EGG);
                NBTTagCompound compound = new NBTTagCompound();
                NBTTagCompound data = egg.getOrCreateSubCompound("BirdData");
                data.setString("id", EntityRegistry.getEntry(getClass()).getRegistryName().toString());
                data.setInteger("variant", this.getVariant().ordinal());
                compound.setTag("BirdData", data);
                egg.setTagCompound(compound);
                this.entityDropItem(egg, 0F);
            }
        }
    }

    @Shadow
    public abstract boolean isChickenJockey();

    @Inject(method = "writeEntityToNBT", at = @At("TAIL"))
    public void onNBTWrite(NBTTagCompound compound, CallbackInfo info) {
        compound.setInteger("possibleEggs", this.possibleEggs);
        compound.setInteger("variant", this.getVariant().ordinal());
        compound.setBoolean("tamed", this.isTamed());
    }

    @Inject(method = "readEntityFromNBT", at = @At("TAIL"))
    public void onNBTRead(NBTTagCompound compound, CallbackInfo info) {
        this.possibleEggs = compound.getInteger("possibleEggs");
        this.dataManager.set(VARIANT, compound.getInteger("variant"));
        this.setTamed(compound.getBoolean("tamed"));
    }

    @Override
    public boolean processInteract(EntityPlayer player, EnumHand hand) {
        ItemStack heldItem = player.getHeldItem(hand);
        boolean tamed = this.isTamed();
        boolean fed = this.isInLove();
        if ((!tamed || !fed) && (this.aiTempt == null || this.aiTempt.isRunning()) && TEMPTATION_ITEMS.contains(heldItem.getItem()) && player.getDistanceSq(this) < 9.0) {
            if (!player.capabilities.isCreativeMode) {
                heldItem.shrink(1);
            }

            if (!this.world.isRemote) {
                if (this.rand.nextInt(3) == 0) {
                    if (!tamed && !ForgeEventFactory.onAnimalTame(this, player)) {
                        this.setTamed(true);
                    } else {
                        this.setInLove(player);
                    }
                    this.playTameEffect(true);
                } else {
                    this.playTameEffect(false);
                }
                if (this.possibleEggs > 0 && this.timeUntilNextEgg > 0 && this.world.rand.nextInt(3) == 0) {
                    this.timeUntilNextEgg = Math.max(0, this.timeUntilNextEgg - this.world.rand.nextInt(500) + 500);
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
            double dx = this.rand.nextGaussian() * 0.02;
            double dy = this.rand.nextGaussian() * 0.02;
            double dz = this.rand.nextGaussian() * 0.02;
            this.world.spawnParticle(particle, this.posX + (double)(this.rand.nextFloat() * this.width * 2F) - (double)this.width, this.posY + 0.5D + (double)(this.rand.nextFloat() * this.height), this.posZ + (double)(this.rand.nextFloat() * this.width * 2F) - (double)this.width, dx, dy, dz);
        }
    }

    @Override
    protected float getSoundPitch() {
        return this.isRooster() && !this.isChild() ? super.getSoundPitch() * 0.7F : super.getSoundPitch();
    }

    @Override
    public void updateAITasks() {
        if (this.getMoveHelper().isUpdating()) {
            double speed = this.getMoveHelper().getSpeed();
            if (speed == 1.33) {
                if (!this.isSprinting() && this.world.getTotalWorldTime() - this.lastScaredSound > 20L) {
                    this.lastScaredSound = this.world.getTotalWorldTime();
                    this.playSound(SoundEvents.ENTITY_CHICKEN_HURT, this.getSoundVolume(), this.getSoundPitch());
                }
                this.setSneaking(false);
                this.setSprinting(true);
            } else {
                this.setSneaking(false);
                this.setSprinting(false);
            }
        } else {
            this.setSneaking(false);
            this.setSprinting(false);
        }
    }

    @Override
    public ChickenVariant getVariant() {
        return ChickenVariant.values()[this.dataManager.get(VARIANT)];
    }

    @Override
    public boolean isRooster() {
        return getVariant() == ChickenVariant.ROOSTER;
    }

    @Override
    public boolean canMateWith(EntityAnimal other) {
        return super.canMateWith(other) && this.isRooster() != ((MixinEntityChicken) other).isRooster();
    }

    @Override
    public void setEggLayingDelay(int delay) {
        this.timeUntilNextEgg = delay;
    }

    @Override
    public int getEggLayingDelay() {
        return this.timeUntilNextEgg;
    }

    @Override
    public void setPossibleEggsCount(int possibleEggs) {
        this.possibleEggs = possibleEggs;
    }

    @Override
    public int getPossibleEggsCount() {
        return this.possibleEggs;
    }

    @Override
    public boolean isTamed() {
        return this.dataManager.get(TAMED);
    }

    @Override
    public void setTamed(boolean tamed) {
        this.dataManager.set(TAMED, tamed);
        this.tasks.removeTask(this.aiAvoidPlayer);
        if (!this.isTamed()) {
            this.tasks.addTask(9, this.aiAvoidPlayer);
        }
    }
}
