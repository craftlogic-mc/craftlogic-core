package ru.craftlogic.client.screen;

import net.minecraft.util.text.ITextComponent;
import net.minecraft.util.text.TextComponentTranslation;
import ru.craftlogic.api.block.Updatable;
import ru.craftlogic.api.screen.Screen;
import ru.craftlogic.api.screen.element.ElementButton;
import ru.craftlogic.api.screen.element.ElementLabel;
import ru.craftlogic.api.util.BooleanConsumer;

public class ScreenQuestion extends Screen implements Updatable {
    private final ITextComponent question;
    private int countdown;
    private boolean answered;
    private BooleanConsumer handler;

    public ScreenQuestion(ITextComponent question, int timeout, BooleanConsumer handler) {
        this.question = question;
        this.countdown = timeout * 20;
        this.handler = handler;
    }

    @Override
    public void update() {
        if (this.countdown-- == 0) {
            this.close();
        }
    }

    @Override
    protected void init() {
        int x = getWindowWidth() / 2;
        int y = getWindowHeight() / 2;
        addElement(new ElementLabel(this,
            x, y - 25 + getFontRenderer().FONT_HEIGHT / 2,
            this.question,
            ElementLabel.LabelAlign.CENTER,
            0xE0E0E0,
            true
        ));
        addElement(new ElementButton(this, x - 50 - 105, y + 16, new TextComponentTranslation("gui.yes"), 100)
            .withHandler((mouseX, mouseY, mouseButton) -> {
                this.handler.accept(true);
                this.answered = true;
                this.mc.displayGuiScreen(null);
            }
        ));
        addElement(new ElementButton(this, x - 50 + 105, y + 16, new TextComponentTranslation("gui.no"), 100)
            .withHandler((mouseX, mouseY, mouseButton) -> {
                this.handler.accept(false);
                this.answered = true;
                this.mc.displayGuiScreen(null);
            })
        );
    }

    @Override
    public void onGuiClosed() {
        if (!this.answered) {
            this.handler.accept(false);
        }
    }

    @Override
    public void drawBackground(int mouseX, int mouseY, float deltaTime) {
        this.drawDefaultBackground();
    }

    @Override
    public void drawForeground(int mouseX, int mouseY, float deltaTime) {}
}
