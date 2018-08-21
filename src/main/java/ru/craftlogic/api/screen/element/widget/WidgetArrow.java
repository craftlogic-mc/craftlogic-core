package ru.craftlogic.api.screen.element.widget;

import ru.craftlogic.api.screen.ElementContainer;
import ru.craftlogic.api.screen.element.Widget;
import ru.craftlogic.api.util.FloatSupplier;

public class WidgetArrow extends Widget {
    private final ArrowDirection direction;
    private final FloatSupplier progress;

    public WidgetArrow(ElementContainer container, int x, int y, ArrowDirection direction, FloatSupplier progress) {
        super(container, x, y);
        this.direction = direction;
        this.progress = progress;
    }

    @Override
    public int getWidth() {
        return 23;
    }

    @Override
    public int getHeight() {
        return 16;
    }

    @Override
    protected void drawBackground(int mouseX, int mouseY, float deltaTime) {
        if (this.isVisible()) {
            float progress = this.progress.getAsFloat();
            int width = getWidth();
            final int height = getHeight();
            int progressWidth = 0;
            if (progress > 0) {
                if ((progressWidth = (int)((float)width * progress)) > 0) {
                    width -= progressWidth;
                }
            }
            int x = getX();
            int y = getY();
            int offset = this.direction != ArrowDirection.RIGHT ? 34 : 0;
            if (width > 0) {
                this.drawTexturedRect(x + progressWidth, y, width, height, 17 + progressWidth, 1 + offset);
            }
            if (progressWidth > 0) {
                this.drawTexturedRect(x, y, progressWidth, height, 43, 18 + offset);
            }
        }
    }

    public enum ArrowDirection {
        LEFT,
        RIGHT
    }
}
