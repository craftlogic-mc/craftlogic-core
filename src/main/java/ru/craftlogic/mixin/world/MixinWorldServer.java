package ru.craftlogic.mixin.world;

import net.minecraft.world.WorldServer;
import net.minecraftforge.common.MinecraftForge;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import ru.craftlogic.api.event.world.WorldChunksSaveEvent;
import ru.craftlogic.api.event.world.WorldFlushToDiskEvent;

@Mixin(WorldServer.class)
public class MixinWorldServer {
    @Inject(method = "saveAllChunks", at = @At("HEAD"))
    public void onSaveAllChunks(CallbackInfo ci) {
        MinecraftForge.EVENT_BUS.post(new WorldChunksSaveEvent((WorldServer) (Object) this));
    }

    @Inject(method = "flushToDisk", at = @At("HEAD"))
    public void onFlushToDisk(CallbackInfo ci) {
        MinecraftForge.EVENT_BUS.post(new WorldFlushToDiskEvent((WorldServer) (Object) this));
    }
}
