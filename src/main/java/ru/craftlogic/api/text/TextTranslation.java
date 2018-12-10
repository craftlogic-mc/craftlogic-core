package ru.craftlogic.api.text;

import net.minecraft.command.CommandException;
import net.minecraft.util.text.ITextComponent;
import net.minecraft.util.text.TextComponentTranslation;

import java.util.ArrayList;
import java.util.List;
import java.util.function.Consumer;

public final class TextTranslation extends Text<TextComponentTranslation, TextTranslation> {

    private final String format;
    private final List<Object> args = new ArrayList<>();

    TextTranslation(String format) {
        this.format = format;
    }

    public TextTranslation arg(Number arg) {
        this.args.add(arg);
        return this;
    }

    public TextTranslation arg(Number arg, Consumer<TextString> decorator) {
        if (decorator != null) {
            TextString text = Text.string(String.valueOf(arg));
            decorator.accept(text);
            this.args.add(text.build());
        } else {
            this.args.add(arg);
        }
        return this;
    }

    public TextTranslation arg(ITextComponent arg) {
        return this.arg(arg, null);
    }

    public TextTranslation arg(ITextComponent arg, Consumer<TextWrapped> decorator) {
        if (decorator != null) {
            decorator.accept(new TextWrapped(arg));
        }
        this.args.add(arg);
        return this;
    }

    public TextTranslation arg(Text<?, ?> arg) {
        this.args.add(arg.build());
        return this;
    }

    public TextTranslation arg(String arg) {
        return this.arg(arg, null);
    }

    public TextTranslation arg(String arg, Consumer<TextString> decorator) {
        TextString text = Text.string(arg);
        if (decorator != null) {
            decorator.accept(text);
        }
        this.args.add(text.build());
        return this;
    }

    public TextTranslation argTranslate(CommandException exc) {
        return this.argTranslate(exc, null);
    }

    public TextTranslation argTranslate(CommandException exc, Consumer<TextTranslation> decorator) {
        TextTranslation text = Text.translation(exc);
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
        TextTranslation text = Text.translation(format);
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