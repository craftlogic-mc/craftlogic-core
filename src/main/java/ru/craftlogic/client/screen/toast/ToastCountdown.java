package ru.craftlogic.client.screen.toast;

import com.google.gson.JsonObject;
import net.minecraft.client.Minecraft;
import net.minecraft.client.audio.PositionedSoundRecord;
import net.minecraft.client.audio.SoundHandler;
import net.minecraft.client.gui.Gui;
import net.minecraft.client.gui.toasts.GuiToast;
import net.minecraft.client.renderer.GlStateManager;
import net.minecraft.util.JsonUtils;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.SoundEvent;
import net.minecraft.util.text.ITextComponent;
import ru.craftlogic.api.CraftMessages;
import ru.craftlogic.api.CraftSounds;

public class ToastCountdown extends AdvancedToast {
    private String id;
    private final ITextComponent title;
    private final int color;
    private final SoundEvent tickSound;
    private int timeout;
    private int counter;

    public ToastCountdown(JsonObject data) {
        this(
            JsonUtils.getString(data, "id"),
            ITextComponent.Serializer.fromJsonLenient(JsonUtils.getString(data, "title")),
            JsonUtils.getInt(data, "timeout"),
            JsonUtils.getInt(data, "color", 0xFF555555),
            data.has("tickSound") ? SoundEvent.REGISTRY.getObject(new ResourceLocation(data.get("tickSound").getAsString())) : CraftSounds.COUNTDOWN_TICK
        );
    }

    public ToastCountdown(String id, ITextComponent title, int timeout, int color, SoundEvent tickSound) {
        this.id = id;
        this.title = title;
        this.timeout = this.counter = timeout;
        this.color = color;
        this.tickSound = tickSound;
    }

    @Override
    public String getType() {
        return this.id;
    }

    @Override
    public Visibility draw(GuiToast container, long deltaTime) {
        Minecraft client = container.getMinecraft();
        client.getTextureManager().bindTexture(TEXTURE_TOASTS);
        GlStateManager.color(1, 1, 1);
        container.drawTexturedModalRect(0, 0, 0, 96, 160, 32);

        drawText(container, this.title, 7, 7, 0xFF000000);
        drawText(container, CraftMessages.parseDuration(this.counter * 1000), 7, 18, 0xFF555555);

        Gui.drawRect(3, 28, 157, 29, -1);
        float progress = 1F - (float)((double)deltaTime / (double)this.timeout / 1000);

        if (this.counter > 0) {
            Gui.drawRect(3, 28, (int) (3 + 154 * progress), 29, this.color);
        }

        int c = this.timeout - (int)(deltaTime / 1000);
        if (c != this.counter) {
            this.counter = c;
            SoundHandler soundHandler = client.getSoundHandler();
            soundHandler.playSound(PositionedSoundRecord.getRecord(this.tickSound, 1F, 1F));
        }

        return this.counter > 0 ? Visibility.SHOW : Visibility.HIDE;
    }

    public void setCountdown(int countdown) {
        if (countdown > this.timeout) {
            this.timeout = countdown;
        }
        this.counter = countdown;
    }
}
