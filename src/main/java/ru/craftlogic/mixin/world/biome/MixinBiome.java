package ru.craftlogic.mixin.world.biome;

import net.minecraft.client.resources.I18n;
import net.minecraft.util.ResourceLocation;
import net.minecraft.world.biome.Biome;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Overwrite;
import org.spongepowered.asm.mixin.Shadow;

@Mixin(Biome.class)
public class MixinBiome {
    @Shadow @Final private String biomeName;

    /**
     * @author Radviger
     * @reason Localized biome names
     */
    @SideOnly(Side.CLIENT)
    @Overwrite
    public final String getBiomeName() {
        ResourceLocation name = ((Biome)(Object)this).getRegistryName();
        if (name != null) {
            String key = "biome." + name.getPath();
            if (I18n.hasKey(key)) {
                return I18n.format(key);
            }
        }
        return biomeName;
    }
}
