package ru.craftlogic.mixin.block;

import net.minecraft.block.BlockFire;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import java.util.Random;

@Mixin(BlockFire.class)
public class MixinBlockFire {
    @Inject(method = "tryCatchFire", at = @At("HEAD"), cancellable = true)
    private void tryCatchFire(World world, BlockPos pos, int chance, Random random, int age, EnumFacing face, CallbackInfo ci) {
        if (!world.getGameRules().getBoolean("doFireSpread")) {
            ci.cancel();
        }
    }
}
