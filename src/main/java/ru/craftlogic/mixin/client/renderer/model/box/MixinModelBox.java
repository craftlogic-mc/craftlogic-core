package ru.craftlogic.mixin.client.renderer.model.box;

import net.minecraft.client.model.ModelBox;
import net.minecraft.client.renderer.BufferBuilder;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import ru.craftlogic.api.model.AdvancedModelBox;

@Mixin(ModelBox.class)
public class MixinModelBox implements AdvancedModelBox {
    private boolean visible = true;

    @Override
    public boolean isVisible() {
        return this.visible;
    }

    @Override
    public void setVisible(boolean visible) {
        this.visible = visible;
    }

    @Inject(method = "render", at = @At("HEAD"), cancellable = true)
    protected void onRender(BufferBuilder buff, float partialTicks, CallbackInfo info) {
        if (!visible) info.cancel();
    }
}
