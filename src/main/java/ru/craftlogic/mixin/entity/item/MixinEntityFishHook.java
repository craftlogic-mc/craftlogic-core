package ru.craftlogic.mixin.entity.item;

import net.minecraft.entity.Entity;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.entity.projectile.EntityFishHook;
import net.minecraftforge.common.MinecraftForge;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import ru.craftlogic.api.event.player.PlayerHookEntityEvent;

@Mixin(EntityFishHook.class)
public abstract class MixinEntityFishHook {
    @Shadow public Entity caughtEntity;
    @Shadow private EntityPlayer angler;

    @Inject(method = "setHookedEntity", at = @At("HEAD"), cancellable = true)
    private void setHookedEntity(CallbackInfo ci) {
        if (MinecraftForge.EVENT_BUS.post(new PlayerHookEntityEvent(caughtEntity, angler))) {
            caughtEntity = null;
            ci.cancel();
        }
    }
}
