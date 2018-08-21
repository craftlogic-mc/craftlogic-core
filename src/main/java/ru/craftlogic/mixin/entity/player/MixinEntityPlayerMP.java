package ru.craftlogic.mixin.entity.player;

import net.minecraft.entity.Entity;
import net.minecraft.entity.player.EntityPlayerMP;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.world.World;
import net.minecraftforge.common.MinecraftForge;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import ru.craftlogic.api.entity.Player;
import ru.craftlogic.api.event.player.PlayerSneakEvent;

@Mixin(EntityPlayerMP.class)
public abstract class MixinEntityPlayerMP extends Entity implements Player {
    @Shadow
    private long playerLastActiveTime;
    private long firstPlayed;

    public MixinEntityPlayerMP(World world) {
        super(world);
    }

    @Override
    public void setSneaking(boolean sneaking) {
        MinecraftForge.EVENT_BUS.post(new PlayerSneakEvent((EntityPlayerMP)(Object)this, sneaking));
        super.setSneaking(sneaking);
    }

    @Inject(method = "readEntityFromNBT", at = @At("RETURN"))
    public void onRead(NBTTagCompound compound, CallbackInfo info) {
        this.firstPlayed = compound.getLong("firstPlayed");
        this.playerLastActiveTime = compound.getLong("lastPlayed");
    }

    @Inject(method = "writeEntityToNBT", at = @At("RETURN"))
    public void onSave(NBTTagCompound compound, CallbackInfo info) {
        compound.setLong("firstPlayed", this.firstPlayed);
        compound.setLong("lastPlayed", this.playerLastActiveTime);
    }

    @Override
    public long getFirstPlayed() {
        return firstPlayed;
    }

    @Override
    public void setFirstPlayed(long firstPlayed) {
        this.firstPlayed = firstPlayed;
    }
}
