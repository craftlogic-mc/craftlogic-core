package ru.craftlogic.mixin.client.renderer;

import net.minecraft.client.renderer.block.model.ModelBakery;
import net.minecraft.init.Blocks;
import net.minecraft.item.Item;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Redirect;

import java.util.List;
import java.util.Map;

@Mixin(ModelBakery.class)
public class MixinModelBakery {
    @Shadow @Final private Map<Item, List<String>> variantNames;

    @Redirect(method = "registerVariantNames", at = @At(value = "INVOKE", target = "Ljava/util/Map;put(Ljava/lang/Object;Ljava/lang/Object;)Ljava/lang/Object;", remap = false))
    public Object onRegisterDefaultVariant(Map<Object, Object> registry, Object key, Object value) {
        if ((Map<?, ?>)registry == this.variantNames) {
            if (key == Item.getItemFromBlock(Blocks.CARPET)) {
                return null;
            }
        }
        return registry.put(key, value);
    }
}
