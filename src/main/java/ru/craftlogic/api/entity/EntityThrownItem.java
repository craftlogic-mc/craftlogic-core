package ru.craftlogic.api.entity;

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

import java.util.HashMap;
import java.util.Map;
import java.util.function.BiConsumer;

public class EntityThrownItem extends EntityThrowable {
    private static final Map<Item, BiConsumer<EntityThrownItem, RayTraceResult>> CALLBACKS = new HashMap<>();
    private static final Map<Item, Float> DAMAGES = new HashMap<>();

    private static final DataParameter<ItemStack> ITEM = EntityDataManager.createKey(EntityThrownItem.class, DataSerializers.ITEM_STACK);

    private ItemStack item;

    public EntityThrownItem(World world) {
        super(world);
    }

    public EntityThrownItem(World world, EntityLivingBase thrower, ItemStack item) {
        super(world, thrower);
        this.item = item;
        this.dataManager.set(ITEM, item);
    }

    public EntityThrownItem(World world, double x, double y, double z, ItemStack item) {
        super(world, x, y, z);
        this.item = item;
        this.dataManager.set(ITEM, item);
    }

    public static void registerThrowable(Item item, float damage, BiConsumer<EntityThrownItem, RayTraceResult> callback) {
        CALLBACKS.put(item, callback);
        DAMAGES.put(item, damage);
    }

    public static boolean isThrowable(Item item) {
        return CALLBACKS.containsKey(item);
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
        Item item = this.dataManager.get(ITEM).getItem();
        if (DAMAGES.containsKey(item)) {
            if (target.entityHit != null) {
                target.entityHit.attackEntityFrom(DamageSource.causeThrownDamage(this, this.getThrower()), DAMAGES.get(item));
            }
        }

        if (!this.world.isRemote) {
            BiConsumer<EntityThrownItem, RayTraceResult> callback = CALLBACKS.get(item);
            if (callback != null) {
                callback.accept(this, target);
            }
            this.world.setEntityState(this, (byte)3);
            this.setDead();
        }
    }
}