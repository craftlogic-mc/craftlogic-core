package ru.craftlogic.mixin.block;

import net.minecraft.block.BlockFire;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Redirect;

import java.util.Random;

@Mixin(BlockFire.class)
public abstract class MixinBlockFire {
    @Shadow protected abstract void tryCatchFire(World world, BlockPos pos, int chance, Random random, int age, EnumFacing face);

    @Redirect(method = "updateTick", at = @At(value = "INVOKE", target = "Lnet/minecraft/block/BlockFire;tryCatchFire(Lnet/minecraft/world/World;Lnet/minecraft/util/math/BlockPos;ILjava/util/Random;ILnet/minecraft/util/EnumFacing;)V"))
    private void onCatchFire(BlockFire bf, World world, BlockPos pos, int chance, Random random, int age, EnumFacing face) {
        if (world.getGameRules().getBoolean("doFireSpread")) {
            tryCatchFire(world, pos, chance, random, age, face);
        }
    }
}
