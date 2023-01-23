package ru.craftlogic.mixin.network;

import net.minecraft.entity.player.EntityPlayerMP;
import net.minecraft.network.NetHandlerPlayServer;
import net.minecraft.server.MinecraftServer;
import net.minecraft.util.math.MathHelper;
import net.minecraft.util.text.ITextComponent;
import net.minecraft.util.text.TextComponentTranslation;
import net.minecraft.util.text.TextFormatting;
import net.minecraftforge.common.MinecraftForge;
import org.apache.logging.log4j.Logger;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Overwrite;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.Redirect;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import ru.craftlogic.CraftConfig;
import ru.craftlogic.api.event.player.PlayerJoinedMessageEvent;
import ru.craftlogic.api.event.player.PlayerLeftMessageEvent;
import ru.craftlogic.api.network.AdvancedNetHandlerPlayServer;
import ru.craftlogic.api.server.Server;

@Mixin(NetHandlerPlayServer.class)
public class MixinNetHandlerPlayServer implements AdvancedNetHandlerPlayServer {
    @Shadow @Final
    private static Logger LOGGER;
    @Shadow @Final
    private MinecraftServer server;
    @Shadow
    public EntityPlayerMP player;

    /**
     * @author Radviger
     * @reason Ability to edit/disable player leaving messages
     */
    @Overwrite
    public void onDisconnect(ITextComponent reason) {
        LOGGER.info("{} lost connection: {}", this.player.getName(), reason.getUnformattedText());
        this.server.refreshStatusNextTick();
        TextComponentTranslation message = new TextComponentTranslation("multiplayer.player.left", this.player.getDisplayName());
        PlayerLeftMessageEvent event = new PlayerLeftMessageEvent(this.player, message);
        if (!MinecraftForge.EVENT_BUS.post(event)) {
            message.getStyle().setColor(TextFormatting.YELLOW);
            this.server.getPlayerList().sendMessage(message);
        }
        this.player.mountEntityAndWakeUp();
        this.server.getPlayerList().playerLoggedOut(this.player);
        if (this.server.isSinglePlayer() && this.player.getName().equals(this.server.getServerOwner())) {
            LOGGER.info("Stopping singleplayer server as player logged out");
            this.server.initiateShutdown();
        }
    }

    @Redirect(method = "processCustomPayload", at = @At(value = "INVOKE", target = "Lnet/minecraft/util/math/MathHelper;clamp(III)I"))
    protected int onClampStructSize(int value, int min, int max) {
        if (min == -32) min = -CraftConfig.tweaks.maxStructureSize;
        if (max == 32) max = CraftConfig.tweaks.maxStructureSize;
        return MathHelper.clamp(value, min, max);
    }

    @Redirect(method = "processChatMessage", at = @At(value = "INVOKE", target = "Ljava/lang/String;startsWith(Ljava/lang/String;)Z"))
    protected boolean onCheckForSlashCommand(String value, String prefix) {
        return value.startsWith(prefix) || value.matches("\\.[\u0410-\u044F\u0401\u0451]+($|\\s)");
    }

    @Override
    public MinecraftServer getServer() {
        return server;
    }

    @Override
    public void resetPosition() {
        this.captureCurrentPosition();
    }

    @Shadow
    private void captureCurrentPosition() {}

    @Inject(method = "update", at = @At("HEAD"))
    private void onUpdateStart(CallbackInfo ci) {
        Server.from(server).currentlyProcessingPlayer(player.getUniqueID());
    }

    @Inject(method = "update", at = @At("RETURN"))
    private void onUpdateEnd(CallbackInfo ci) {
        Server.from(server).currentlyProcessingPlayer(null);
    }
}
