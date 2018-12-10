package ru.craftlogic.mixin.tileentity;

import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.tileentity.TileEntityDispenser;
import net.minecraft.tileentity.TileEntityLockableLoot;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;
import ru.craftlogic.api.tile.Ownable;

import java.util.UUID;

@Mixin(TileEntityDispenser.class)
public abstract class MixinTileEntityDispenser extends TileEntityLockableLoot implements Ownable {
    private UUID owner;

    @Override
    public UUID getOwner() {
        return this.owner;
    }

    @Override
    public void setOwner(UUID owner) {
        this.owner = owner;
    }

    @Inject(method = "readFromNBT", at = @At("RETURN"))
    private void onNBTRead(NBTTagCompound compound, CallbackInfo info) {
        if (compound.hasKey("Owner")) {
            this.owner = compound.getUniqueId("Owner");
        }
    }

    @Inject(method = "writeToNBT", at = @At("RETURN"))
    private void onNBTWrite(NBTTagCompound compound, CallbackInfoReturnable<NBTTagCompound> info) {
        if (this.owner != null) {
            compound.setUniqueId("Owner", this.owner);
        }
    }
}
