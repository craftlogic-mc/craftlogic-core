package ru.craftlogic.mixin.entity.passive;

import net.minecraft.block.BlockCrops;
import net.minecraft.entity.Entity;
import net.minecraft.entity.SharedMonsterAttributes;
import net.minecraft.entity.ai.*;
import net.minecraft.entity.passive.EntityAnimal;
import net.minecraft.entity.passive.EntityCow;
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
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import ru.craftlogic.api.entity.Cow;
import ru.craftlogic.common.entity.ai.EntityAIEatCrops;
import ru.craftlogic.common.entity.ai.EntityAIEatGrassAdvanced;

import javax.annotation.Nullable;

@Mixin(EntityCow.class)
public abstract class MixinEntityCow extends EntityAnimal implements Cow {
    private static final DataParameter<Integer> MILK = EntityDataManager.createKey(EntityCow.class, DataSerializers.VARINT);
    private static final DataParameter<Boolean> TAMED = EntityDataManager.createKey(EntityCow.class, DataSerializers.BOOLEAN);

    private int eatTimer;
    private long lastScaredSound;
    private EntityAIEatGrass grassEatAI;
    private EntityAIEatCrops<MixinEntityCow> cropsEatAI;
    private EntityAITempt aiTempt;
    private EntityAIAvoidEntity<EntityPlayer> aiAvoidPlayer;

    public MixinEntityCow(World world) {
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

    @Override
    protected void entityInit() {
        super.entityInit();
        dataManager.register(MILK, 0);
        dataManager.register(TAMED, false);
    }

    @Inject(method = "initEntityAI", at = @At("RETURN"))
    protected void onAiInit(CallbackInfo info) {
        grassEatAI = new EntityAIEatGrassAdvanced<>(this, true, () -> !this.hasMilk());
        cropsEatAI = new EntityAIEatCrops<>(this, 1.2F, 16, (pos, state) ->
            state.getBlock() == Blocks.RED_FLOWER
                || state.getBlock() == Blocks.YELLOW_FLOWER
                || state.getBlock() == Blocks.WHEAT && state.getValue(BlockCrops.AGE) == ((BlockCrops) Blocks.WHEAT).getMaxAge()
            , () -> !this.hasMilk());
        tasks.addTask(8, grassEatAI);
        tasks.addTask(9, cropsEatAI);
        tasks.taskEntries.removeIf(entry -> entry.action instanceof EntityAIPanic);
        for (EntityAITasks.EntityAITaskEntry taskEntry : tasks.taskEntries) {
            if (taskEntry.action instanceof EntityAITempt && taskEntry.priority == 3) {
                aiTempt = (EntityAITempt) taskEntry.action;
                break;
            }
        }
    }

    @Inject(method = "applyEntityAttributes", at = @At("TAIL"))
    protected void onApplyAttributes(CallbackInfo ci) {
        getAttributeMap().registerAttribute(SharedMonsterAttributes.ATTACK_DAMAGE).setBaseValue(2.5);
    }

    @Override
    public boolean attackEntityAsMob(Entity victim) {
        boolean attack = victim.attackEntityFrom(DamageSource.causeMobDamage(this), (float)((int)getEntityAttribute(SharedMonsterAttributes.ATTACK_DAMAGE).getAttributeValue()));
        if (attack) {
            applyEnchantments(this, victim);
        }

        return attack;
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

    @Override
    public boolean hasMilk() {
        return dataManager.get(MILK) >= 1000;
    }

    /**
     * @author Radviger
     * @reason Custom cows
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
        }
        if (heldItem.getItem() == Items.BUCKET && !player.capabilities.isCreativeMode && !isChild()) {
            int milk = dataManager.get(MILK);
            if (milk >= 1000) {
                dataManager.set(MILK, milk - 1000);
                player.playSound(SoundEvents.ENTITY_COW_MILK, 1F, 1F);
                heldItem.shrink(1);
                if (heldItem.isEmpty()) {
                    player.setHeldItem(hand, new ItemStack(Items.MILK_BUCKET));
                } else if (!player.inventory.addItemStackToInventory(new ItemStack(Items.MILK_BUCKET))) {
                    player.dropItem(new ItemStack(Items.MILK_BUCKET), false);
                }
            } else {
                player.playSound(SoundEvents.ENTITY_COW_HURT, 1F, 1F);
            }

            return true;
        } else {
            return super.processInteract(player, hand);
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
     * @reason Custom cows
     */
    @Nullable
    @Overwrite
    protected SoundEvent getAmbientSound() {
        return getAttackTarget() != null ? SoundEvents.ENTITY_COW_HURT : SoundEvents.ENTITY_COW_AMBIENT;
    }

    @Override
    public void updateAITasks() {
        this.eatTimer = grassEatAI.getEatingGrassTimer() + cropsEatAI.getEatingTimer();
        super.updateAITasks();
        if (getMoveHelper().isUpdating()) {
            double speed = getMoveHelper().getSpeed();
            if (speed == 1.33) {
                if (!isSprinting() && world.getTotalWorldTime() - lastScaredSound > 20L) {
                    lastScaredSound = world.getTotalWorldTime();
                    playSound(SoundEvents.ENTITY_COW_HURT, getSoundVolume(), getSoundPitch());
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
    public void eatGrassBonus() {
        if (!isChild()) {
            this.dataManager.set(MILK, Math.min(1000, dataManager.get(MILK) + 500));
        }
    }

    @Override
    public int getTalkInterval() {
        return hasMilk() ? 40 : 120;
    }

    @Override
    public void readEntityFromNBT(NBTTagCompound compound) {
        super.readEntityFromNBT(compound);
        setTamed(compound.getBoolean("tamed"));
        if (!isChild()) {
            tasks.addTask(8, new EntityAIAttackMelee(this, 1.5, true));
        }
        targetTasks.addTask(1, new EntityAIHurtByTarget(this, isChild()));
    }

    @Override
    public void writeEntityToNBT(NBTTagCompound compound) {
        super.writeEntityToNBT(compound);
        compound.setBoolean("tamed", isTamed());
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
