package ru.craftlogic.mixin.item;

import net.minecraft.item.Item;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.registry.RegistryNamespaced;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Overwrite;
import org.spongepowered.asm.mixin.Shadow;

@Mixin(Item.class)
public class MixinItem {
    @Shadow @Final
    public static RegistryNamespaced<ResourceLocation, Item> REGISTRY;

    @Overwrite
    private static void registerItem(int id, ResourceLocation name, Item item) {
        switch (name.toString()) {

        }
        REGISTRY.register(id, name, item);
    }
}
