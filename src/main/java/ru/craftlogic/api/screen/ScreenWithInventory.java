package ru.craftlogic.api.screen;

import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.FontRenderer;
import net.minecraft.client.gui.GuiButton;
import net.minecraft.client.gui.inventory.GuiContainer;
import net.minecraft.client.renderer.Tessellator;
import net.minecraft.inventory.ClickType;
import net.minecraft.inventory.Container;
import net.minecraft.inventory.Slot;
import net.minecraft.util.ResourceLocation;
import org.lwjgl.input.Keyboard;
import ru.craftlogic.api.block.Updatable;
import ru.craftlogic.api.screen.element.widget.WidgetSlot;

import java.io.IOException;
import java.util.HashSet;
import java.util.Set;

public abstract class ScreenWithInventory<C extends Container> extends GuiContainer implements ElementContainer {
    protected C container;
    private float textureScaleX = 1, textureScaleY = 1;
    private Set<Element> elements = new HashSet<>();

    public ScreenWithInventory(C container) {
        super(container);
        this.container = container;
    }

    public C getContainer() {
        return (C) this.inventorySlots;
    }

    @Override
    public boolean addElement(Element element) {
        return this.elements.add(element);
    }

    @Override
    public boolean removeElement(Element element) {
        return this.elements.remove(element);
    }

    @Override
    public Set<Element> getElements() {
        return new HashSet<>(this.elements);
    }

    @Override
    public FontRenderer getFontRenderer() {
        return this.fontRenderer;
    }

    @Override
    public Minecraft getClient() {
        if (this.mc == null) {
            this.mc = Minecraft.getMinecraft();
        }
        return this.mc;
    }

    @Override
    public void drawWorldBackground(int yScroll) {
        Minecraft client = this.getClient();
        if (client.world != null) {
            this.drawGradientRect(0, 0, this.width, this.height, 0xC0101010, 0xD0101010);
        } else {
            this.drawBackground(yScroll);
        }
    }

    @Override
    public Tessellator getTessellator() {
        return Tessellator.getInstance();
    }

    @Override
    public double getZLevel() {
        return this.zLevel;
    }

    @Override
    public float getTextureScaleX() {
        return this.textureScaleX;
    }

    @Override
    public float getTextureScaleY() {
        return this.textureScaleY;
    }

    @Override
    public void updateScreen() {
        super.updateScreen();
        for (Element element : this.elements) {
            if (element instanceof Updatable) {
                ((Updatable) element).update();
            }
        }
    }

    @Override
    public void bindTexture(ResourceLocation texture, int width, int height) {
        getClient().getTextureManager().bindTexture(texture);
        this.textureScaleX = 1F / width;
        this.textureScaleY = 1F / height;
    }

    @Override
    public int getWindowWidth() {
        return this.width;
    }

    @Override
    public int getWindowHeight() {
        return this.height;
    }

    @Override
    public int getWidth() {
        return this.xSize;
    }

    @Override
    public int getHeight() {
        return this.ySize;
    }

    @Override
    public int getLocalX() {
        return this.guiLeft;
    }

    @Override
    public int getLocalY() {
        return this.guiTop;
    }

    @Override
    public final void initGui() {
        super.initGui();
        this.getClient().setIngameNotInFocus();
        this.elements.clear();
        int x = getLocalX();
        int y = getLocalY();
        for (Slot slot : this.container.inventorySlots) {
            this.elements.add(new WidgetSlot(this, x + slot.xPos - 1, y + slot.yPos - 1, WidgetSlot.SlotSize.NORMAL));
        }
        this.init();
    }

    protected abstract void init();

    protected void close() {
        this.mc.displayGuiScreen(null);
        if (this.mc.currentScreen == null) {
            this.mc.setIngameFocus();
        }
    }

    @Override
    public void drawScreen(int mouseX, int mouseY, float partialTicks) {
        if (this.isDefaultBackgroundEnabled()) {
            this.drawDefaultBackground();
        }
        super.drawScreen(mouseX, mouseY, partialTicks);
        this.renderHoveredToolTip(mouseX, mouseY);
    }

    @Override
    protected final void drawGuiContainerBackgroundLayer(float deltaTime, int mouseX, int mouseY) {
        this.drawBackground(mouseX, mouseY, deltaTime);
        for (Element element : this.elements) {
            element.drawBackground(mouseX, mouseY, deltaTime);
        }
    }

    @Override
    protected final void drawGuiContainerForegroundLayer(int mouseX, int mouseY) {
        float deltaTime = getClient().getRenderPartialTicks();
        this.drawForeground(mouseX, mouseY, deltaTime);
        for (Element element : this.elements) {
            element.drawForeground(mouseX, mouseY, deltaTime);
        }
    }

    @Override
    @Deprecated
    protected final <T extends GuiButton> T addButton(T b) {
        throw new UnsupportedOperationException();
    }

    protected boolean isInvKeyClosingScreen() {
        return true;
    }

    protected boolean isDefaultBackgroundEnabled() {
        return true;
    }

    @Override
    protected final void keyTyped(char symbol, int key) {
        if (key == Keyboard.KEY_ESCAPE || isInvKeyClosingScreen() && this.mc.gameSettings.keyBindInventory.isActiveAndMatches(key)) {
            this.close();
        } else {
            this.checkHotbarKeys(key);
            Slot hoveredSlot = getSlotUnderMouse();
            if (hoveredSlot != null && hoveredSlot.getHasStack()) {
                if (this.mc.gameSettings.keyBindPickBlock.isActiveAndMatches(key)) {
                    this.handleMouseClick(hoveredSlot, hoveredSlot.slotNumber, 0, ClickType.CLONE);
                } else if (this.mc.gameSettings.keyBindDrop.isActiveAndMatches(key)) {
                    this.handleMouseClick(hoveredSlot, hoveredSlot.slotNumber, isCtrlKeyDown() ? 1 : 0, ClickType.THROW);
                }
            }
            this.onKeyPressed(symbol, key);
            for (Element element : this.elements) {
                if (element instanceof InteractiveElement) {
                    ((InteractiveElement) element).onKeyPressed(key, symbol);
                }
            }
        }
    }

    protected void onKeyPressed(char symbol, int key) {}

    @Override
    protected final void mouseClicked(int x, int y, int button) throws IOException {
        super.mouseClicked(x, y, button);
        this.onMouseClick(x, y, button);
        for (Element element : this.elements) {
            if (element instanceof InteractiveElement) {
                ((InteractiveElement) element).onMouseClick(x, y, button);
            }
        }
    }

    protected void onMouseClick(int x, int y, int button) {}

    @Override
    protected final void mouseReleased(int x, int y, int button) {
        super.mouseReleased(x, y, button);
        this.onMouseRelease(x, y, button);
        for (Element element : this.elements) {
            if (element instanceof InteractiveElement) {
                ((InteractiveElement) element).onMouseRelease(x, y, button);
            }
        }
    }

    protected void onMouseRelease(int x, int y, int button) {}

    @Override
    protected final void mouseClickMove(int x, int y, int button, long dragTime) {
        super.mouseClickMove(x, y, button, dragTime);
        this.onMouseDrag(x, y, button, dragTime);
        for (Element element : this.elements) {
            if (element instanceof InteractiveElement) {
                ((InteractiveElement) element).onMouseDrag(x, y, button, dragTime);
            }
        }
    }

    protected void onMouseDrag(int x, int y, int button, long dragTime) {}
}
