package ru.craftlogic.mixin.client.gui;

import net.minecraft.client.Minecraft;
import net.minecraftforge.client.resource.IResourceType;
import net.minecraftforge.fml.client.FMLClientHandler;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Redirect;

@Mixin(targets = "net/minecraft/client/gui/GuiLanguage$List")
public class MixinGuiLanguage$List {
    @Redirect(method = "elementClicked", at = @At(value = "INVOKE", target = "Lnet/minecraftforge/fml/client/FMLClientHandler;refreshResources ([Lnet/minecraftforge/client/resource/IResourceType;)V", remap = false))
    public void refreshResources(FMLClientHandler self, IResourceType[] res) {
        Minecraft mc = self.getClient();
        mc.getLanguageManager().onResourceManagerReload(mc.getResourceManager());
    }
}
