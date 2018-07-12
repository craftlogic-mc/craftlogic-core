package ru.craftlogic.api.screen.elements;

import net.minecraft.client.audio.PositionedSoundRecord;
import net.minecraft.client.renderer.GlStateManager;
import net.minecraft.client.renderer.GlStateManager.DestFactor;
import net.minecraft.client.renderer.GlStateManager.SourceFactor;
import net.minecraft.init.SoundEvents;
import net.minecraft.util.SoundEvent;
import ru.craftlogic.api.screen.ElementContainer;
import ru.craftlogic.api.screen.InteractiveElement;

import javax.annotation.Nullable;

public abstract class ElementButton extends InteractiveElement {
    private static final int MAXIMAL_WIDTH = 200;

    private String title;
    private int width;
    private ButtonSize size;
    private ButtonState state = ButtonState.NORMAL;
    private int color = 0xFF_FFFFFF;

    public ElementButton(ElementContainer container, int x, int y, String title) {
        this(container, x, y, title, MAXIMAL_WIDTH);
    }

    public ElementButton(ElementContainer container, int x, int y, String title, int width) {
        this(container, x, y, title, width, ButtonSize.NORMAL);
    }

    public ElementButton(ElementContainer container, int x, int y, String title, int width, ButtonSize size) {
        super(container, x, y);
        this.title = title;
        this.width = Math.min(Math.max(width, size.height), MAXIMAL_WIDTH);
        this.size = size;
    }

    public int getWidth() {
        return width;
    }

    public int getHeight() {
        return size.height;
    }

    public int getColor() {
        return color;
    }

    public ElementButton withColor(int color) {
        this.color = color;
        return this;
    }

    @Override
    protected void onMouseClick(int x, int y, int button) {
        if (isVisible() && isEnabled()) {
            if (x > getX() && y > getY() && x <= getX() + getWidth() && y <= getY() + getHeight()) {
                SoundEvent pressSound = getPressSound();
                if (pressSound != null) {
                    getContainer().getClient().getSoundHandler().playSound(PositionedSoundRecord.getMasterRecord(pressSound, 1F));
                }
                state = ButtonState.PRESSED;
            }
        }
    }

    @Override
    protected void onMouseRelease(int x, int y, int button) {
        if (isVisible() && isEnabled() && state == ButtonState.PRESSED) {
            state = ButtonState.NORMAL;
            SoundEvent releaseSound = getReleaseSound();
            if (releaseSound != null) {
                getContainer().getClient().getSoundHandler().playSound(PositionedSoundRecord.getMasterRecord(releaseSound, 1F));
            }
            if (x > getX() && y > getY() && x <= getX() + getWidth() && y <= getY() + getHeight()) {
                this.onTrigger(x, y, button);
            }
        }
    }

    @Override
    protected void draw(int mouseX, int mouseY, float deltaTime) {
        if (this.isVisible()) {
            ButtonState state = this.state;
            int x = getX();
            int y = getY();
            int w = getWidth();
            int h = size.height;
            if (mouseX > x && mouseY > y && mouseX <= x + w && mouseY <= y + h) {
                if (state != ButtonState.PRESSED) {
                    state = ButtonState.HOVER;
                }
            }
            bindDefaultTexture();
            int ty = size.startY;
            if (this.isEnabled()) {
                ty += state.index * h;
            }
            GlStateManager.color(1F, 1F, 1F, 1F);
            GlStateManager.enableBlend();
            GlStateManager.tryBlendFuncSeparate(SourceFactor.SRC_ALPHA, DestFactor.ONE_MINUS_SRC_ALPHA, SourceFactor.ONE, DestFactor.ZERO);
            GlStateManager.blendFunc(SourceFactor.SRC_ALPHA, DestFactor.ONE_MINUS_SRC_ALPHA);

            drawTexturedRect(x, y, w / 2, h, 0, ty, this.color);
            drawTexturedRect(x + w / 2, y, w / 2, h, 200 - w / 2, ty, this.color);

            int textColor = 0xe0e0e0;
            if (!this.isEnabled()) {
                textColor = 0xa0a0a0;
            } else if (state == ButtonState.HOVER) {
                textColor = 0xffffa0;
            }

            drawCenteredText(title, x + getWidth() / 2, y + getHeight() / 2, textColor, true);
        }
    }

    @Nullable
    protected SoundEvent getPressSound() {
        return SoundEvents.UI_BUTTON_CLICK;
    }

    @Nullable
    protected SoundEvent getReleaseSound() {
        return null;
    }

    protected abstract void onTrigger(int mouseX, int mouseY, int button);

    public enum ButtonState {
        NORMAL(1),
        HOVER(2),
        PRESSED(3);

        private final int index;

        ButtonState(int index) {
            this.index = index;
        }
    }

    public enum ButtonSize {
        SMALL(80, 10),
        NORMAL(0, 20);

        private final int startY, height;

        ButtonSize(int startY, int height) {
            this.startY = startY;
            this.height = height;
        }
    }
}
