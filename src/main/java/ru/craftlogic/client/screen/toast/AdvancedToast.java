package ru.craftlogic.client.screen.toast;

import net.minecraft.client.gui.toasts.GuiToast;
import net.minecraft.client.gui.toasts.IToast;
import net.minecraft.util.text.ITextComponent;
import ru.craftlogic.api.text.Text;

public abstract class AdvancedToast implements IToast {
    protected void drawText(GuiToast container, Text<?, ?> text, int x, int y, int color) {
        this.drawText(container, text.build(), x, y, color);
    }

    protected void drawText(GuiToast container, ITextComponent text, int x, int y, int color) {
        container.getMinecraft().fontRenderer.drawString(text.getFormattedText(), x, y, color);
    }
}
