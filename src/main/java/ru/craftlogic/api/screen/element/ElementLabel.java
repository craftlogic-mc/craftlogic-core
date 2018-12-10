package ru.craftlogic.api.screen.element;

import net.minecraft.util.text.ITextComponent;
import ru.craftlogic.api.screen.Element;
import ru.craftlogic.api.screen.ElementContainer;

import java.util.Map;
import java.util.function.Supplier;

import static ru.craftlogic.api.CraftAPI.parseColor;

public class ElementLabel extends Element {
    private Supplier<ITextComponent> text;
    private final LabelAlign align;
    private final int color;
    private final boolean shadow;

    public ElementLabel(ElementContainer container, Map<String, Object> args) {
        this(container,
            parseInt(args.get("x")),
            parseInt(args.get("y")),
            parseText(args.get("text")),
            args.containsKey("align") ? LabelAlign.valueOf(((String)args.get("align")).toUpperCase()) : LabelAlign.FIT,
            parseColor(args.getOrDefault("color", "0xFFFFFF")),
            (boolean)args.getOrDefault("shadow", false)
        );
    }

    public ElementLabel(ElementContainer container, int x, int y, ITextComponent text, LabelAlign align, int color, boolean shadow) {
        this(container, x, y, () -> text, align, color, shadow);
    }

    public ElementLabel(ElementContainer container, int x, int y, Supplier<ITextComponent> text, LabelAlign align, int color, boolean shadow) {
        super(container, x, y);
        this.text = text;
        this.align = align;
        this.color = color;
        this.shadow = shadow;
    }

    @Override
    public int getWidth() {
        String text = this.text.get().getFormattedText();
        return getContainer().getFontRenderer().getStringWidth(text);
    }

    @Override
    public int getHeight() {
        return getContainer().getFontRenderer().FONT_HEIGHT;
    }

    @Override
    protected void drawBackground(int mouseX, int mouseY, float deltaTime) {}

    @Override
    protected void drawForeground(int mouseX, int mouseY, float deltaTime) {
        super.drawForeground(mouseX, mouseY, deltaTime);
        int x = getX();
        int y = getY();
        if (this.align == LabelAlign.CENTER) {
            this.drawCenteredText(this.text.get(), x, y, color, shadow);
        } else {
            this.drawText(this.text.get(), x, y, color, shadow);
        }
    }

    public enum LabelAlign {
        FIT,
        CENTER
    }
}
