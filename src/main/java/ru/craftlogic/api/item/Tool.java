package ru.craftlogic.api.item;

import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;

public interface Tool {
    Item.ToolMaterial getToolMaterial(ItemStack item);
}
