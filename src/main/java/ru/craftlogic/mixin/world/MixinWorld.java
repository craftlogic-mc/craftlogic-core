package ru.craftlogic.mixin.world;

import ru.craftlogic.api.light.LightingEngineProvider;
import ru.craftlogic.common.world.lighting.LightingEngineFast;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.EnumSkyBlock;
import net.minecraft.world.World;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(World.class)
public abstract class MixinWorld implements LightingEngineProvider {
    private LightingEngineFast lightingEngine;

    /**
     * @author Angeline
     * Initialize the lighting engine on world construction.
     */
    @Inject(method = "<init>", at = @At("RETURN"))
    private void onConstructed(CallbackInfo ci) {
        this.lightingEngine = new LightingEngineFast((World) (Object) this);
    }

    /**
     * Directs the light update to the lighting engine and always returns a success value.
     * @author Angeline
     */
    @Inject(method = "checkLightFor", at = @At("HEAD"), cancellable = true)
    private void checkLightFor(EnumSkyBlock type, BlockPos pos, CallbackInfoReturnable<Boolean> cir) {
        this.lightingEngine.scheduleLightUpdate(type, pos);

        cir.setReturnValue(true);
    }

    @Override
    public LightingEngineFast getLightingEngine() {
        return this.lightingEngine;
    }
}
