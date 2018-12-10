package ru.craftlogic.mixin.client;

import net.minecraft.client.Minecraft;
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
    public Profiler mcProfiler = new AdvancedProfiler(PROFILER_WARN_TIME_MS * 1000_000L);
}
