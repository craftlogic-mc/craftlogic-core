package ru.craftlogic.mixin.entity.monster;

import net.minecraft.entity.EntityLivingBase;
import net.minecraft.entity.monster.EntityMob;
import net.minecraft.entity.monster.EntitySpider;
import net.minecraft.init.SoundEvents;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.network.datasync.DataParameter;
import net.minecraft.network.datasync.DataSerializers;
import net.minecraft.network.datasync.EntityDataManager;
import net.minecraft.util.math.MathHelper;
import net.minecraft.world.World;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Overwrite;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import ru.craftlogic.api.entity.Spider;
import ru.craftlogic.common.entity.ai.EntityAISpiderRangedAttack;
import ru.craftlogic.common.entity.projectile.EntitySpiderSpit;

@Mixin(EntitySpider.class)
public abstract class MixinEntitySpider extends EntityMob implements Spider {
    private static final DataParameter<Float> SIZE = EntityDataManager.createKey(EntitySpider.class, DataSerializers.FLOAT);

    private float spiderWidth = -1F;
    private float spiderHeight;
    
    public MixinEntitySpider(World world) {
        super(world);
    }

    @Inject(method = "<init>", at = @At("RETURN"))
    public void constructor(World world, CallbackInfo info) {
        if (!world.isRemote) {
            float size = world.rand.nextFloat() * 0.6F + 0.4F;
            this.multiplySize(size);
            this.dataManager.set(SIZE, size);
        }
    }

    @Inject(method = "initEntityAI", at = @At("RETURN"))
    protected void onAiInit(CallbackInfo info) {
        this.tasks.addTask(4, new EntityAISpiderRangedAttack<>((EntitySpider & Spider) (Object) this, 1.25, 40, 20F));
    }

    @Inject(method = "entityInit", at = @At("RETURN"))
    protected void onInit(CallbackInfo info) {
        this.dataManager.register(SIZE, 1F);
    }

    @Override
    public void notifyDataManagerChange(DataParameter<?> parameter) {
        super.notifyDataManagerChange(parameter);
        if (parameter == SIZE) {
            float size = this.dataManager.get(SIZE);
            this.multiplySize(size);
        }
    }

    @Override
    protected final void setSize(float width, float height) {
        boolean flag = this.spiderWidth > 0F && this.spiderHeight > 0F;
        this.spiderWidth = width;
        this.spiderHeight = height;
        if (!flag) {
            this.multiplySize(1F);
        }

    }

    protected final void multiplySize(float modifier) {
        super.setSize(this.spiderWidth * modifier, this.spiderHeight * modifier);
    }

    @Override
    public float getRenderSizeModifier() {
        return this.dataManager.get(SIZE);
    }

    @Override
    public void readEntityFromNBT(NBTTagCompound compound) {
        super.readEntityFromNBT(compound);
        if (compound.hasKey("size")) {
            this.dataManager.set(SIZE, compound.getFloat("size"));
        }
    }

    @Override
    public void writeEntityToNBT(NBTTagCompound compound) {
        super.writeEntityToNBT(compound);
        compound.setFloat("size", this.getRenderSizeModifier());
    }

    /**
     * @author Radviger
     * @reason Random-sized spiders
     */
    @Overwrite
    public float getEyeHeight() {
        return 0.65F * this.dataManager.get(SIZE);
    }

    @Override
    public void setSwingingArms(boolean b) { }

    @Override
    public void attackEntityWithRangedAttack(EntityLivingBase target, float damage) {
        EntitySpiderSpit spit = new EntitySpiderSpit(this.world, (EntitySpider) (Object) this);
        double dx = target.posX - this.posX;
        double dy = target.getEntityBoundingBox().minY + (double)(target.height / 3F) - spit.posY;
        double dz = target.posZ - this.posZ;
        float speed = MathHelper.sqrt(dx * dx + dz * dz) * 0.2F;
        spit.shoot(dx, dy + (double)speed, dz, 1.5F, 10F);
        this.world.playSound(null, this.posX, this.posY, this.posZ, SoundEvents.ENTITY_LLAMA_SPIT, this.getSoundCategory(), 1F, 1F + (this.rand.nextFloat() - this.rand.nextFloat()) * 0.2F);
        this.world.spawnEntity(spit);
    }
}
