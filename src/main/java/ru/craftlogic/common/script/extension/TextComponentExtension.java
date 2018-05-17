package ru.craftlogic.common.script.extension;

import net.minecraft.util.text.ITextComponent;
import net.minecraft.util.text.TextComponentString;
import net.minecraft.util.text.TextFormatting;
import net.minecraft.util.text.event.ClickEvent;
import net.minecraft.util.text.event.HoverEvent;

public class TextComponentExtension {
    public static ITextComponent add(ITextComponent self, ITextComponent other) {
        self.appendSibling(other);
        return self;
    }

    public static ITextComponent add(ITextComponent self, String text) {
        ITextComponent other = new TextComponentString(text);
        self.appendSibling(other);
        return self;
    }

    public static ITextComponent add(String self, ITextComponent other) {
        ITextComponent main = new TextComponentString(self);
        main.appendSibling(other);
        return main;
    }

    public static ITextComponent plus(ITextComponent self, ITextComponent other) {
        return add(self, other);
    }

    public static ITextComponent plus(ITextComponent self, String text) {
        return add(self, text);
    }

    public static ITextComponent plus(String self, ITextComponent other) {
        return add(self, other);
    }

    public static ITextComponent plus(ITextComponent self, Object text) {
        return add(self, text.toString());
    }

    //style

    public static ITextComponent color(ITextComponent self, TextFormatting color) {
        self.getStyle().setColor(color);
        return self;
    }

    public static ITextComponent color(String self, TextFormatting color) {
        return color(new TextComponentString(self), color);
    }

    public static ITextComponent bold(ITextComponent self) {
        self.getStyle().setBold(true);
        return self;
    }

    public static ITextComponent bold(String self) {
        return bold(new TextComponentString(self));
    }

    public static ITextComponent italic(ITextComponent self) {
        self.getStyle().setItalic(true);
        return self;
    }

    public static ITextComponent italic(String self) {
        return italic(new TextComponentString(self));
    }

    public static ITextComponent underlined(ITextComponent self) {
        self.getStyle().setUnderlined(true);
        return self;
    }

    public static ITextComponent underlined(String self) {
        return underlined(new TextComponentString(self));
    }

    public static ITextComponent strikethrough(ITextComponent self) {
        self.getStyle().setStrikethrough(true);
        return self;
    }

    public static ITextComponent strikethrough(String self) {
        return strikethrough(new TextComponentString(self));
    }

    public static ITextComponent obfuscated(ITextComponent self) {
        self.getStyle().setObfuscated(true);
        return self;
    }

    public static ITextComponent obfuscated(String self) {
        return obfuscated(new TextComponentString(self));
    }

    //Click event

    public static ITextComponent openURL(ITextComponent self, String arg) {
        self.getStyle().setClickEvent(new ClickEvent(ClickEvent.Action.OPEN_URL, arg));
        return self;
    }

    public static ITextComponent openURL(String self, String arg) {
        return openURL(new TextComponentString(self), arg);
    }

    public static ITextComponent exec(ITextComponent self, String arg) {
        self.getStyle().setClickEvent(new ClickEvent(ClickEvent.Action.RUN_COMMAND, arg));
        return self;
    }

    public static ITextComponent exec(String self, String arg) {
        return exec(new TextComponentString(self), arg);
    }

    public static ITextComponent suggest(ITextComponent self, String arg) {
        self.getStyle().setClickEvent(new ClickEvent(ClickEvent.Action.SUGGEST_COMMAND, arg));
        return self;
    }

    public static ITextComponent suggest(String self, String arg) {
        return suggest(new TextComponentString(self), arg);
    }

    //Hover events

    public static ITextComponent tooltip(ITextComponent self, TextComponentString arg) {
        self.getStyle().setHoverEvent(new HoverEvent(HoverEvent.Action.SHOW_TEXT, arg));
        return self;
    }

    public static ITextComponent tooltip(String self, TextComponentString arg) {
        return tooltip(new TextComponentString(self), arg);
    }

    public static ITextComponent tooltip(ITextComponent self, String arg) {
        self.getStyle().setHoverEvent(new HoverEvent(HoverEvent.Action.SHOW_TEXT, new TextComponentString(arg)));
        return self;
    }

    public static ITextComponent tooltip(String self, String arg) {
        return tooltip(new TextComponentString(self), arg);
    }

    //direct colors

    public static ITextComponent black(ITextComponent self) {
        self.getStyle().setColor(TextFormatting.BLACK);
        return self;
    }

    public static ITextComponent black(String self) {
        return black(new TextComponentString(self));
    }

    public static ITextComponent darkBlue(ITextComponent self) {
        self.getStyle().setColor(TextFormatting.DARK_BLUE);
        return self;
    }

    public static ITextComponent darkBlue(String self) {
        return darkBlue(new TextComponentString(self));
    }

    public static ITextComponent darkGreen(ITextComponent self) {
        self.getStyle().setColor(TextFormatting.DARK_GREEN);
        return self;
    }

    public static ITextComponent darkGreen(String self) {
        return darkGreen(new TextComponentString(self));
    }

    public static ITextComponent darkAqua(ITextComponent self) {
        self.getStyle().setColor(TextFormatting.DARK_AQUA);
        return self;
    }

    public static ITextComponent darkAqua(String self) {
        return darkAqua(new TextComponentString(self));
    }

    public static ITextComponent darkRed(ITextComponent self) {
        self.getStyle().setColor(TextFormatting.DARK_RED);
        return self;
    }

    public static ITextComponent darkRed(String self) {
        return darkRed(new TextComponentString(self));
    }

    public static ITextComponent darkPurple(ITextComponent self) {
        self.getStyle().setColor(TextFormatting.DARK_PURPLE);
        return self;
    }

    public static ITextComponent darkPurple(String self) {
        return darkPurple(new TextComponentString(self));
    }

    public static ITextComponent gold(ITextComponent self) {
        self.getStyle().setColor(TextFormatting.GOLD);
        return self;
    }

    public static ITextComponent gold(String self) {
        return gold(new TextComponentString(self));
    }

    public static ITextComponent gray(ITextComponent self) {
        self.getStyle().setColor(TextFormatting.GRAY);
        return self;
    }

    public static ITextComponent gray(String self) {
        return gray(new TextComponentString(self));
    }

    public static ITextComponent darkGray(ITextComponent self) {
        self.getStyle().setColor(TextFormatting.DARK_GRAY);
        return self;
    }

    public static ITextComponent darkGray(String self) {
        return darkGray(new TextComponentString(self));
    }

    public static ITextComponent blue(ITextComponent self) {
        self.getStyle().setColor(TextFormatting.BLUE);
        return self;
    }

    public static ITextComponent blue(String self) {
        return blue(new TextComponentString(self));
    }

    public static ITextComponent green(ITextComponent self) {
        self.getStyle().setColor(TextFormatting.GREEN);
        return self;
    }

    public static ITextComponent green(String self) {
        return green(new TextComponentString(self));
    }

    public static ITextComponent aqua(ITextComponent self) {
        self.getStyle().setColor(TextFormatting.AQUA);
        return self;
    }

    public static ITextComponent aqua(String self) {
        return aqua(new TextComponentString(self));
    }

    public static ITextComponent red(ITextComponent self) {
        self.getStyle().setColor(TextFormatting.RED);
        return self;
    }

    public static ITextComponent red(String self) {
        return red(new TextComponentString(self));
    }

    public static ITextComponent lightPurple(ITextComponent self) {
        self.getStyle().setColor(TextFormatting.LIGHT_PURPLE);
        return self;
    }

    public static ITextComponent lightPurple(String self) {
        return lightPurple(new TextComponentString(self));
    }

    public static ITextComponent yellow(ITextComponent self) {
        self.getStyle().setColor(TextFormatting.YELLOW);
        return self;
    }

    public static ITextComponent yellow(String self) {
        return yellow(new TextComponentString(self));
    }

    public static ITextComponent white(ITextComponent self) {
        self.getStyle().setColor(TextFormatting.WHITE);
        return self;
    }

    public static ITextComponent white(String self) {
        return white(new TextComponentString(self));
    }
}
