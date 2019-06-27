package ru.craftlogic.mixin.entity.monster;

import net.minecraft.entity.ai.EntityAICreeperSwell;
import net.minecraft.entity.monster.EntityCreeper;
import net.minecraft.entity.monster.EntityMob;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.init.SoundEvents;
import net.minecraft.item.ItemShears;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.util.EnumHand;
import net.minecraft.world.World;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import ru.craftlogic.api.CraftItems;
import ru.craftlogic.api.entity.Creeper;

@Mixin(EntityCreeper.class)
public abstract class MixinEntityCreeper extends EntityMob implements Creeper {
    @Shadow
    public abstract void setCreeperState(int state);

    @Shadow public abstract int getCreeperState();

    public MixinEntityCreeper(World world) {
        super(world);
    }

    @Override
    public boolean isDepressed() {
        return this.getCreeperState() == 0;
    }

    @Override
    protected boolean processInteract(EntityPlayer player, EnumHand hand) {
        if (!player.world.isRemote && !isDepressed()) {
            ItemStack heldItem = player.getHeldItem(hand);
            if (heldItem.getItem() instanceof ItemShears) {
                heldItem.damageItem(1, player);
                this.setCreeperState(0);
                this.tasks.taskEntries.removeIf(e -> e.action instanceof EntityAICreeperSwell);
                this.playSound(SoundEvents.ENTITY_CREEPER_HURT, 1F, 0.5F);
                this.dropItem(CraftItems.CREEPER_OYSTERS, 1);
                return true;
            }
        }
        return false;
    }

    @Inject(method = "readEntityFromNBT", at = @At("RETURN"))
    public void onNbtRead(NBTTagCompound compound, CallbackInfo info) {
        if (compound.getBoolean("depressed")) {
            this.setCreeperState(0);
        }
    }

    @Inject(method = "writeEntityToNBT", at = @At("RETURN"))
    public void onNbtWrite(NBTTagCompound compound, CallbackInfo info) {
        compound.setBoolean("depressed", isDepressed());
    }
}
