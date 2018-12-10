package ru.craftlogic.api.screen.element;

import ru.craftlogic.api.screen.Element;
import ru.craftlogic.api.screen.ElementContainer;

import java.util.Map;

import static ru.craftlogic.api.CraftAPI.parseColor;

public class ElementGradient extends Element {
    private final int width, height;
    private final int startColor, endColor;

    public ElementGradient(ElementContainer container, Map<String, Object> args) {
        this(container,
            parseInt(args.get("x")),
            parseInt(args.get("y")),
            parseInt(args.get("width")),
            parseInt(args.get("height")),
            parseColor(args.containsKey("start_color") ? args.get("start_color") : args.get("color")),
            parseColor(args.containsValue("end_color") ? args.get("end_color") : args.get("color"))
        );
    }

    public ElementGradient(ElementContainer container, int x, int y, int width, int height, int startColor, int endColor) {
        super(container, x, y);
        this.width = width;
        this.height = height;
        this.startColor = startColor;
        this.endColor = endColor;
    }

    @Override
    public int getWidth() {
        return this.width;
    }

    @Override
    public int getHeight() {
        return this.height;
    }

    @Override
    protected void drawBackground(int mouseX, int mouseY, float deltaTime) {
        if (this.isVisible()) {
            drawGradientRect(getX(), getY(), getWidth(), getHeight(), this.startColor, this.endColor);
        }
    }
}
