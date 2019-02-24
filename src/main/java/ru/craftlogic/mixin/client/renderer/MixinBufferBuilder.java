package ru.craftlogic.mixin.client.renderer;

import net.minecraft.client.renderer.BufferBuilder;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import ru.craftlogic.api.util.AdvancedBufferBuilder;

import java.nio.ByteOrder;
import java.nio.IntBuffer;

@Mixin(BufferBuilder.class)
public abstract class MixinBufferBuilder implements AdvancedBufferBuilder {
    @Shadow
    private IntBuffer rawIntBuffer;

    @Override
    public void putColorRGBA_I(int red, int green, int blue, int alpha) {
        for (int j = 0; j < 4; j++) {
            int i = this.getColorIndex(j + 1);
            if (ByteOrder.nativeOrder() == ByteOrder.LITTLE_ENDIAN) {
                this.rawIntBuffer.put(i, alpha << 24 | blue << 16 | green << 8 | red);
            } else {
                this.rawIntBuffer.put(i, red << 24 | green << 16 | blue << 8 | alpha);
            }
        }
    }

    @Shadow
    public abstract int getColorIndex(int index);
}
