package ru.craftlogic.api.util;

import net.minecraft.item.ItemStack;

import java.util.function.Predicate;

public class ItemStackMatcher {
    public static Predicate<ItemStack> typeOnly(ItemStack type) {
        return stack -> ItemStack.areItemsEqual(stack, type);
    }

    public static Predicate<ItemStack> typeAndTag(ItemStack type) {
        return typeOnly(type).and(stack -> ItemStack.areItemStackTagsEqual(stack, type));
    }
}
