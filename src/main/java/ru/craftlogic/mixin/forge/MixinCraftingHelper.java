package ru.craftlogic.mixin.forge;

import com.google.gson.Gson;
import com.google.gson.JsonArray;
import com.google.gson.JsonObject;
import com.google.gson.JsonParseException;
import net.minecraft.item.crafting.IRecipe;
import net.minecraft.util.JsonUtils;
import net.minecraft.util.ResourceLocation;
import net.minecraftforge.common.crafting.CraftingHelper;
import net.minecraftforge.common.crafting.JsonContext;
import net.minecraftforge.fml.common.FMLLog;
import net.minecraftforge.fml.common.Loader;
import net.minecraftforge.fml.common.ModContainer;
import net.minecraftforge.fml.common.registry.ForgeRegistries;
import org.apache.commons.io.FilenameUtils;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Overwrite;
import org.spongepowered.asm.mixin.Shadow;
import ru.craftlogic.api.CraftRecipes;

import java.io.BufferedReader;
import java.io.IOException;
import java.lang.reflect.Method;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.function.BiFunction;
import java.util.function.Function;

@Mixin(value = CraftingHelper.class, remap = false)
public abstract class MixinCraftingHelper {
    @Shadow(remap = false) private static Gson GSON;

    @Shadow(remap = false)
    public static boolean findFiles(ModContainer mod, String base, Function<Path, Boolean> preprocessor, BiFunction<Path, Path, Boolean> processor, boolean defaultUnfoundRoot, boolean visitAllFiles) {
        return false;
    }

    @Shadow(remap = false)
    public static boolean processConditions(JsonArray conditions, JsonContext context) {
        return false;
    }

    @Shadow(remap = false)
    public static IRecipe getRecipe(JsonObject json, JsonContext context) {
        return null;
    }

    /**
     * @author Radviger
     * @reason Crafting system overhaul
     */
    @Overwrite(remap = false)
    private static boolean loadRecipes(ModContainer mod) {
        JsonContext ctx = new JsonContext(mod.getModId());
        return findFiles(mod, "assets/" + mod.getModId() + "/recipes", (root) -> {
            Path constp = root.resolve("_constants.json");
            if (Files.exists(constp)) {
                try (BufferedReader reader = Files.newBufferedReader(constp)) {
                    JsonObject[] json = JsonUtils.fromJson(GSON, reader, JsonObject[].class);
                    try {
                        Method m = ctx.getClass().getDeclaredMethod("loadConstants", JsonObject[].class);
                        m.setAccessible(true);
                        m.invoke(ctx, (Object)json);
                    } catch (ReflectiveOperationException ignored) {}
                    return true;
                } catch (IOException e) {
                    FMLLog.log.error("Error loading _constants.json: ", e);
                }

                return false;
            } else {
                return true;
            }
        }, (root, file) -> {
            Loader.instance().setActiveModContainer(mod);
            String relative = root.relativize(file).toString();
            if ("json".equals(FilenameUtils.getExtension(file.toString())) && !relative.startsWith("_")) {
                String name = FilenameUtils.removeExtension(relative).replaceAll("\\\\", "/");
                if (CraftRecipes.isReservedRecipe(name)) {
                    return true;
                }
                ResourceLocation key = new ResourceLocation(ctx.getModId(), name);

                try (BufferedReader reader = Files.newBufferedReader(file)) {
                    JsonObject json = JsonUtils.fromJson(GSON, reader, JsonObject.class);
                    if (!json.has("conditions") || processConditions(JsonUtils.getJsonArray(json, "conditions"), ctx)) {
                        IRecipe recipe = getRecipe(json, ctx);
                        ForgeRegistries.RECIPES.register(recipe.setRegistryName(key));
                        return true;
                    }

                } catch (JsonParseException e) {
                    FMLLog.log.error("Parsing error loading recipe {}", key, e);
                    return false;
                } catch (IOException e) {
                    FMLLog.log.error("Couldn't read recipe {} from {}", key, file, e);
                    return false;
                }
            }
            return true;
        }, true, true);
    }
}
