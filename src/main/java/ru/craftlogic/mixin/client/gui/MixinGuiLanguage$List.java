package ru.craftlogic.mixin.client.gui;

import net.minecraft.client.Minecraft;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Redirect;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(targets = "net/minecraft/client/gui/GuiLanguage$List")
public class MixinGuiLanguage$List {
    @Redirect(method = "elementClicked(IZII)V", at = @At(value = "INVOKE", target = "Lnet/minecraft/client/Minecraft;refreshResources()V"))
    protected void refreshResources(Minecraft mc, CallbackInfo info) {
        mc.getLanguageManager().onResourceManagerReload(mc.getResourceManager());
    }
}
