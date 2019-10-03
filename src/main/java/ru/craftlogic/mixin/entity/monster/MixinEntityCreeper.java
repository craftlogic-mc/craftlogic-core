package ru.craftlogic.mixin.entity.monster;

import net.minecraft.entity.ai.EntityAICreeperSwell;
import net.minecraft.entity.monster.EntityCreeper;
import net.minecraft.entity.monster.EntityMob;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.init.SoundEvents;
import net.minecraft.item.ItemShears;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.network.datasync.DataParameter;
import net.minecraft.network.datasync.DataSerializers;
import net.minecraft.network.datasync.EntityDataManager;
import net.minecraft.util.EnumHand;
import net.minecraft.world.World;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Overwrite;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import ru.craftlogic.api.CraftItems;
import ru.craftlogic.api.entity.Creeper;

@Mixin(EntityCreeper.class)
public abstract class MixinEntityCreeper extends EntityMob implements Creeper {
    @Shadow @Final private static DataParameter<Boolean> IGNITED;
    private static final DataParameter<Boolean> DEPRESSED = EntityDataManager.createKey(EntityCreeper.class, DataSerializers.BOOLEAN);

    @Shadow
    public abstract void setCreeperState(int state);

    @Shadow public abstract int getCreeperState();

    @Shadow public abstract boolean hasIgnited();

    @Shadow protected abstract void explode();

    @Shadow private int lastActiveTime;

    @Shadow private int timeSinceIgnited;

    @Shadow private int fuseTime;

    public MixinEntityCreeper(World world) {
        super(world);
    }

    @Inject(method = "entityInit", at = @At("RETURN"))
    protected void onInit(CallbackInfo info) {
        this.dataManager.register(DEPRESSED, false);
    }

    @Override
    public boolean isDepressed() {
        return this.dataManager.get(DEPRESSED);
    }

    @Override
    protected boolean processInteract(EntityPlayer player, EnumHand hand) {
        if (!player.world.isRemote && !isDepressed()) {
            ItemStack heldItem = player.getHeldItem(hand);
            if (heldItem.getItem() instanceof ItemShears) {
                heldItem.damageItem(1, player);
                this.setCreeperState(-1);
                this.dataManager.set(IGNITED, false);
                this.dataManager.set(DEPRESSED, true);
                this.tasks.taskEntries.removeIf(e -> e.action instanceof EntityAICreeperSwell);
                this.playSound(SoundEvents.ENTITY_CREEPER_HURT, 1F, 0.5F);
                this.dropItem(CraftItems.CREEPER_OYSTERS, 1);
                return true;
            }
        }
        return false;
    }

    /**
     * @author Radviger
     * @reason Depressed creepers
     */
    @Overwrite
    public void onUpdate() {
        if (isEntityAlive()) {
            lastActiveTime = timeSinceIgnited;

            if (isDepressed()) {
                setCreeperState(-1);
            } else if (hasIgnited()) {
                setCreeperState(1);
            }

            int i = getCreeperState();

            if (i > 0 && timeSinceIgnited == 0) {
                playSound(SoundEvents.ENTITY_CREEPER_PRIMED, 1F, 0.5F);
            }

            timeSinceIgnited += i;

            if (timeSinceIgnited < 0) {
                timeSinceIgnited = 0;
            }

            if (timeSinceIgnited >= fuseTime) {
                timeSinceIgnited = fuseTime;
                explode();
            }
        }

        super.onUpdate();
    }

    @Inject(method = "readEntityFromNBT", at = @At("RETURN"))
    public void onNbtRead(NBTTagCompound compound, CallbackInfo info) {
        if (compound.getBoolean("depressed")) {
            this.setCreeperState(-1);
            this.dataManager.set(IGNITED, false);
            this.dataManager.set(DEPRESSED, true);
        }
    }

    @Inject(method = "writeEntityToNBT", at = @At("RETURN"))
    public void onNbtWrite(NBTTagCompound compound, CallbackInfo info) {
        compound.setBoolean("depressed", isDepressed());
    }
}
