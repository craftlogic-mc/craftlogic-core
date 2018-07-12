package ru.craftlogic.mixin.item;

import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.item.ItemSword;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import ru.craftlogic.api.item.Tool;

@Mixin(ItemSword.class)
public class MixinItemSword extends Item implements Tool {
    @Shadow @Final
    private ToolMaterial material;

    @Override
    public ToolMaterial getToolMaterial(ItemStack item) {
        return material;
    }
}
