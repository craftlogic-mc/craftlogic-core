package ru.craftlogic.common.entity;

import net.minecraft.entity.EntityLivingBase;
import net.minecraft.entity.projectile.EntityThrowable;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.network.datasync.DataParameter;
import net.minecraft.network.datasync.DataSerializers;
import net.minecraft.network.datasync.EntityDataManager;
import net.minecraft.util.DamageSource;
import net.minecraft.util.EnumParticleTypes;
import net.minecraft.util.math.RayTraceResult;
import net.minecraft.world.World;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;
import ru.craftlogic.api.item.ThrowableItem;

public class EntityThrownItem extends EntityThrowable {
    private static final DataParameter<ItemStack> ITEM = EntityDataManager.createKey(EntityThrownItem.class, DataSerializers.ITEM_STACK);

    private ItemStack item;

    public EntityThrownItem(World world) {
        super(world);
    }

    public EntityThrownItem(World world, EntityLivingBase thrower, ItemStack item) {
        super(world, thrower);
        this.item = item.copy();
        this.item.setCount(1);
        this.dataManager.set(ITEM, this.item);
    }

    public EntityThrownItem(World world, double x, double y, double z, ItemStack item) {
        super(world, x, y, z);
        this.item = item.copy();
        this.item.setCount(1);
        this.dataManager.set(ITEM, this.item);
    }

    @Override
    protected float getGravityVelocity() {
        return 0.06F;
    }

    @Override
    protected void entityInit() {
        super.entityInit();
        this.dataManager.register(ITEM, this.item);
    }

    public ItemStack getItem() {
        return this.dataManager.get(ITEM);
    }

    @Override
    public void writeEntityToNBT(NBTTagCompound compound) {
        super.writeEntityToNBT(compound);
        NBTTagCompound item = new NBTTagCompound();
        this.item.writeToNBT(item);
        compound.setTag("item", item);
    }

    @Override
    public void readEntityFromNBT(NBTTagCompound compound) {
        super.readEntityFromNBT(compound);
        this.item = new ItemStack(compound.getCompoundTag("item"));
    }

    @SideOnly(Side.CLIENT)
    @Override
    public void handleStatusUpdate(byte id) {
        ItemStack item = this.dataManager.get(ITEM);
        if (id == 3) {
            double offset = 0.08D;

            for(int i = 0; i < 8; ++i) {
                this.world.spawnParticle(EnumParticleTypes.ITEM_CRACK, this.posX, this.posY, this.posZ, ((double)this.rand.nextFloat() - 0.5D) * offset, ((double)this.rand.nextFloat() - 0.5D) * offset, ((double)this.rand.nextFloat() - 0.5D) * offset, Item.getIdFromItem(item.getItem()), item.getMetadata());
            }
        }
    }

    @Override
    protected void onImpact(RayTraceResult target) {
        ItemStack item = this.dataManager.get(ITEM);
        ThrowableItem throwable = (ThrowableItem) item.getItem();
        EntityLivingBase thrower = this.getThrower();
        float damage = throwable.getProjectileDamage(this, target);
        if (damage > 0) {
            if (target.entityHit != null) {
                target.entityHit.attackEntityFrom(DamageSource.causeThrownDamage(this, thrower), damage);
            }
        }

        if (!this.world.isRemote) {
            if (throwable.onProjectileHit(this, target)) {
                this.world.setEntityState(this, (byte)3);
            } else {
                this.entityDropItem(item.copy(), 0F);
            }
            this.setDead();
        }
    }
}