package ru.craftlogic.mixin.world;

import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.world.WorldEntitySpawner;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Redirect;

@Mixin(WorldEntitySpawner.class)
public class MixinEntitySpawner {

    @Redirect(method = "findChunksForSpawning", at = @At(value = "INVOKE", target = "Lnet/minecraft/entity/player/EntityPlayer;isSpectator()Z"))
    public boolean shouldNotSpawnInChunk(EntityPlayer player) {
        return player.isSpectator() || player.getClass().getName().endsWith("ReallyExistingPlayer");
    }
}
