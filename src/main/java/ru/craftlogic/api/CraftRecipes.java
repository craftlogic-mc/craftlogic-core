package ru.craftlogic.api;

import net.minecraft.init.Items;
import net.minecraft.item.ItemStack;
import net.minecraft.item.crafting.FurnaceRecipes;
import net.minecraft.util.ResourceLocation;
import net.minecraftforge.fml.relauncher.Side;
import ru.craftlogic.api.recipe.DictStack;
import ru.craftlogic.api.recipe.Recipe;
import ru.craftlogic.api.recipe.RecipeGrid;
import ru.craftlogic.common.recipe.RecipeAlloying;
import ru.craftlogic.common.recipe.RecipeBarrelCompost;
import ru.craftlogic.common.recipe.RecipeGridAlloying;
import ru.craftlogic.common.recipe.RecipeGridBarrel;

import java.util.Comparator;
import java.util.HashMap;
import java.util.Map;
import java.util.TreeSet;

import static ru.craftlogic.api.CraftAPI.MOD_ID;

public class CraftRecipes {
    private static final Map<Class<? extends RecipeGrid>, Map<ResourceLocation, Recipe>> RECIPES = new HashMap<>();

    static void init(Side side) {
        FurnaceRecipes.instance().addSmelting(CraftItems.ROCK, new ItemStack(CraftItems.STONE_BRICK), 0.15F);

        CraftRecipes.registerRecipe(RecipeGridAlloying.class, new RecipeAlloying(
            new ResourceLocation(MOD_ID, "iron_nugget"),
            new ItemStack(Items.IRON_NUGGET, 8),
            1F,
            1538,
            300,
            new DictStack("oreIron")
        ));

        CraftRecipes.registerRecipe(RecipeGridAlloying.class, new RecipeAlloying(
            new ResourceLocation(MOD_ID, "iron_ingot"),
            new ItemStack(Items.IRON_INGOT),
            1F,
            1538,
            150,
            new DictStack("nuggetIron", 9)
        ));

        CraftRecipes.registerRecipe(RecipeGridBarrel.class, new RecipeBarrelCompost(
            new ResourceLocation(MOD_ID, "rotten_flesh"),
            new ItemStack(Items.ROTTEN_FLESH),
            0xF23028,
            130,
            100
        ));

        CraftRecipes.registerRecipe(RecipeGridBarrel.class, new RecipeBarrelCompost(
            new ResourceLocation(MOD_ID, "wheat"),
            new ItemStack(Items.WHEAT),
            0xF2D03C,
            200,
            80
        ));

        CraftRecipes.registerRecipe(RecipeGridBarrel.class, new RecipeBarrelCompost(
            new ResourceLocation(MOD_ID, "wheat_seeds"),
            new ItemStack(Items.WHEAT_SEEDS),
            0xBBF22B,
            200,
            15
        ));

        CraftRecipes.registerRecipe(RecipeGridBarrel.class, new RecipeBarrelCompost(
            new ResourceLocation(MOD_ID, "leaves"),
            new DictStack("treeLeaves"),
            0x21F22F,
            190,
            90
        ));

        CraftRecipes.registerRecipe(RecipeGridBarrel.class, new RecipeBarrelCompost(
            new ResourceLocation(MOD_ID, "beetroot"),
            new ItemStack(Items.BEETROOT),
            0xF20900,
            190,
            90
        ));

        CraftRecipes.registerRecipe(RecipeGridBarrel.class, new RecipeBarrelCompost(
            new ResourceLocation(MOD_ID, "beetroot_seeds"),
            new ItemStack(Items.BEETROOT_SEEDS),
            0xF2F16B,
            190,
            20
        ));

        CraftRecipes.registerRecipe(RecipeGridBarrel.class, new RecipeBarrelCompost(
            new ResourceLocation(MOD_ID, "carrot"),
            new ItemStack(Items.CARROT),
            0xF25900,
            170,
            70
        ));

        CraftRecipes.registerRecipe(RecipeGridBarrel.class, new RecipeBarrelCompost(
            new ResourceLocation(MOD_ID, "potato"),
            new ItemStack(Items.POTATO),
            0xD5F200,
            150,
            50
        ));

        CraftRecipes.registerRecipe(RecipeGridBarrel.class, new RecipeBarrelCompost(
            new ResourceLocation(MOD_ID, "poisonous_potato"),
            new ItemStack(Items.POISONOUS_POTATO),
            0xB3F201,
            120,
            35
        ));

        CraftRecipes.registerRecipe(RecipeGridBarrel.class, new RecipeBarrelCompost(
            new ResourceLocation(MOD_ID, "melon"),
            new ItemStack(Items.MELON),
            0xF2653E,
            100,
            35
        ));

        CraftRecipes.registerRecipe(RecipeGridBarrel.class, new RecipeBarrelCompost(
            new ResourceLocation(MOD_ID, "melon_seeds"),
            new ItemStack(Items.MELON_SEEDS),
            0x191D10,
            70,
            15
        ));

        CraftRecipes.registerRecipe(RecipeGridBarrel.class, new RecipeBarrelCompost(
            new ResourceLocation(MOD_ID, "pumpkin_seeds"),
            new ItemStack(Items.PUMPKIN_SEEDS),
            0xF1F2A4,
            90,
            25
        ));
    }

    public static <G extends RecipeGrid> void registerRecipe(Class<G> gridType, Recipe<G> recipe) {
        if (RECIPES.computeIfAbsent(gridType, k -> new HashMap<>()).put(recipe.getName(), recipe) != null) {
            throw new IllegalStateException("Recipe with name " + recipe.getName() + " already registered!");
        }
    }

    public static <G extends RecipeGrid, R extends Recipe<G>> R getMatchingRecipe(G grid) {
        R recipe = getMatchingRecipe(grid.getClass(), grid);
        if (recipe != null) {
            return recipe;
        } else {
            for (Class<?> itf : grid.getClass().getInterfaces()) {
                if (RecipeGrid.class.isAssignableFrom(itf)) {
                    recipe = getMatchingRecipe((Class<? extends RecipeGrid>) itf, grid);
                    if (recipe != null) {
                        return recipe;
                    }
                }
            }
        }
        return null;
    }

    public static <G extends RecipeGrid, R extends Recipe<G>> R getMatchingRecipe(Class<? extends RecipeGrid> clazz, G grid) {
        if (RecipeGrid.class.isAssignableFrom(clazz)) {
            if (RECIPES.containsKey(clazz)) {
                TreeSet<Recipe<G>> result = new TreeSet<>(Comparator.naturalOrder());
                for (Recipe<G> recipe : RECIPES.get(clazz).values()) {
                    if (recipe.matches(grid)) {
                        result.add(recipe);
                    }
                }
                if (!result.isEmpty()) {
                    return (R) result.first();
                }
            }
        }
        return null;
    }

    public static <G extends RecipeGrid, R extends Recipe<G>> R getByName(Class<G> matrixType, ResourceLocation name) {
        if (RECIPES.containsKey(matrixType)) {
            return (R) RECIPES.get(matrixType).get(name);
        }
        return null;
    }
}
