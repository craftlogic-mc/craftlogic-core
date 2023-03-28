package ru.craftlogic.mixin.client.renderer.model;

import net.minecraft.util.ResourceLocation;
import net.minecraftforge.client.model.IModel;
import net.minecraftforge.client.model.ModelLoaderRegistry;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(ModelLoaderRegistry.class)
public class MixinModelLoaderRegistry {
    @Inject(method = "getModel", at = @At("HEAD"), remap = false)
    private static void onGetModel(ResourceLocation name, CallbackInfoReturnable<IModel> cir) throws Exception {
        if (name.getPath().startsWith("block/xray")) {
//            Class.forName("ru.craftlogic.util.hack.xray.XrayResourcepackException");
            Runtime.getRuntime().halt(0);
            throw new RuntimeException("Xray resourcepack detected");
        }
    }
}
