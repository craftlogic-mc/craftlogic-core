package ru.craftlogic.mixin.world.storage.loot;

import net.minecraft.util.ResourceLocation;
import net.minecraft.world.storage.loot.LootTableList;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Overwrite;
import org.spongepowered.asm.mixin.Shadow;

import java.util.Set;

@Mixin(LootTableList.class)
public class MixinLootTableList {
    @Shadow @Final
    private static Set<ResourceLocation> LOOT_TABLES;

    @Overwrite
    public static ResourceLocation register(ResourceLocation id) {
        if (id.toString().startsWith("minecraft:entities/sheep/")) {
            id = new ResourceLocation("craftlogic", id.getResourcePath());
        }
        if (LOOT_TABLES.add(id)) {
            return id;
        } else {
            throw new IllegalArgumentException(id + " is already a registered built-in loot table");
        }
    }
}
