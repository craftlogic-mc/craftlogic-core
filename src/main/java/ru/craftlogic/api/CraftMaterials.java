package ru.craftlogic.api;

import net.minecraft.block.material.MapColor;
import net.minecraft.block.material.Material;
import net.minecraft.block.material.MaterialLiquid;
import net.minecraft.item.EnumDyeColor;

public class CraftMaterials {
    public static final Material CRUDE_OIL = new MaterialLiquid(MapColor.BLACK);
    public static final Material MILK = new MaterialLiquid(MapColor.getBlockColor(EnumDyeColor.WHITE));
}
