package ru.craftlogic.common.entity;

import com.mojang.authlib.GameProfile;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.entity.player.EntityPlayer;
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
import net.minecraft.world.WorldServer;
import net.minecraftforge.common.util.FakePlayerFactory;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;
import ru.craftlogic.api.item.ThrowableItem;

import javax.annotation.Nullable;
import java.util.UUID;

public class EntityThrownItem extends EntityThrowable {
    private static final DataParameter<ItemStack> ITEM = EntityDataManager.createKey(EntityThrownItem.class, DataSerializers.ITEM_STACK);

    private boolean drop;
    private UUID owner;

    public EntityThrownItem(World world) {
        super(world);
    }

    public EntityThrownItem(World world, EntityLivingBase thrower, ItemStack item, boolean drop) {
        super(world, thrower);
        this.drop = drop;
        ItemStack i = item.copy();
        i.setCount(1);
        this.dataManager.set(ITEM, i);
        if (thrower instanceof EntityPlayer) {
            this.owner = thrower.getUniqueID();
        }
    }

    public EntityThrownItem(World world, double x, double y, double z, ItemStack item, boolean drop) {
        super(world, x, y, z);
        this.drop = drop;
        ItemStack i = item.copy();
        i.setCount(1);
        this.dataManager.set(ITEM, i);
    }

    @Override
    protected float getGravityVelocity() {
        ItemStack item = getItem();
        if (!item.isEmpty() && item.getItem() instanceof ThrowableItem) {
            return ((ThrowableItem) item.getItem()).getProjectileGravityVelocity(this);
        }
        return 0.06F;
    }

    @Override
    protected void entityInit() {
        super.entityInit();
        this.dataManager.register(ITEM, ItemStack.EMPTY);
    }

    public ItemStack getItem() {
        return this.dataManager.get(ITEM);
    }

    public void setItem(ItemStack item) {
        this.dataManager.set(ITEM, item);
    }

    @Override
    public void writeEntityToNBT(NBTTagCompound compound) {
        super.writeEntityToNBT(compound);
        compound.setTag("item", this.getItem().writeToNBT(new NBTTagCompound()));
        compound.setBoolean("drop", this.drop);
        if (this.owner != null) {
            compound.setString("owner", this.owner.toString());
        }
    }

    @Override
    public void readEntityFromNBT(NBTTagCompound compound) {
        super.readEntityFromNBT(compound);
        this.setItem(new ItemStack(compound.getCompoundTag("item")));
        this.drop = compound.getBoolean("drop");
        if (compound.hasKey("owner")) {
            this.owner = UUID.fromString(compound.getString("owner"));
        }
    }

    @SideOnly(Side.CLIENT)
    @Override
    public void handleStatusUpdate(byte id) {
        ItemStack item = getItem();
        if (id == 3) {
            double offset = 0.08D;

            for(int i = 0; i < 8; ++i) {
                this.world.spawnParticle(EnumParticleTypes.ITEM_CRACK, this.posX, this.posY, this.posZ, ((double)this.rand.nextFloat() - 0.5D) * offset, ((double)this.rand.nextFloat() - 0.5D) * offset, ((double)this.rand.nextFloat() - 0.5D) * offset, Item.getIdFromItem(item.getItem()), item.getMetadata());
            }
        }
    }

    @Override
    protected void onImpact(RayTraceResult target) {
        ItemStack item = getItem();
        if (!item.isEmpty()) {
            if (item.getItem() instanceof ThrowableItem) {
                EntityLivingBase thrower = this.getThrower();
                float damage = ((ThrowableItem) item.getItem()).getProjectileDamage(this, target);
                if (damage > 0) {
                    if (target.entityHit != null) {
                        target.entityHit.attackEntityFrom(DamageSource.causeThrownDamage(this, thrower), damage);
                    }
                }
            }

            if (!this.world.isRemote) {
                if (item.getItem() instanceof ThrowableItem && ((ThrowableItem) item.getItem()).onProjectileHit(this, target)) {
                    this.world.setEntityState(this, (byte) 3);
                } else if (this.drop) {
                    this.entityDropItem(item.copy(), 0F);
                }
                this.setDead();
            }
        }
    }

    @Nullable
    @Override
    public EntityLivingBase getThrower() {
        if (this.owner != null) {
            WorldServer world = (WorldServer) this.world;
            EntityPlayer player = world.getPlayerEntityByUUID(this.owner);
            return player != null ? player : FakePlayerFactory.get(world, new GameProfile(this.owner, null));
        }
        return super.getThrower();
    }

    @Override
    public void onUpdate() {
        if (this.getItem() == ItemStack.EMPTY) {
            System.out.println("Killing empty-stack throwable");
            this.setDead();
        } else {
            super.onUpdate();
        }
    }
}