package ru.craftlogic.api.screen.element;

import com.google.common.base.Predicate;
import net.minecraft.client.gui.FontRenderer;
import net.minecraft.client.gui.Gui;
import net.minecraft.client.gui.GuiScreen;
import net.minecraft.client.renderer.BufferBuilder;
import net.minecraft.client.renderer.GlStateManager;
import net.minecraft.client.renderer.GlStateManager.LogicOp;
import net.minecraft.client.renderer.Tessellator;
import net.minecraft.client.renderer.vertex.DefaultVertexFormats;
import net.minecraft.util.ChatAllowedCharacters;
import net.minecraft.util.math.MathHelper;
import org.lwjgl.input.Keyboard;
import org.lwjgl.opengl.GL11;
import ru.craftlogic.api.block.Updatable;
import ru.craftlogic.api.screen.ElementContainer;
import ru.craftlogic.api.screen.InteractiveElement;

import java.util.Collections;
import java.util.List;
import java.util.function.Function;

public class ElementTextInput extends InteractiveElement implements Updatable {
    private static int MIN_WIDTH = 70;

    private final int width;
    private final Function<String, List<String>> autocompleter;
    private Predicate<String> filter;
    private String value;
    private int selectionStart, selectionEnd, cursorPosition, maxStringLength, lineScrollOffset, cursorCounter;
    private boolean enableBackground = true, canLoseFocus = true, focused = false;

    public ElementTextInput(ElementContainer container, int x, int y) {
        this(container, x, y, MIN_WIDTH);
    }

    public ElementTextInput(ElementContainer container, int x, int y, int width) {
        this(container, x, y, width, c -> true);
    }

    public ElementTextInput(ElementContainer container, int x, int y, int width, Predicate<String> filter) {
        this(container, x, y, width, filter, s -> Collections.emptyList());
    }

    public ElementTextInput(ElementContainer container, int x, int y, int width, Predicate<String> filter, Function<String, List<String>> autocompleter) {
        super(container, x, y);
        this.width = Math.min(width, MIN_WIDTH);
        this.filter = filter;
        this.autocompleter = autocompleter;
    }

    public String getValue() {
        return value;
    }

    @Override
    public int getWidth() {
        return this.isBackgroundEnabled() ? this.width - 8 : this.width;
    }

    @Override
    public int getHeight() {
        return 20;
    }

    @Override
    protected void drawBackground(int mouseX, int mouseY, float deltaTime) {
        if (this.isVisible()) {
            int x = getX();
            int y = getY();
            int h = getHeight();
            if (this.isBackgroundEnabled()) {
                this.drawColoredRect(x - 1, y - 1, this.width + 1, h + 1, 0xA0A0A0);
                this.drawColoredRect(x, y, this.width, h, 0x000000);
            }

        }
    }

    @Override
    protected void drawForeground(int mouseX, int mouseY, float deltaTime) {
        super.drawForeground(mouseX, mouseY, deltaTime);
        if (this.isVisible()) {
            int x = getX();
            int y = getY();
            int w = getWidth();
            int h = getHeight();

            int textColor = this.isEnabled() ? 0xFFFFFF : 0xEEEEEE;
            int start = this.cursorPosition - this.lineScrollOffset;
            int end = this.selectionEnd - this.lineScrollOffset;
            FontRenderer fontRenderer = getContainer().getFontRenderer();

            String lvt_4_1_ = fontRenderer.trimStringToWidth(this.value.substring(this.lineScrollOffset), this.getWidth());
            boolean lvt_5_1_ = start >= 0 && start <= lvt_4_1_.length();
            boolean lvt_6_1_ = this.isFocused() && this.cursorCounter / 6 % 2 == 0 && lvt_5_1_;
            int lvt_7_1_ = this.enableBackground ? x + 4 : x;
            int lvt_8_1_ = this.enableBackground ? y + (h - 8) / 2 : y;
            int lvt_9_1_ = lvt_7_1_;
            if (end > lvt_4_1_.length()) {
                end = lvt_4_1_.length();
            }

            if (!lvt_4_1_.isEmpty()) {
                String lvt_10_1_ = lvt_5_1_ ? lvt_4_1_.substring(0, start) : lvt_4_1_;
                lvt_9_1_ = fontRenderer.drawStringWithShadow(lvt_10_1_, (float)lvt_7_1_, (float)lvt_8_1_, textColor);
            }

            boolean lvt_10_2_ = this.cursorPosition < this.value.length() || this.value.length() >= this.getMaxStringLength();
            int lvt_11_1_ = lvt_9_1_;
            if (!lvt_5_1_) {
                lvt_11_1_ = start > 0 ? lvt_7_1_ + this.width : lvt_7_1_;
            } else if (lvt_10_2_) {
                lvt_11_1_ = lvt_9_1_ - 1;
                --lvt_9_1_;
            }

            if (!lvt_4_1_.isEmpty() && lvt_5_1_ && start < lvt_4_1_.length()) {
                lvt_9_1_ = fontRenderer.drawStringWithShadow(lvt_4_1_.substring(start), (float)lvt_9_1_, (float)lvt_8_1_, textColor);
            }

            if (lvt_6_1_) {
                if (lvt_10_2_) {
                    Gui.drawRect(lvt_11_1_, lvt_8_1_ - 1, lvt_11_1_ + 1, lvt_8_1_ + 1 + fontRenderer.FONT_HEIGHT, -3092272);
                } else {
                    fontRenderer.drawStringWithShadow("_", (float)lvt_11_1_, (float)lvt_8_1_, textColor);
                }
            }

            if (end != start) {
                int lvt_12_1_ = lvt_7_1_ + fontRenderer.getStringWidth(lvt_4_1_.substring(0, end));
                this.drawSelectionBox(lvt_11_1_, lvt_8_1_ - 1, lvt_12_1_ - 1, lvt_8_1_ + 1 + fontRenderer.FONT_HEIGHT);
            }
        }
    }

    @Override
    public void update() {
        ++this.cursorCounter;
    }

    public void setValue(String value) {
        if (this.filter.apply(value)) {
            if (value.length() > this.maxStringLength) {
                this.value = value.substring(0, this.maxStringLength);
            } else {
                this.value = value;
            }

            this.setCursorPositionEnd();
        }
    }

    public String getSelectedText() {
        int start = this.cursorPosition < this.selectionEnd ? this.cursorPosition : this.selectionEnd;
        int end = this.cursorPosition < this.selectionEnd ? this.selectionEnd : this.cursorPosition;
        return this.value.substring(start, end);
    }

    public void setFilter(Predicate<String> filter) {
        this.filter = filter;
    }

    public void writeText(String input) {
        String text = "";
        String filteredText = ChatAllowedCharacters.filterAllowedCharacters(input);
        int start = this.cursorPosition < this.selectionEnd ? this.cursorPosition : this.selectionEnd;
        int end = this.cursorPosition < this.selectionEnd ? this.selectionEnd : this.cursorPosition;
        int len = this.maxStringLength - this.value.length() - (start - end);
        if (!this.value.isEmpty()) {
            text += this.value.substring(0, start);
        }

        int lvt_7_2_;
        if (len < filteredText.length()) {
            text += filteredText.substring(0, len);
            lvt_7_2_ = len;
        } else {
            text += filteredText;
            lvt_7_2_ = filteredText.length();
        }

        if (!this.value.isEmpty() && end < this.value.length()) {
            text += this.value.substring(end);
        }

        if (this.filter.apply(text)) {
            this.value = text;
            this.moveCursorBy(start - this.selectionEnd + lvt_7_2_);
            //this.setResponderEntryValue(this.id, this.value);//FIXME
        }
    }

    public void deleteWords(int amount) {
        if (!this.value.isEmpty()) {
            if (this.selectionEnd != this.cursorPosition) {
                this.writeText("");
            } else {
                this.deleteFromCursor(this.getNthWordFromCursor(amount) - this.cursorPosition);
            }
        }
    }

    public void deleteFromCursor(int p_deleteFromCursor_1_) {
        if (!this.value.isEmpty()) {
            if (this.selectionEnd != this.cursorPosition) {
                this.writeText("");
            } else {
                boolean lvt_2_1_ = p_deleteFromCursor_1_ < 0;
                int lvt_3_1_ = lvt_2_1_ ? this.cursorPosition + p_deleteFromCursor_1_ : this.cursorPosition;
                int lvt_4_1_ = lvt_2_1_ ? this.cursorPosition : this.cursorPosition + p_deleteFromCursor_1_;
                String text = "";
                if (lvt_3_1_ >= 0) {
                    text = this.value.substring(0, lvt_3_1_);
                }

                if (lvt_4_1_ < this.value.length()) {
                    text = text + this.value.substring(lvt_4_1_);
                }

                if (this.filter.apply(text)) {
                    this.value = text;
                    if (lvt_2_1_) {
                        this.moveCursorBy(p_deleteFromCursor_1_);
                    }

                    //this.setResponderEntryValue(this.id, this.value);//FIXME
                }
            }
        }
    }

    public int getNthWordFromCursor(int start) {
        return this.getNthWordFromPos(start, this.getCursorPosition());
    }

    public int getNthWordFromPos(int p_getNthWordFromPos_1_, int p_getNthWordFromPos_2_) {
        return this.getNthWordFromPosWS(p_getNthWordFromPos_1_, p_getNthWordFromPos_2_, true);
    }

    public int getNthWordFromPosWS(int p_getNthWordFromPosWS_1_, int p_getNthWordFromPosWS_2_, boolean p_getNthWordFromPosWS_3_) {
        int lvt_4_1_ = p_getNthWordFromPosWS_2_;
        boolean lvt_5_1_ = p_getNthWordFromPosWS_1_ < 0;
        int lvt_6_1_ = Math.abs(p_getNthWordFromPosWS_1_);

        for(int lvt_7_1_ = 0; lvt_7_1_ < lvt_6_1_; ++lvt_7_1_) {
            if (!lvt_5_1_) {
                int lvt_8_1_ = this.value.length();
                lvt_4_1_ = this.value.indexOf(" ", lvt_4_1_);
                if (lvt_4_1_ == -1) {
                    lvt_4_1_ = lvt_8_1_;
                } else {
                    while(p_getNthWordFromPosWS_3_ && lvt_4_1_ < lvt_8_1_ && this.value.charAt(lvt_4_1_) == ' ') {
                        ++lvt_4_1_;
                    }
                }
            } else {
                while(p_getNthWordFromPosWS_3_ && lvt_4_1_ > 0 && this.value.charAt(lvt_4_1_ - 1) == ' ') {
                    --lvt_4_1_;
                }

                while(lvt_4_1_ > 0 && this.value.charAt(lvt_4_1_ - 1) != ' ') {
                    --lvt_4_1_;
                }
            }
        }

        return lvt_4_1_;
    }

    public void moveCursorBy(int offset) {
        this.setCursorPosition(this.selectionEnd + offset);
    }

    public void setCursorPosition(int position) {
        this.cursorPosition = position;
        int length = this.value.length();
        this.cursorPosition = MathHelper.clamp(this.cursorPosition, 0, length);
        this.setSelectionPos(this.cursorPosition);
    }

    public void setCursorPositionZero() {
        this.setCursorPosition(0);
    }

    public void setCursorPositionEnd() {
        this.setCursorPosition(this.value.length());
    }

    @Override
    protected void onKeyPressed(int key, char symbol) {
        boolean enabled = this.isEnabled();
        if (this.isFocused()) {
            if (GuiScreen.isKeyComboCtrlA(key)) {
                this.setCursorPositionEnd();
                this.setSelectionPos(0);
            } else if (GuiScreen.isKeyComboCtrlC(key)) {
                GuiScreen.setClipboardString(this.getSelectedText());
            } else if (GuiScreen.isKeyComboCtrlV(key)) {
                if (enabled) {
                    this.writeText(GuiScreen.getClipboardString());
                }
            } else if (GuiScreen.isKeyComboCtrlX(key)) {
                GuiScreen.setClipboardString(this.getSelectedText());
                if (enabled) {
                    this.writeText("");
                }
            } else {
                switch(key) {
                    case Keyboard.KEY_BACK:
                        if (GuiScreen.isCtrlKeyDown()) {
                            if (enabled) {
                                this.deleteWords(-1);
                            }
                        } else if (enabled) {
                            this.deleteFromCursor(-1);
                        }
                        break;
                    case Keyboard.KEY_HOME:
                        if (GuiScreen.isShiftKeyDown()) {
                            this.setSelectionPos(0);
                        } else {
                            this.setCursorPositionZero();
                        }
                        break;
                    case Keyboard.KEY_LEFT:
                        if (GuiScreen.isShiftKeyDown()) {
                            if (GuiScreen.isCtrlKeyDown()) {
                                this.setSelectionPos(this.getNthWordFromPos(-1, this.getSelectionEnd()));
                            } else {
                                this.setSelectionPos(this.getSelectionEnd() - 1);
                            }
                        } else if (GuiScreen.isCtrlKeyDown()) {
                            this.setCursorPosition(this.getNthWordFromCursor(-1));
                        } else {
                            this.moveCursorBy(-1);
                        }
                        break;
                    case Keyboard.KEY_RIGHT:
                        if (GuiScreen.isShiftKeyDown()) {
                            if (GuiScreen.isCtrlKeyDown()) {
                                this.setSelectionPos(this.getNthWordFromPos(1, this.getSelectionEnd()));
                            } else {
                                this.setSelectionPos(this.getSelectionEnd() + 1);
                            }
                        } else if (GuiScreen.isCtrlKeyDown()) {
                            this.setCursorPosition(this.getNthWordFromCursor(1));
                        } else {
                            this.moveCursorBy(1);
                        }
                        break;
                    case Keyboard.KEY_END:
                        if (GuiScreen.isShiftKeyDown()) {
                            this.setSelectionPos(this.value.length());
                        } else {
                            this.setCursorPositionEnd();
                        }

                        break;
                    case Keyboard.KEY_DELETE:
                        if (GuiScreen.isCtrlKeyDown()) {
                            if (enabled) {
                                this.deleteWords(1);
                            }
                        } else if (enabled) {
                            this.deleteFromCursor(1);
                        }

                        break;
                    default:
                        if (ChatAllowedCharacters.isAllowedCharacter(symbol)) {
                            if (enabled) {
                                this.writeText(Character.toString(symbol));
                            }
                        }
                        break;
                }
            }
        }
    }

    @Override
    protected void onMouseClick(int mouseX, int mouseY, int button) {
        int x = getX();
        int y = getY();
        int h = getHeight();
        boolean hover = mouseX >= x && mouseX < x + this.width && mouseY >= y && mouseY < y + h;
        if (this.canLoseFocus) {
            this.setFocused(hover);
        }

        if (this.isFocused() && hover && button == 0) {
            int localX = mouseX - x;
            if (this.enableBackground) {
                localX -= 4;
            }
            FontRenderer fontRenderer = getContainer().getFontRenderer();

            String textWidth = fontRenderer.trimStringToWidth(this.value.substring(this.lineScrollOffset), this.getWidth());
            this.setCursorPosition(fontRenderer.trimStringToWidth(textWidth, localX).length() + this.lineScrollOffset);
        }
    }

    private void drawSelectionBox(int xStart, int yStart, int xEnd, int yEnd) {
        if (xStart < xEnd) {
            int tmp = xStart;
            xStart = xEnd;
            xEnd = tmp;
        }

        if (yStart < yEnd) {
            int tmp = yStart;
            yStart = yEnd;
            yEnd = tmp;
        }
        int x = getX();
        int w = getWidth();

        if (xEnd > x + w) {
            xEnd = x + w;
        }

        if (xStart > x + w) {
            xStart = x + w;
        }

        Tessellator tess = Tessellator.getInstance();
        BufferBuilder buf = tess.getBuffer();
        GlStateManager.color(0F, 0F, 255F, 255F);
        GlStateManager.disableTexture2D();
        GlStateManager.enableColorLogic();
        GlStateManager.colorLogicOp(LogicOp.OR_REVERSE);
        buf.begin(GL11.GL_QUADS, DefaultVertexFormats.POSITION);
        buf.pos(xStart, yEnd, 0).endVertex();
        buf.pos(xEnd, yEnd, 0).endVertex();
        buf.pos(xEnd, yStart, 0).endVertex();
        buf.pos(xStart, yStart, 0).endVertex();
        tess.draw();
        GlStateManager.disableColorLogic();
        GlStateManager.enableTexture2D();
    }

    public void setMaxStringLength(int maxStringLength) {
        this.maxStringLength = maxStringLength;
        if (this.value.length() > maxStringLength) {
            this.value = this.value.substring(0, maxStringLength);
        }
    }

    public int getMaxStringLength() {
        return this.maxStringLength;
    }

    public int getCursorPosition() {
        return this.cursorPosition;
    }

    public boolean isBackgroundEnabled() {
        return this.enableBackground;
    }

    public void setBackgroundEnabled(boolean enabled) {
        this.enableBackground = enabled;
    }

    public void setFocused(boolean focused) {
        if (focused && !this.focused) {
            this.cursorCounter = 0;
        }

        this.focused = focused;

        ((GuiScreen)getContainer()).setFocused(focused);
    }

    public boolean isFocused() {
        return this.focused;
    }

    public int getSelectionEnd() {
        return this.selectionEnd;
    }

    public void setSelectionPos(int pos) {
        int length = this.value.length();
        pos = MathHelper.clamp(pos, 0, length);

        this.selectionEnd = pos;
        if (this.lineScrollOffset > length) {
            this.lineScrollOffset = length;
        }

        FontRenderer fontRenderer = getContainer().getFontRenderer();

        int width = this.getWidth();
        String stringWidth = fontRenderer.trimStringToWidth(this.value.substring(this.lineScrollOffset), width);
        int offset = stringWidth.length() + this.lineScrollOffset;
        if (pos == this.lineScrollOffset) {
            this.lineScrollOffset -= fontRenderer.trimStringToWidth(this.value, width, true).length();
        }

        if (pos > offset) {
            this.lineScrollOffset += pos - offset;
        } else if (pos <= this.lineScrollOffset) {
            this.lineScrollOffset -= this.lineScrollOffset - pos;
        }

        this.lineScrollOffset = MathHelper.clamp(this.lineScrollOffset, 0, length);
    }

    public void setCanLoseFocus(boolean canLoseFocus) {
        this.canLoseFocus = canLoseFocus;
    }
}
