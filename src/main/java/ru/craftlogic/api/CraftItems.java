package ru.craftlogic.api;

import net.minecraft.creativetab.CreativeTabs;
import net.minecraft.item.Item;
import net.minecraftforge.fml.common.registry.GameRegistry;
import net.minecraftforge.fml.relauncher.Side;
import ru.craftlogic.CraftConfig;
import ru.craftlogic.api.item.ItemBase;
import ru.craftlogic.api.item.ItemFoodBase;
import ru.craftlogic.common.item.*;

import javax.annotation.Nonnull;

public class CraftItems {
    public static Item SPIT;
    public static Item ASH;
    public static Item MOSS;
    public static Item STONE_BRICK;
    public static Item RAW_EGG;
    public static Item FRIED_EGG;
    public static Item WOOL_CARD;
    public static Item CHAIN_LINKS;
    public static Item CHAIN_MESH;
    public static Item CROWBAR;
    public static Item MILK_BOTTLE;
    public static Item CREEPER_OYSTERS;
    public static Item GRASS;
    public static Item STONE_AXE_HEADING;
    public static Item BERRY;

    static void init(Side side) {
        SPIT = registerItem(new ItemBase("spit", CreativeTabs.MISC));
        ASH = registerItem(new ItemBase("ash", CreativeTabs.MATERIALS));
        if (CraftConfig.items.enableMoss) {
            MOSS = registerItem(new ItemMoss());
        }
        if (CraftConfig.items.enableStoneBricks) {
            STONE_BRICK = registerItem(new ItemBase("stone_brick", CreativeTabs.MATERIALS));
        }
        if (CraftConfig.items.enableRawEggs) {
            RAW_EGG = registerItem(new ItemFoodBase("egg_raw", CreativeTabs.FOOD, 4, 0.1F, false));
            FRIED_EGG = registerItem(new ItemFoodBase("egg_fried", CreativeTabs.FOOD, 5, 0.5F, false));
        }
        WOOL_CARD = registerItem(new ItemWoolCard());
        if (CraftConfig.items.enableChainCrafting) {
            CHAIN_LINKS = registerItem(new ItemBase("chain_links", CreativeTabs.MATERIALS));
            CHAIN_MESH = registerItem(new ItemBase("chain_mesh", CreativeTabs.MATERIALS));
        }
        CROWBAR = registerItem(new ItemCrowbar());
        MILK_BOTTLE = registerItem(new ItemMilkBottle());
        CREEPER_OYSTERS = registerItem(new ItemCreeperOysters());
        GRASS = registerItem(new ItemBase("grass", CreativeTabs.MATERIALS));
        STONE_AXE_HEADING = registerItem(new ItemBase("stone_axe_heading", CreativeTabs.TOOLS).setMaxStackSize(1));
        BERRY = registerItem(new ItemBerry());
    }

    public static Item registerItem(@Nonnull Item item) {
        GameRegistry.findRegistry(Item.class).register(item);
        return item;
    }
}
