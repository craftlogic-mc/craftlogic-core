package ru.craftlogic.mixin.entity.monster;

import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.entity.IEntityLivingData;
import net.minecraft.entity.ai.EntityAIAttackMelee;
import net.minecraft.entity.ai.EntityAIAttackRangedBow;
import net.minecraft.entity.ai.EntityAIZombieAttack;
import net.minecraft.entity.monster.EntityMob;
import net.minecraft.entity.monster.EntityZombie;
import net.minecraft.entity.projectile.EntityArrow;
import net.minecraft.entity.projectile.EntityTippedArrow;
import net.minecraft.init.Items;
import net.minecraft.init.MobEffects;
import net.minecraft.init.SoundEvents;
import net.minecraft.inventory.EntityEquipmentSlot;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.network.datasync.DataParameter;
import net.minecraft.network.datasync.DataSerializers;
import net.minecraft.network.datasync.EntityDataManager;
import net.minecraft.potion.PotionEffect;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.MathHelper;
import net.minecraft.world.DifficultyInstance;
import net.minecraft.world.EnumDifficulty;
import net.minecraft.world.World;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;
import ru.craftlogic.api.entity.Zombie;
import ru.craftlogic.common.entity.ai.EntityAICustomZombieAttack;

import javax.annotation.Nullable;
import java.util.Random;

@Mixin(EntityZombie.class)
public abstract class MixinEntityZombie extends EntityMob implements Zombie {
    private static final DataParameter<Boolean> SWINGING_ARMS = EntityDataManager.createKey(EntityZombie.class, DataSerializers.BOOLEAN);
    private static final DataParameter<Float> SIZE = EntityDataManager.createKey(EntityZombie.class, DataSerializers.FLOAT);
    private static final DataParameter<Byte> VARIANT = EntityDataManager.createKey(EntityZombie.class, DataSerializers.BYTE);

    private final EntityAIAttackRangedBow aiArrowAttack = new EntityAIAttackRangedBow<>(this, 1, 20, 15);
    private final EntityAIAttackMelee aiAttackOnCollide = new EntityAICustomZombieAttack<>((EntityZombie & Zombie)(Object)this, 1.2, false);

    public MixinEntityZombie(World world) {
        super(world);
    }

    @Inject(method = "<init>", at = @At("RETURN"))
    public void constructor(World world, CallbackInfo info) {
        this.setCombatTask();
        if (!world.isRemote) {
            float size = world.rand.nextFloat() * 0.2F + 1F;
            this.multiplySize(size);
            this.dataManager.set(SIZE, size);
        }
    }

    @Inject(method = "entityInit", at = @At("RETURN"))
    protected void onInit(CallbackInfo info) {
        this.dataManager.register(SWINGING_ARMS, false);
        this.dataManager.register(SIZE, 1F);
        this.dataManager.register(VARIANT, (byte)0);
    }

    @Inject(method = "initEntityAI", at = @At("RETURN"))
    protected void onAiInit(CallbackInfo info) {
        this.tasks.taskEntries.removeIf(e -> e.action instanceof EntityAIZombieAttack);
    }

    @Inject(method = "notifyDataManagerChange", at = @At("RETURN"))
    public void dataChange(DataParameter<?> parameter, CallbackInfo info) {
        if (parameter == SIZE) {
            this.multiplySize(this.dataManager.get(SIZE));
        }
    }

    @Shadow @Final
    protected abstract void multiplySize(float modifier);

    @Override
    public ZombieVariant getVariant() {
        int variant = this.dataManager.get(VARIANT);
        return ZombieVariant.values()[variant];
    }

    @Override
    protected float getSoundPitch() {
        return super.getSoundPitch() / this.getRenderSizeModifier();
    }

    @SideOnly(Side.CLIENT)
    @Override
    public boolean isSwingingArms() {
        return this.dataManager.get(SWINGING_ARMS);
    }

    @Override
    public float getRenderSizeModifier() {
        return this.dataManager.get(SIZE);
    }

    @Override
    public void setSwingingArms(boolean swingingArms) {
        this.dataManager.set(SWINGING_ARMS, swingingArms);
    }

    @Inject(method = "attackEntityAsMob", at = @At("RETURN"))
    public void onMeleeAttack(Entity target, CallbackInfoReturnable<Boolean> info) {
        if (info.getReturnValue() && target instanceof EntityLivingBase) {
            float difficulty = this.world.getDifficultyForLocation(new BlockPos(this)).getAdditionalDifficulty();
            switch (this.getVariant()) {
                case PLAGUE:
                    ((EntityLivingBase) target).addPotionEffect(new PotionEffect(MobEffects.POISON, 140 * (int)difficulty));
                    break;
                case HUNGRY:
                    ((EntityLivingBase) target).addPotionEffect(new PotionEffect(MobEffects.HUNGER, 140 * (int)difficulty));
                    break;
            }
        }
    }

    @Override
    public void attackEntityWithRangedAttack(EntityLivingBase target, float damage) {
        EntityArrow arrow = this.getArrow(damage);
        double dx = target.posX - this.posX;
        double dy = target.getEntityBoundingBox().minY + (double)(target.height / 3.0F) - arrow.posY;
        double dz = target.posZ - this.posZ;
        double distance = (double) MathHelper.sqrt(dx * dx + dz * dz);
        arrow.shoot(dx, dy + distance * 0.20000000298023224D, dz, 1.6F, (float)(14 - this.world.getDifficulty().getDifficultyId() * 4));
        this.playSound(SoundEvents.ENTITY_SKELETON_SHOOT, 1.0F, 1.0F / (this.getRNG().nextFloat() * 0.4F + 0.8F));
        this.world.spawnEntity(arrow);
    }

    protected EntityArrow getArrow(float damage) {
        float difficulty = this.world.getDifficultyForLocation(new BlockPos(this)).getAdditionalDifficulty();
        EntityTippedArrow arrow = new EntityTippedArrow(this.world, this);
        arrow.setEnchantmentEffectsFromEntity(this, damage);
        switch (this.getVariant()) {
            case PLAGUE:
                arrow.addEffect(new PotionEffect(MobEffects.POISON, 140 * (int)difficulty));
                break;
            case HUNGRY:
                arrow.addEffect(new PotionEffect(MobEffects.HUNGER, 140 * (int)difficulty));
                break;
        }
        return arrow;
    }

    public void setCombatTask() {
        if (this.world != null && !this.world.isRemote) {
            this.tasks.removeTask(this.aiAttackOnCollide);
            this.tasks.removeTask(this.aiArrowAttack);
            ItemStack heldItem = this.getHeldItemMainhand();
            if (heldItem.getItem() == Items.BOW) {
                int cooldown = this.world.getDifficulty() == EnumDifficulty.HARD ? 20 : 40;
                this.aiArrowAttack.setAttackCooldown(cooldown);
                this.tasks.addTask(2, this.aiArrowAttack);
            } else {
                this.tasks.addTask(2, this.aiAttackOnCollide);
            }
        }
    }

    @Inject(method = "readEntityFromNBT", at = @At("RETURN"))
    public void onNbtRead(NBTTagCompound compound, CallbackInfo info) {
        if (compound.hasKey("size")) {
            this.dataManager.set(SIZE, compound.getFloat("size"));
        }
        this.dataManager.set(VARIANT, compound.getByte("variant"));
        this.setCombatTask();
    }

    @Inject(method = "writeEntityToNBT", at = @At("RETURN"))
    public void onNbtWrite(NBTTagCompound compound, CallbackInfo info) {
        compound.setFloat("size", this.getRenderSizeModifier());
        compound.setByte("variant", (byte) this.getVariant().ordinal());
    }

    @Override
    public void setItemStackToSlot(EntityEquipmentSlot slot, ItemStack stack) {
        super.setItemStackToSlot(slot, stack);
        if (!this.world.isRemote && slot == EntityEquipmentSlot.MAINHAND) {
            this.setCombatTask();
        }
    }

    @Override
    protected void setEquipmentBasedOnDifficulty(DifficultyInstance difficulty) {
        super.setEquipmentBasedOnDifficulty(difficulty);
        if (this.rand.nextInt(this.getVariant().getBowRarity()) == 0) {
            this.setItemStackToSlot(EntityEquipmentSlot.MAINHAND, new ItemStack(Items.BOW));
        }
    }

    @Inject(method = "onInitialSpawn", at = @At("RETURN"))
    public void onSpawned(DifficultyInstance difficulty, @Nullable IEntityLivingData data, CallbackInfoReturnable<IEntityLivingData> info) {
        boolean isExactZombie = (Object)this.getClass() == EntityZombie.class;
        Random rand = this.world.rand;
        int variant = isExactZombie && rand.nextInt(5) == 0 ? rand.nextInt(ZombieVariant.values().length) : 0;
        this.dataManager.set(VARIANT, (byte) variant);
        this.setEquipmentBasedOnDifficulty(difficulty);
        this.setEnchantmentBasedOnDifficulty(difficulty);
        this.setCombatTask();
    }
}
