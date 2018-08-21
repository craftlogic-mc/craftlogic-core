package ru.craftlogic.api.screen.element.widget;

import net.minecraft.util.text.ITextComponent;
import net.minecraft.util.text.TextComponentTranslation;
import ru.craftlogic.api.screen.ElementContainer;
import ru.craftlogic.api.screen.element.Widget;

import java.util.function.IntSupplier;

public class WidgetTemperature extends Widget {
    private final IntSupplier temperature, maxTemperature;

    public WidgetTemperature(ElementContainer container, int x, int y, IntSupplier temperature, IntSupplier maxTemperature) {
        super(container, x, y);
        this.maxTemperature = maxTemperature;
        this.temperature = temperature;
    }

    @Override
    public int getWidth() {
        return 11;
    }

    @Override
    public int getHeight() {
        return 50;
    }

    @Override
    protected void drawBackground(int mouseX, int mouseY, float deltaTime) {
        if (this.isVisible()) {
            int x = getX();
            int y = getY();
            final int width = getWidth();
            final int height = getHeight();

            this.bindDefaultTexture();

            this.drawTexturedRect(x, y, width, height, 0, 0);

            int maxTemperature = this.maxTemperature.getAsInt();
            int temperature = this.temperature.getAsInt();

            float progress = (float)temperature / (float)maxTemperature;
            int progressHeight = (int)((float)height * progress);
            if (progressHeight > 0) {
                int progressRemaining = height - progressHeight;
                this.drawTexturedRect(x + width - 2, y + progressRemaining, 2, progressHeight, 12, progressRemaining);
            }
        }
    }

    @Override
    protected ITextComponent getTooltip(int mouseX, int mouseY) {
        int maxTemperature = this.maxTemperature.getAsInt();
        int temperature = this.temperature.getAsInt();
        return new TextComponentTranslation("tooltip.temperature", temperature, maxTemperature);
    }
}
