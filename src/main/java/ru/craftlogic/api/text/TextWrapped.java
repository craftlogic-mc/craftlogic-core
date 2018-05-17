package ru.craftlogic.api.text;

import net.minecraft.util.text.ITextComponent;

public class TextWrapped extends Text<ITextComponent, TextWrapped> {
    private final ITextComponent text;

    public TextWrapped(ITextComponent text) {
        this.text = text;
    }

    @Override
    protected ITextComponent buildSub() {
        return this.text;
    }
}
