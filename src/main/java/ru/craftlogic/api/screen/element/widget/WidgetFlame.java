package ru.craftlogic.api.screen.element.widget;

import ru.craftlogic.api.screen.ElementContainer;
import ru.craftlogic.api.screen.element.Widget;
import ru.craftlogic.api.util.FloatSupplier;

public class WidgetFlame extends Widget {
    private final FloatSupplier progress;

    public WidgetFlame(ElementContainer container, int x, int y, FloatSupplier progress) {
        super(container, x, y);
        this.progress = progress;
    }

    @Override
    public int getWidth() {
        return 16;
    }

    @Override
    public int getHeight() {
        return 16;
    }

    @Override
    protected void drawBackground(int mouseX, int mouseY, float deltaTime) {
        if (this.isVisible()) {
            float progress = this.progress.getAsFloat();
            final int width = getWidth();
            int height = getHeight();
            int progressHeight = 0;
            if (progress > 0) {
                if ((progressHeight = (int)((float)height * progress)) > 0) {
                    height -= progressHeight;
                }
            }
            int x = getX();
            int y = getY();
            this.bindDefaultTexture();
            if (height > 0) {
                this.drawTexturedRect(x, y, width, height, 43, 1);
            }
            if (progressHeight > 0) {
                this.drawTexturedRect(x, y + height, width, progressHeight, 43, 35 - progressHeight);
            }
        }
    }
}
