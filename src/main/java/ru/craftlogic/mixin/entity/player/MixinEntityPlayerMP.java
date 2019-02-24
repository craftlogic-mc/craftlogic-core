package ru.craftlogic.mixin.entity.player;

import net.minecraft.entity.Entity;
import net.minecraft.entity.player.EntityPlayerMP;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.server.MinecraftServer;
import net.minecraft.world.World;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import ru.craftlogic.api.entity.AdvancedPlayer;
import ru.craftlogic.api.server.Server;
import ru.craftlogic.api.world.Player;

@Mixin(EntityPlayerMP.class)
public abstract class MixinEntityPlayerMP extends Entity implements AdvancedPlayer {
    @Shadow
    private long playerLastActiveTime;
    @Shadow @Final public MinecraftServer mcServer;
    private long firstPlayed;
    private long timePlayed;

    public MixinEntityPlayerMP(World world) {
        super(world);
    }

    @Inject(method = "readEntityFromNBT", at = @At("RETURN"))
    public void onRead(NBTTagCompound compound, CallbackInfo info) {
        this.firstPlayed = compound.getLong("firstPlayed");
        this.playerLastActiveTime = compound.getLong("lastPlayed");
        this.timePlayed = compound.getLong("timePlayed");
    }

    @Inject(method = "writeEntityToNBT", at = @At("RETURN"))
    public void onSave(NBTTagCompound compound, CallbackInfo info) {
        compound.setLong("firstPlayed", this.firstPlayed);
        compound.setLong("lastPlayed", this.playerLastActiveTime);
        compound.setLong("timePlayed", this.timePlayed);
    }

    @Override
    public long getFirstPlayed() {
        return firstPlayed;
    }

    @Override
    public void setFirstPlayed(long firstPlayed) {
        this.firstPlayed = firstPlayed;
    }

    @Override
    public void setTimePlayed(long timePlayed) {
        this.timePlayed = timePlayed;
    }

    @Override
    public long getTimePlayed() {
        return timePlayed;
    }

    @Override
    public Player wrapped() {
        return Server.from(this.mcServer).getPlayerManager().getOnline(this.getUniqueID());
    }
}
