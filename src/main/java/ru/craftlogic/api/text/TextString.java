package ru.craftlogic.api.text;

import net.minecraft.util.text.TextComponentString;

public final class TextString extends Text<TextComponentString, TextString> {
    private final String text;

    TextString(String text) {
        this.text = text;
    }

    @Override
    protected TextComponentString buildSub() {
        return new TextComponentString(this.text);
    }
}
