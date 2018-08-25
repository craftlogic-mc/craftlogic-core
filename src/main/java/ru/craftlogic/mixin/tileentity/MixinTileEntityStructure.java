package ru.craftlogic.mixin.tileentity;

import net.minecraft.tileentity.TileEntity;
import net.minecraft.tileentity.TileEntityStructure;
import net.minecraft.util.math.MathHelper;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Redirect;
import ru.craftlogic.CraftConfig;

@Mixin(TileEntityStructure.class)
public abstract class MixinTileEntityStructure extends TileEntity {
    @Redirect(method = "readFromNBT", at = @At(value = "INVOKE", target = "Lnet/minecraft/util/math/MathHelper;clamp(III)I"))
    protected int onClampSize(int value, int min, int max) {
        if (min == -32) min = -CraftConfig.tweaks.maxStructureSize;
        if (max == 32) max = CraftConfig.tweaks.maxStructureSize;
        return MathHelper.clamp(value, min, max);
    }
}
