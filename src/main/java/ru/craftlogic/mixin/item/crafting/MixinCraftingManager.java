package ru.craftlogic.mixin.item.crafting;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonObject;
import com.google.gson.JsonParseException;
import net.minecraft.item.crafting.CraftingManager;
import net.minecraft.item.crafting.IRecipe;
import net.minecraft.util.JsonUtils;
import net.minecraft.util.ResourceLocation;
import org.apache.commons.io.FilenameUtils;
import org.apache.commons.io.IOUtils;
import org.apache.logging.log4j.Logger;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Overwrite;
import org.spongepowered.asm.mixin.Shadow;
import ru.craftlogic.util.DummyRecipe;

import java.io.BufferedReader;
import java.io.IOException;
import java.net.URI;
import java.net.URISyntaxException;
import java.net.URL;
import java.nio.file.*;
import java.util.Arrays;
import java.util.Collections;
import java.util.Iterator;
import java.util.List;

@Mixin(CraftingManager.class)
public class MixinCraftingManager {
    @Shadow @Final
    private static Logger LOGGER;

    private static List<String> DISABLED_RECIPES = Arrays.asList(
        "minecraft:diorite",
        "minecraft:andesite",
        "minecraft:granite",
        "minecraft:polished_diorite",
        "minecraft:polished_andesite",
        "minecraft:polished_granite"
    );

    /**
     * @author Radviger
     * @reason Dirty hook //FIXME!
     */
    @Overwrite
    private static boolean parseJsonRecipes() {
        FileSystem filesystem = null;
        Gson gson = (new GsonBuilder()).setPrettyPrinting().disableHtmlEscaping().create();

        try {
            try {
                URL url = CraftingManager.class.getResource("/assets/.mcassetsroot");
                if (url == null) {
                    LOGGER.error("Couldn't find .mcassetsroot");
                    return false;
                }

                URI uri = url.toURI();
                Path path;
                if ("file".equals(uri.getScheme())) {
                    path = Paths.get(CraftingManager.class.getResource("/assets/minecraft/recipes").toURI());
                } else {
                    if (!"jar".equals(uri.getScheme())) {
                        LOGGER.error("Unsupported scheme " + uri + " trying to list all recipes");
                        return false;
                    }

                    filesystem = FileSystems.newFileSystem(uri, Collections.emptyMap());
                    path = filesystem.getPath("/assets/minecraft/recipes");
                }

                Iterator<Path> iterator = Files.walk(path).iterator();

                while(iterator.hasNext()) {
                    Path p = iterator.next();
                    if ("json".equals(FilenameUtils.getExtension(p.toString()))) {
                        Path f = path.relativize(p);
                        String s = FilenameUtils.removeExtension(f.toString()).replaceAll("\\\\", "/");
                        ResourceLocation id = new ResourceLocation(s);

                        if (!isRecipeReplaced(id)) {
                            try (BufferedReader reader = Files.newBufferedReader(p)) {
                                register(s, parseRecipeJson(JsonUtils.fromJson(gson, reader, JsonObject.class)));
                            } catch (JsonParseException exc) {
                                LOGGER.error("Parsing error loading recipe " + id, exc);
                                //return false;
                            } catch (IOException exc) {
                                LOGGER.error("Couldn't read recipe " + id + " from " + p, exc);
                                //return false;
                            }
                        } else {
                            register(s, new DummyRecipe());
                        }
                    }
                }

                return true;
            } catch (URISyntaxException | IOException exc) {
                LOGGER.error("Couldn't get a list of all recipe files", exc);
            }
        } finally {
            IOUtils.closeQuietly(filesystem);
        }

        return false;
    }

    @Shadow
    private static void register(String id, IRecipe recipe) { }

    @Shadow
    private static IRecipe parseRecipeJson(JsonObject json) { return null; }

    private static boolean isRecipeReplaced(ResourceLocation id) {
        return DISABLED_RECIPES.contains(id.toString());
    }
}
