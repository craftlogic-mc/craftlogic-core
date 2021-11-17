package ru.craftlogic.mixin.client;

import net.minecraft.client.Minecraft;
import net.minecraft.client.multiplayer.WorldClient;
import net.minecraft.profiler.Profiler;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Mutable;
import org.spongepowered.asm.mixin.Shadow;
import ru.craftlogic.util.AdvancedProfiler;

@Mixin(Minecraft.class)
public class MixinMinecraft {
    private static final long PROFILER_WARN_TIME_MS = 50L;
    @Shadow @Final @Mutable
    public Profiler profiler = new AdvancedProfiler(PROFILER_WARN_TIME_MS * 1000_000L);

    @Shadow public WorldClient world;

    /**
     * @author Angeline
     * Forces the client to process light updates before rendering the world. We inject before the call to the profiler
     * which designates the start of world rendering. This is a rather injection site.
     */
    /*@Inject(method = "runTick", at = @At(value = "CONSTANT", args = "stringValue=levelRenderer", shift = At.Shift.BY, by = -3))
    private void onRunTick(CallbackInfo ci) {
        this.profiler.endStartSection("lighting");

        ((LightingEngineProvider) this.world).getLightingEngine().processLightUpdates();
    }*/
}
