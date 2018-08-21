package ru.craftlogic.client.screen.toast;

import com.google.gson.JsonObject;
import net.minecraft.client.gui.toasts.GuiToast;
import net.minecraft.client.renderer.GlStateManager;
import net.minecraft.util.JsonUtils;
import net.minecraft.util.text.ITextComponent;

public class ToastText extends AdvancedToast {
    private final ITextComponent title;
    private final ITextComponent subtitle;
    private final long timeout;

    public ToastText(JsonObject data) {
        this(
            ITextComponent.Serializer.fromJsonLenient(JsonUtils.getString(data, "title")),
            data.has("subtitle") ? ITextComponent.Serializer.fromJsonLenient(JsonUtils.getString(data, "subtitle")) : null,
            JsonUtils.getInt(data, "timeout")
        );
    }

    public ToastText(ITextComponent title, ITextComponent subtitle, long timeout) {
        this.title = title;
        this.subtitle = subtitle;
        this.timeout = timeout;
    }

    @Override
    public Visibility draw(GuiToast container, long deltaTime) {
        container.getMinecraft().getTextureManager().bindTexture(TEXTURE_TOASTS);
        GlStateManager.color(1, 1, 1);
        container.drawTexturedModalRect(0, 0, 0, 96, 160, 32);
        if (this.subtitle == null) {
            drawText(container, this.title, 7, 12, 0xFF000000);
        } else {
            drawText(container,this.title, 7, 7, 0xFF000000);
            drawText(container,this.subtitle, 7, 18, 0xFF555555);
        }

        return deltaTime > this.timeout ? Visibility.HIDE : Visibility.SHOW;
    }
}
