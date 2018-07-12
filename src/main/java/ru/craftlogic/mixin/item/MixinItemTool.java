package ru.craftlogic.mixin.item;

import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.item.ItemTool;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import ru.craftlogic.api.item.Tool;

@Mixin(ItemTool.class)
public class MixinItemTool extends Item implements Tool {
    @Shadow
    protected ToolMaterial toolMaterial;

    @Override
    public ToolMaterial getToolMaterial(ItemStack item) {
        return toolMaterial;
    }
}
