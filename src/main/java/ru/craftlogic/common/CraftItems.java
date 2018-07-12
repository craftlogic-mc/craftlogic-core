package ru.craftlogic.common;

import net.minecraft.creativetab.CreativeTabs;
import net.minecraft.item.Item;
import ru.craftlogic.api.item.ItemBase;
import ru.craftlogic.api.item.ItemFoodBase;
import ru.craftlogic.common.item.*;

import static ru.craftlogic.CraftLogic.registerItem;

public class CraftItems {
    public static Item ASH;
    public static Item THERMOMETER;
    public static Item ROCK;
    public static Item MOSS;
    public static Item STONE_BRICK;
    public static Item RAW_EGG;
    public static Item FRIED_EGG;
    public static Item WOOL_CARD;
    public static Item CROWBAR;

    static void init() {
        ASH = registerItem(new ItemBase("ash", CreativeTabs.MATERIALS));
        THERMOMETER = registerItem(new ItemThermometer());
        ROCK = registerItem(new ItemRock());
        MOSS = registerItem(new ItemBase("moss", CreativeTabs.MATERIALS));
        STONE_BRICK = registerItem(new ItemStoneBrick());
        RAW_EGG = registerItem(new ItemFoodBase("egg_raw", CreativeTabs.FOOD, 4, 0.1F, false));
        FRIED_EGG = registerItem(new ItemFoodBase("egg_fried", CreativeTabs.FOOD, 5, 0.5F, false));
        WOOL_CARD = registerItem(new ItemWoolCard());
        CROWBAR = registerItem(new ItemCrowbar());
    }
}
