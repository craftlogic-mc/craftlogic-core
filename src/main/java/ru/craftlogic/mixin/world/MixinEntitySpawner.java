package ru.craftlogic.mixin.world;

import net.minecraft.entity.EnumCreatureType;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.world.WorldEntitySpawner;
import net.minecraft.world.WorldServer;
import net.minecraftforge.common.MinecraftForge;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.Redirect;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;
import ru.craftlogic.api.event.world.WorldEntitySpawnCountEvent;

@Mixin(WorldEntitySpawner.class)
public class MixinEntitySpawner {

    private WorldServer world;

    @Redirect(method = "findChunksForSpawning", at = @At(value = "INVOKE", target = "Lnet/minecraft/entity/player/EntityPlayer;isSpectator()Z"))
    public boolean shouldNotSpawnInChunk(EntityPlayer player) {
        return player.isSpectator() || player.getClass().getName().endsWith("ReallyExistingPlayer");
    }

    @Redirect(method = "findChunksForSpawning", at = @At(value = "INVOKE", target = "Lnet/minecraft/entity/EnumCreatureType;getMaxNumberOfCreature()I"))
    public int maxMobCount(EnumCreatureType type) {
        WorldEntitySpawnCountEvent event = new WorldEntitySpawnCountEvent(type, world);
        MinecraftForge.EVENT_BUS.post(event);
        return event.maxCount;
    }

    @Inject(method = "findChunksForSpawning", at = @At("HEAD"))
    public void onSearchChunksStart(WorldServer ws, boolean b, boolean p_findChunksForSpawning_3_, boolean p_findChunksForSpawning_4_, CallbackInfoReturnable<Integer> cir) {
        world = ws;
    }

    @Inject(method = "findChunksForSpawning", at = @At("RETURN"))
    public void onSearchChunksEnd(WorldServer ws, boolean b, boolean p_findChunksForSpawning_3_, boolean p_findChunksForSpawning_4_, CallbackInfoReturnable<Integer> cir) {
        world = null;
    }
}
