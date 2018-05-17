package ru.craftlogic.api.screen;

import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.FontRenderer;
import net.minecraft.client.gui.GuiButton;
import net.minecraft.client.gui.GuiScreen;
import net.minecraft.client.renderer.Tessellator;
import net.minecraft.util.ResourceLocation;
import org.lwjgl.input.Keyboard;

import java.util.HashSet;
import java.util.Set;

public abstract class Screen extends GuiScreen implements ElementContainer {
    private float textureScaleX = 1, textureScaleY = 1;
    private Set<Element> elements = new HashSet<>();

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
    public void drawWorldBackground(int index) {
        Minecraft client = this.getClient();
        if (client.world != null) {
            this.drawGradientRect(0, 0, this.width, this.height, -1072689136, -804253680);
        } else {
            this.drawBackground(index);
        }
    }

    @Override
    public Tessellator getTessellator() {
        return Tessellator.getInstance();
    }

    @Override
    public int getWidth() {
        return this.width;
    }

    @Override
    public int getHeight() {
        return this.height;
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
    public void bindTexture(ResourceLocation texture, int width, int height) {
        getClient().getTextureManager().bindTexture(texture);
        this.textureScaleX = 1F / width;
        this.textureScaleY = 1F / height;
    }

    @Override
    public final void drawScreen(int mouseX, int mouseY, float deltaTime) {
        this.drawBackground(mouseX, mouseY, deltaTime);
        for (Element element : this.elements) {
            element.draw(mouseX, mouseY, deltaTime);
        }
        this.drawForeground(mouseX, mouseY, deltaTime);
    }

    @Override
    public final void initGui() {
        super.initGui();
        this.getClient().setIngameNotInFocus();
        this.init();
    }

    protected abstract void init();

    protected void close() {
        this.mc.displayGuiScreen(null);
        if (this.mc.currentScreen == null) {
            this.mc.setIngameFocus();
        }
    }

    @Deprecated
    @Override
    protected final <T extends GuiButton> T addButton(T b) {
        throw new UnsupportedOperationException();
    }

    @Override
    protected final void keyTyped(char symbol, int key) {
        if (key == Keyboard.KEY_ESCAPE) {
            this.close();
        } else {
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
    protected final void mouseClicked(int x, int y, int button) {
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
        this.onMouseDrag(x, y, button, dragTime);
        for (Element element : this.elements) {
            if (element instanceof InteractiveElement) {
                ((InteractiveElement) element).onMouseDrag(x, y, button, dragTime);
            }
        }
    }

    protected void onMouseDrag(int x, int y, int button, long dragTime) {}
}
