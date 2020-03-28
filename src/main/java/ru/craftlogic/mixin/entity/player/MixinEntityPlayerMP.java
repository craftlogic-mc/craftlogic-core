package ru.craftlogic.mixin.entity.player;

import com.mojang.authlib.GameProfile;
import net.minecraft.entity.Entity;
import net.minecraft.entity.player.EntityPlayerMP;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.management.UserListOpsEntry;
import net.minecraft.world.World;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Overwrite;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import ru.craftlogic.api.entity.AdvancedPlayer;
import ru.craftlogic.api.permission.PermissionManager;
import ru.craftlogic.api.server.Server;
import ru.craftlogic.api.world.Player;

@Mixin(EntityPlayerMP.class)
public abstract class MixinEntityPlayerMP extends Entity implements AdvancedPlayer {
    @Shadow
    private long playerLastActiveTime;
    @Shadow @Final public MinecraftServer server;
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

    @Inject(method = "copyFrom", at = @At(value = "INVOKE", target = "Lnet/minecraftforge/event/ForgeEventFactory;onPlayerClone(Lnet/minecraft/entity/player/EntityPlayer;Lnet/minecraft/entity/player/EntityPlayer;Z)V", remap = false))
    public void onCopy(EntityPlayerMP from, boolean death, CallbackInfo info) {
        AdvancedPlayer oldAp = (AdvancedPlayer) from;
        this.setFirstPlayed(oldAp.getFirstPlayed());
        this.setTimePlayed(oldAp.getTimePlayed());
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
        return Server.from(this.server).getPlayerManager().getOnline(this.getUniqueID());
    }

    /**
     * @author Radviger
     * @reason command permissions
     */
    @Overwrite
    public boolean canUseCommand(int permLevel, String commandName) {
        GameProfile profile = ((EntityPlayerMP) (Object) this).getGameProfile();
        Server server = Server.from(this.server);
        PermissionManager permissionManager = server.getPermissionManager();
        if (permissionManager.isEnabled()) {
            return permissionManager.hasPermission(profile, "commands." + commandName);
        } else {
            switch (commandName) {
                case "tell":
                case "help":
                case "me":
                case "trigger":
                    return true;
                case "seed":
                    if (!this.server.isDedicatedServer()) {
                        return true;
                    }
                default:
                    if (this.server.getPlayerList().canSendCommands(profile)) {
                        UserListOpsEntry opEntry = this.server.getPlayerList().getOppedPlayers().getEntry(profile);

                        if (opEntry != null) {
                            return opEntry.getPermissionLevel() >= permLevel;
                        } else {
                            return this.server.getOpPermissionLevel() >= permLevel;
                        }
                    } else {
                        return false;
                    }
            }
        }
    }
}
