package ru.craftlogic.mixin.entity.item;

import net.minecraft.entity.Entity;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.entity.projectile.EntityArrow;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.world.World;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import java.util.UUID;

@Mixin(EntityArrow.class)
public abstract class MixinEntityArrow extends Entity {

    @Shadow public Entity shootingEntity;

    public MixinEntityArrow(World world) {
        super(world);
    }

    @Inject(method = "readEntityFromNBT", at = @At("RETURN"))
    public void onNbtRead(NBTTagCompound compound, CallbackInfo info) {
        if (compound.hasKey("shootingPlayer")) {
            UUID playerId = compound.getUniqueId("shootingPlayer");
            this.shootingEntity = this.world.getPlayerEntityByUUID(playerId);
        }
    }

    @Inject(method = "writeEntityToNBT", at = @At("RETURN"))
    public void onNbtWrite(NBTTagCompound compound, CallbackInfo info) {
        if (this.shootingEntity instanceof EntityPlayer) {
            compound.setUniqueId("shootingPlayer", this.shootingEntity.getUniqueID());
        }
    }
}
