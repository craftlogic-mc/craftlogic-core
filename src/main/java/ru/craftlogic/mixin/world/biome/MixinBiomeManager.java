package ru.craftlogic.mixin.world.biome;

import net.minecraft.init.Biomes;
import net.minecraftforge.common.BiomeManager;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Redirect;
import ru.craftlogic.CraftConfig;

import java.util.List;

@Mixin(BiomeManager.class)
public class MixinBiomeManager {
    @Redirect(method = "setupBiomes", at = @At(value = "INVOKE", target = "Ljava/util/List;add(Ljava/lang/Object;)Z"), remap = false)
    private static boolean onBiomeSetup(List<Object> list, Object e) {
        if (e instanceof BiomeManager.BiomeEntry && CraftConfig.tweaks.disableRoofedForest) {
            BiomeManager.BiomeEntry entry = (BiomeManager.BiomeEntry) e;
            if (entry.biome == Biomes.ROOFED_FOREST) {
                return false;
            }
        }
        return list.add(e);
    }
}
