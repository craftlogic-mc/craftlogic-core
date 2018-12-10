package ru.craftlogic.api.screen.element;

import net.minecraft.client.audio.PositionedSoundRecord;
import net.minecraft.client.renderer.GlStateManager;
import net.minecraft.client.renderer.GlStateManager.DestFactor;
import net.minecraft.client.renderer.GlStateManager.SourceFactor;
import net.minecraft.init.SoundEvents;
import net.minecraft.util.SoundEvent;
import net.minecraft.util.text.ITextComponent;
import ru.craftlogic.api.screen.ElementContainer;
import ru.craftlogic.api.screen.InteractiveElement;

import javax.annotation.Nullable;
import java.util.Map;

public class ElementCheckBox extends InteractiveElement {
    private ITextComponent label;
    private boolean checked;
    private CheckBoxHandler handler;

    public ElementCheckBox(ElementContainer container, Map<String, Object> args) {
        this(container,
            parseInt(args.get("x")),
            parseInt(args.get("y")),
            parseText(args.get("label")),
            parseBoolean(args.getOrDefault("checked", false))
        );
    }

    public ElementCheckBox(ElementContainer container, int x, int y, ITextComponent label, boolean checked) {
        super(container, x, y);
        this.label = label;
        this.checked = checked;
    }

    public ITextComponent getLabel() {
        return label;
    }

    public boolean isChecked() {
        return checked;
    }

    public void setChecked(boolean checked) {
        this.checked = checked;
    }

    public int getLabelWidth() {
        return getContainer().getFontRenderer().getStringWidth(label.getFormattedText());
    }

    @Override
    public int getWidth() {
        return 10 + getLabelWidth();
    }

    @Override
    public int getHeight() {
        return 10;
    }

    @Override
    protected void drawBackground(int mouseX, int mouseY, float deltaTime) {
        if (isVisible()) {
            int x = getX();
            int y = getY();
            int color = isEnabled() ? 0xE0E0E0 : 0xA0A0A0;

            bindDefaultTexture();

            GlStateManager.color(1F, 1F, 1F, 1F);
            GlStateManager.enableBlend();
            GlStateManager.tryBlendFuncSeparate(SourceFactor.SRC_ALPHA, DestFactor.ONE_MINUS_SRC_ALPHA, SourceFactor.ONE, DestFactor.ZERO);
            GlStateManager.blendFunc(SourceFactor.SRC_ALPHA, DestFactor.ONE_MINUS_SRC_ALPHA);

            drawTexturedRect(x, y, 10, 10, 200, 80);

            if (this.checked) {
                drawCenteredText("x", x + 6, y + 1, color);
            }

            drawText(label, x, y, color, true);
        }
    }

    public ElementCheckBox withHandler(CheckBoxHandler handler) {
        this.handler = handler;
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
            }
        }
    }

    @Override
    protected void onMouseRelease(int x, int y, int button) {
        if (isVisible() && isEnabled()) {
            SoundEvent releaseSound = getReleaseSound();
            if (releaseSound != null) {
                getContainer().getClient().getSoundHandler().playSound(PositionedSoundRecord.getMasterRecord(releaseSound, 1F));
            }
            if (x > getX() && y > getY() && x <= getX() + getWidth() && y <= getY() + getHeight()) {
                this.checked = !this.checked;
                if (this.handler != null) {
                    this.handler.onChecked(x, y, button, this.checked);
                }
            }
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

    public interface CheckBoxHandler {
        void onChecked(int mouseX, int mouseY, int mouseButton, boolean checked);
    }
}
