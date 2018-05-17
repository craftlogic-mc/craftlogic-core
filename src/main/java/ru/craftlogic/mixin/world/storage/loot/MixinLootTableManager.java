package ru.craftlogic.mixin.world.storage.loot;

import com.google.common.cache.LoadingCache;
import net.minecraft.util.ResourceLocation;
import net.minecraft.world.storage.loot.LootTable;
import net.minecraft.world.storage.loot.LootTableManager;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Overwrite;
import org.spongepowered.asm.mixin.Shadow;

@Mixin(LootTableManager.class)
public class MixinLootTableManager {
    @Shadow @Final
    private LoadingCache<ResourceLocation, LootTable> registeredLootTables;

    @Overwrite
    public LootTable getLootTableFromLocation(ResourceLocation id) {
        if (id.toString().startsWith("minecraft:entities/sheep/")) {
            return this.getLootTableFromLocation(new ResourceLocation("craftlogic", id.getResourcePath()));
        }
        return this.registeredLootTables.getUnchecked(id);
    }
}
