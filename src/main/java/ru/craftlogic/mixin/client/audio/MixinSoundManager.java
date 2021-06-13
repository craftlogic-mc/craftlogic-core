package ru.craftlogic.mixin.client.audio;

import net.minecraft.client.audio.SoundManager;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import paulscode.sound.SoundSystemConfig;

@Mixin(SoundManager.class)
public class MixinSoundManager {
    @Inject(method = "<init>", at = @At("RETURN"))
    public void constructor(CallbackInfo ci) {
        SoundSystemConfig.setNumberNormalChannels(Short.MAX_VALUE);
    }
}
