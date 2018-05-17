package ru.craftlogic.api.text;

import net.minecraft.util.text.ITextComponent;
import net.minecraft.util.text.TextComponentTranslation;

import java.util.ArrayList;
import java.util.List;
import java.util.function.Consumer;

public final class TextTranslation extends Text<TextComponentTranslation, TextTranslation> {

    private final String format;
    private final List<Object> args = new ArrayList<>();

    public TextTranslation(String format) {
        this.format = format;
    }

    public TextTranslation arg(Number arg) {
        this.args.add(arg);
        return this;
    }

    public TextTranslation arg(ITextComponent arg) {
        this.args.add(arg);
        return this;
    }

    public TextTranslation argText(String arg) {
        return this.argText(arg, null);
    }

    public TextTranslation argText(String arg, Consumer<TextString> decorator) {
        TextString text = new TextString(arg);
        if (decorator != null) {
            decorator.accept(text);
        }
        this.args.add(text.build());
        return this;
    }

    public TextTranslation argTranslate(String format) {
        return this.argTranslate(format, null);
    }

    public TextTranslation argTranslate(String format, Consumer<TextTranslation> decorator) {
        TextTranslation text = new TextTranslation(format);
        if (decorator != null) {
            decorator.accept(text);
        }
        this.args.add(text.build());
        return this;
    }

    @Override
    protected TextComponentTranslation buildSub() {
        return new TextComponentTranslation(this.format, this.args.toArray());
    }
}