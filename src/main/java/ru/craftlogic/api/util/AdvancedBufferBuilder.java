package ru.craftlogic.api.util;

import net.minecraft.util.math.MathHelper;

public interface AdvancedBufferBuilder {
    default void putColorRGBA_F(float red, float green, float blue, float alpha) {
        int r = MathHelper.clamp((int)(red * 255F), 0, 255);
        int g = MathHelper.clamp((int)(green * 255F), 0, 255);
        int b = MathHelper.clamp((int)(blue * 255F), 0, 255);
        int a = MathHelper.clamp((int)(alpha * 255F), 0, 255);
        this.putColorRGBA_I(r, g, b, a);
    }

    void putColorRGBA_I(int red, int green, int blue, int alpha);
}
