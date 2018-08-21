package ru.craftlogic.api.screen.element.widget;

import ru.craftlogic.api.screen.ElementContainer;
import ru.craftlogic.api.screen.element.Widget;

public class WidgetSlot extends Widget {
    private final SlotSize size;

    public WidgetSlot(ElementContainer container, int x, int y, SlotSize size) {
        super(container, x, y);
        this.size = size;
    }

    @Override
    public int getWidth() {
        return size == SlotSize.NORMAL ? 18 : 26;
    }

    @Override
    public int getHeight() {
        return size == SlotSize.NORMAL ? 18 : 26;
    }

    @Override
    protected void drawBackground(int mouseX, int mouseY, float deltaTime) {
        if (this.isVisible()) {
            int x = getX();
            int y = getY();
            int width = getWidth();
            int height = getHeight();
            boolean big = size != SlotSize.NORMAL;
            this.bindDefaultTexture();
            this.drawTexturedRect(x, y, width, height, big ? 82 : 62, 1);
        }
    }

    public enum SlotSize {
        NORMAL,
        LARGE
    }
}
