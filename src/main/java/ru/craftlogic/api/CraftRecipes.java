package ru.craftlogic.api;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonObject;
import com.google.gson.JsonParseException;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.item.crafting.FurnaceRecipes;
import net.minecraft.util.JsonUtils;
import net.minecraft.util.ResourceLocation;
import net.minecraftforge.fml.common.Loader;
import net.minecraftforge.fml.common.ModContainer;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.oredict.OreDictionary;
import org.apache.commons.io.FilenameUtils;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import ru.craftlogic.CraftConfig;
import ru.craftlogic.api.recipe.DictStack;
import ru.craftlogic.api.recipe.Recipe;
import ru.craftlogic.api.recipe.RecipeGrid;
import ru.craftlogic.api.util.Pair;
import ru.craftlogic.common.recipe.RecipeAlloying;
import ru.craftlogic.common.recipe.RecipeBarrelCompost;
import ru.craftlogic.common.recipe.RecipeGridAlloying;
import ru.craftlogic.common.recipe.RecipeGridBarrel;
import ru.craftlogic.util.FileExtensionFilter;

import java.io.BufferedReader;
import java.io.IOException;
import java.nio.file.Files;
import java.util.*;
import java.util.function.BiConsumer;

import static ru.craftlogic.api.CraftAPI.walkModResources;
import static ru.craftlogic.api.recipe.Recipe.parseItem;

public class CraftRecipes {
    private static final Map<Class<? extends RecipeGrid>, Map<ResourceLocation, Recipe>> RECIPES = new HashMap<>();
    private static final Map<String, Pair<Class<? extends RecipeGrid>, RecipeFactory>> LOADABLE_TYPES = new HashMap<>();
    private static final Logger LOGGER = LogManager.getLogger("CraftRecipes");
    private static final Gson GSON = new GsonBuilder().setPrettyPrinting().disableHtmlEscaping().create();

    static void init(Side side) {
        registerGridType(RecipeGridAlloying.class, "alloying", RecipeAlloying::new);
        registerGridType(RecipeGridBarrel.class, "composting", RecipeBarrelCompost::new);

        OreDictionary.registerOre("logWood", new ItemStack(CraftBlocks.PLANKS2, 1, OreDictionary.WILDCARD_VALUE));
        OreDictionary.registerOre("plankWood", new ItemStack(CraftBlocks.PLANKS2, 1, OreDictionary.WILDCARD_VALUE));
        OreDictionary.registerOre("stairWood", new ItemStack(CraftBlocks.PINE_STAIRS));
        OreDictionary.registerOre("stairWood", new ItemStack(CraftBlocks.WILLOW_STAIRS));
        OreDictionary.registerOre("fenceWood", new ItemStack(CraftBlocks.PINE_FENCE));
        OreDictionary.registerOre("fenceWood", new ItemStack(CraftBlocks.WILLOW_FENCE));
        OreDictionary.registerOre("fenceGateWood", new ItemStack(CraftBlocks.PINE_FENCE_GATE));
        OreDictionary.registerOre("fenceGateWood", new ItemStack(CraftBlocks.WILLOW_FENCE_GATE));
        OreDictionary.registerOre("treeSapling", new ItemStack(CraftBlocks.SAPLING2, 1, OreDictionary.WILDCARD_VALUE));
        OreDictionary.registerOre("treeLeaves", new ItemStack(CraftBlocks.LEAVES3, 1, OreDictionary.WILDCARD_VALUE));
    }

    static void postInit(Side side) {
        if (CraftConfig.items.enableRocks && CraftConfig.items.enableStoneBricks) {
            FurnaceRecipes.instance().addSmelting(Item.getItemFromBlock(CraftBlocks.ROCK), new ItemStack(CraftItems.STONE_BRICK), 0.15F);
        }
        if (!LOADABLE_TYPES.containsKey("smelting")) {
            parseJsonRecipes("smelting", (name, raw) -> {
                Object rawInput = parseItem(raw.get("input"));
                List<ItemStack> input = rawInput instanceof ItemStack ?
                    Collections.singletonList((ItemStack) rawInput) :
                    ((DictStack) rawInput).getAllVariants();

                LOGGER.info("Registered smelting recipe: " + name);

                for (ItemStack i : input) {
                    FurnaceRecipes.instance().addSmeltingRecipe(
                        i, (ItemStack) parseItem(raw.get("output")),
                        JsonUtils.getFloat(raw, "exp", 0F)
                    );
                }
            });
        }
        for (Map.Entry<String, Pair<Class<? extends RecipeGrid>, RecipeFactory>> entry : LOADABLE_TYPES.entrySet()) {
            String type = entry.getKey();
            Class<? extends RecipeGrid> grid = entry.getValue().first();
            RecipeFactory factory = entry.getValue().second();
            parseJsonRecipes(type, (name, raw) -> {
                LOGGER.info("Registered " + type + " recipe: " + name);
                registerRecipe(grid, factory.parse(name, raw));
            });
        }
    }

    @FunctionalInterface
    public interface RecipeFactory<R extends Recipe> {
        R parse(ResourceLocation name, JsonObject json);
    }

    private static void parseJsonRecipes(String type, BiConsumer<ResourceLocation, JsonObject> consumer) {
        for (ModContainer mod : Loader.instance().getActiveModList()) {
            walkModResources(mod, "assets/" + mod.getModId() + "/recipes/" + type,
                new FileExtensionFilter("json"),
                path -> {
                    String name = FilenameUtils.removeExtension(path.getFileName().toString()).replaceAll("\\\\", "/");
                    ResourceLocation id = new ResourceLocation(mod.getModId(), name);

                    try (BufferedReader reader = Files.newBufferedReader(path)) {
                        JsonObject raw = JsonUtils.fromJson(GSON, reader, JsonObject.class);
                        consumer.accept(id, raw);
                    } catch (JsonParseException exc) {
                        LOGGER.error("Parsing error loading recipe " + id, exc);
                    } catch (IOException exc) {
                        LOGGER.error("Couldn't read recipe " + id + " from " + path, exc);
                    }
                }
            );
        }
    }

    public static <G extends RecipeGrid> void registerGridType(Class<G> gridType, String gridName, RecipeFactory<? extends Recipe<G>> factory) {
        if (LOADABLE_TYPES.containsKey(gridName)) {
            throw new IllegalStateException("Grid type " + gridName + " already registered for class " + gridType);
        }
        LOADABLE_TYPES.put(gridName, Pair.of(gridType, factory));
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

    public static boolean isReservedRecipe(String name) {
        if (name.startsWith("smelting/")) {
            return true;
        }
        for (String type : LOADABLE_TYPES.keySet()) {
            if (name.startsWith(type + "/")) {
                return true;
            }
        }
        return false;
    }
}
