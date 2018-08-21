package ru.craftlogic.api.screen.element;

import net.minecraft.util.ResourceLocation;
import ru.craftlogic.api.screen.Element;
import ru.craftlogic.api.screen.ElementContainer;

public abstract class Widget extends Element {
    protected static final ResourceLocation WIDGETS_TEXTURE = new ResourceLocation("craftlogic", "textures/gui/widgets.png");

    public Widget(ElementContainer container, int x, int y) {
        super(container, x, y);
    }

    @Override
    protected void bindDefaultTexture() {
        getContainer().bindTexture(WIDGETS_TEXTURE);
    }
}
