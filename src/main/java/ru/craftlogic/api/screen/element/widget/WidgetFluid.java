package ru.craftlogic.api.screen.element.widget;

import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.GlStateManager;
import net.minecraft.client.renderer.GlStateManager.DestFactor;
import net.minecraft.client.renderer.GlStateManager.SourceFactor;
import net.minecraft.client.renderer.texture.TextureAtlasSprite;
import net.minecraft.client.renderer.texture.TextureMap;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.text.ITextComponent;
import net.minecraft.util.text.TextComponentTranslation;
import net.minecraftforge.fluids.Fluid;
import net.minecraftforge.fluids.FluidStack;
import org.lwjgl.opengl.GL11;
import ru.craftlogic.api.screen.ElementContainer;
import ru.craftlogic.api.screen.element.Widget;

import java.util.function.IntSupplier;
import java.util.function.Supplier;

public class WidgetFluid extends Widget {
    private final Supplier<Fluid> fluid;
    private final IntSupplier stored;
    private final IntSupplier capacity;

    public WidgetFluid(ElementContainer container, int x, int y, Supplier<Fluid> fluid, IntSupplier stored, IntSupplier capacity) {
        super(container, x, y);
        this.fluid = fluid;
        this.stored = stored;
        this.capacity = capacity;
    }

    @Override
    public int getWidth() {
        return 16;
    }

    @Override
    public int getHeight() {
        return 50;
    }

    @Override
    protected void drawBackground(int mouseX, int mouseY, float deltaTime) {
        if (this.isVisible()) {
            int stored = this.stored.getAsInt();
            int capacity = this.capacity.getAsInt();
            int x = getX();
            int y = getY();
            final int width = getWidth();
            final int height = getHeight();
            this.drawGradientRect(x, y, width, height, 0xFF747474, 0xFF393939);
            if (stored > 0) {
                float progress = (float)stored / capacity;
                int progressHeight = (int)((float)height * progress);
                if (progressHeight > 0) {
                    Fluid fluid = this.fluid.get();
                    if (fluid != null) {
                        FluidStack fs = new FluidStack(fluid, stored);
                        ResourceLocation texture = fluid.getStill(fs);
                        if (texture != null) {
                            ElementContainer container = getContainer();
                            Minecraft client = container.getClient();
                            TextureAtlasSprite icon = client.getTextureMapBlocks().getTextureExtry(texture.toString());
                            if (icon != null) {
                                int color = fluid.getColor(fs);

                                int iconX = icon.getOriginX();
                                int iconY = icon.getOriginY();
                                int iconWidth = icon.getIconWidth();
                                int iconHeight = icon.getIconHeight();
                                float u = icon.getMinU();
                                float v = icon.getMinV();
                                float du = (icon.getMaxU() - u);
                                float dv = (icon.getMaxV() - v);

                                int textureWidth = (int) ((float)iconWidth/du);
                                int textureHeight = (int) ((float)iconHeight/dv);

                                container.bindTexture(TextureMap.LOCATION_BLOCKS_TEXTURE, textureWidth, textureHeight);

                                int i = 1;

                                GL11.glPushMatrix();
                                GlStateManager.enableBlend();
                                GlStateManager.blendFunc(SourceFactor.SRC_ALPHA, DestFactor.ONE_MINUS_SRC_ALPHA);

                                while (progressHeight > 0) {
                                    int h = progressHeight >= 16 ? 16 : progressHeight;
                                    int offset = 16 - h;

                                    container.drawTexturedRect(x, y + height - i * 16 + offset, width, h, iconX, iconY + offset, iconWidth, iconHeight - offset, color);

                                    i++;
                                    progressHeight -= h;
                                }

                                GL11.glPopMatrix();
                            }
                        }
                    }
                }
            }
            this.bindDefaultTexture();
            this.drawTexturedRect(x, y, width, height, 110, 0);
        }
    }

    @Override
    protected ITextComponent getTooltip(int mouseX, int mouseY) {
        int stored = this.stored.getAsInt();
        if (stored > 0) {
            Fluid fluid = this.fluid.get();
            int capacity = this.capacity.getAsInt();
            FluidStack stack = new FluidStack(fluid, stored);
            return new TextComponentTranslation("tooltip.fluid", stack.getLocalizedName(), stored, capacity);
        } else {
            return new TextComponentTranslation("tooltip.empty");
        }
    }
}
