package ru.craftlogic.client.screen.toast;

import com.google.gson.JsonObject;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.Gui;
import net.minecraft.client.gui.toasts.GuiToast;
import net.minecraft.client.network.NetHandlerPlayClient;
import net.minecraft.client.renderer.GlStateManager;
import net.minecraft.util.JsonUtils;
import net.minecraft.util.text.ITextComponent;
import net.minecraft.util.text.TextComponentString;
import net.minecraft.util.text.TextComponentTranslation;
import net.minecraft.util.text.TextFormatting;
import ru.craftlogic.api.CraftAPI;
import ru.craftlogic.client.ProxyClient;
import ru.craftlogic.network.message.MessageConfirmation;

public class ToastConfirmation extends AdvancedToast {
    private String id;
    private final ITextComponent title;
    private final int color;
    private final int timeout;
    private int counter;
    private boolean done;

    public ToastConfirmation(JsonObject data) {
        this(
            JsonUtils.getString(data, "id"),
            ITextComponent.Serializer.fromJsonLenient(JsonUtils.getString(data, "title")),
            JsonUtils.getInt(data, "timeout"),
            JsonUtils.getInt(data, "color", 0xFF555555)
        );
    }

    public ToastConfirmation(String id, ITextComponent title, int timeout, int color) {
        this.id = id;
        this.title = title;
        this.timeout = this.counter = timeout;
        this.color = color;
    }

    @Override
    public String getType() {
        return this.id;
    }

    public void resetTimer() {
        counter = timeout;
    }

    public void confirm(boolean confirmed) {
        Minecraft mc = Minecraft.getMinecraft();
        NetHandlerPlayClient connection = mc.getConnection();
        if (connection != null) {
            counter = 0;
            done = true;
            CraftAPI.NETWORK.sendToServer(new MessageConfirmation(id, confirmed));
        }
    }

    public String getId() {
        return id;
    }

    @Override
    public Visibility draw(GuiToast toasts, long delta) {
        Minecraft client = toasts.getMinecraft();
        client.getTextureManager().bindTexture(TEXTURE_TOASTS);
        GlStateManager.color(1, 1, 1);
        toasts.drawTexturedModalRect(0, 0, 0, 96, 160, 32);

        drawText(toasts, title, 7, 7, 0xFF000000);
        TextComponentString yes = new TextComponentString(ProxyClient.keyBindAccept.getDisplayName());
        yes.getStyle().setColor(TextFormatting.DARK_GREEN);
        TextComponentString no = new TextComponentString(ProxyClient.keyBindDecline.getDisplayName());
        no.getStyle().setColor(TextFormatting.RED);
        drawText(toasts, new TextComponentTranslation("gui.confirmation", yes, no), 7, 18, 0xFF555555);

        Gui.drawRect(3, 28, 157, 29, -1);
        float progress = 1F - (float)(delta / (double)timeout / 1000);

        if (counter > 0) {
            Gui.drawRect(3, 28, (int) (3 + 154 * progress), 29, 0xFF000000 | color);
            counter = timeout - (int)(delta / 1000);
        } else if (!done) {
            confirm(false);
        }

        return counter > 0 ? Visibility.SHOW : Visibility.HIDE;
    }

    protected void drawText(GuiToast toasts, ITextComponent text, int x, int y, int color) {
        toasts.getMinecraft().fontRenderer.drawString(text.getFormattedText(), x, y, color);
    }

}
