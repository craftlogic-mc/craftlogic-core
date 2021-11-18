package ru.craftlogic.mixin.world;

import net.minecraft.world.Teleporter;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.Constant;
import org.spongepowered.asm.mixin.injection.ModifyConstant;

@Mixin(Teleporter.class)
public class MixinTeleporter {

    @ModifyConstant(method = "placeInExistingPortal", constant = @Constant(intValue = -128))
    public int negSearchRange(int old) {
        return -32;
    }

    @ModifyConstant(method = "placeInExistingPortal", constant = @Constant(intValue = 128))
    public int posSearchRange(int old) {
        return 32;
    }

}
