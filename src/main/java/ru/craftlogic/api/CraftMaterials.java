package ru.craftlogic.api;

import net.minecraft.block.material.MapColor;
import net.minecraft.block.material.Material;
import net.minecraft.item.EnumDyeColor;

public class CraftMaterials {
    public static final Material CRUDE_OIL = new Material(MapColor.BLACK);
    public static final Material MILK = new Material(MapColor.getBlockColor(EnumDyeColor.WHITE));
}
