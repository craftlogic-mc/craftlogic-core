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
import org.spongepowered.asm.mixin.injection.Redirect;
import ru.craftlogic.CraftConfig;
import ru.craftlogic.api.event.player.PlayerJoinedMessageEvent;

@Mixin(NetHandlerPlayServer.class)
public class MixinNetHandlerPlayServer {
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
        PlayerJoinedMessageEvent event = new PlayerJoinedMessageEvent(this.player, message);
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
}
