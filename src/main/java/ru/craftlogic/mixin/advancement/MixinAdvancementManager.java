package ru.craftlogic.mixin.advancement;

import net.minecraft.advancements.AdvancementManager;
import net.minecraft.util.ResourceLocation;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Redirect;

import java.util.Map;

@Mixin(AdvancementManager.class)
public class MixinAdvancementManager {
    @Redirect(method = "loadBuiltInAdvancements", at = @At(value = "INVOKE", target = "Ljava/util/Map;containsKey(Ljava/lang/Object;)Z", remap = false))
    private boolean isBuildInAdvancementLoaded(Map<Object, Object> map, Object key) {
        if (key instanceof ResourceLocation) {
            ResourceLocation id = (ResourceLocation) key;
            if (id.getNamespace().equals("minecraft")) { //Omit vanilla advancements
                return true;
            }
        }
        return map.containsKey(key);
    }
}
