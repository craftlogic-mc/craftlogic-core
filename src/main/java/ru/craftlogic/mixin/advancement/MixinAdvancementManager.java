package ru.craftlogic.mixin.advancement;

import com.google.gson.Gson;
import com.google.gson.JsonParseException;
import net.minecraft.advancements.Advancement;
import net.minecraft.advancements.AdvancementManager;
import net.minecraft.item.crafting.CraftingManager;
import net.minecraft.util.JsonUtils;
import net.minecraft.util.ResourceLocation;
import org.apache.commons.io.FilenameUtils;
import org.apache.commons.io.IOUtils;
import org.apache.logging.log4j.Logger;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Overwrite;
import org.spongepowered.asm.mixin.Shadow;

import java.io.BufferedReader;
import java.io.IOException;
import java.net.URI;
import java.net.URISyntaxException;
import java.net.URL;
import java.nio.file.*;
import java.util.Collections;
import java.util.Iterator;
import java.util.Map;

@Mixin(AdvancementManager.class)
public class MixinAdvancementManager {
    @Shadow @Final
    private static Logger LOGGER;
    @Shadow @Final
    public static Gson GSON;
    @Shadow
    private boolean hasErrored;

    @Overwrite
    private void loadBuiltInAdvancements(Map<ResourceLocation, Advancement.Builder> registry) {
        FileSystem filesystem = null;

        try {
            URL url = AdvancementManager.class.getResource("/assets/.mcassetsroot");
            if (url == null) {
                LOGGER.error("Couldn't find .mcassetsroot");
                this.hasErrored = true;
                return;
            }

            URI uri = url.toURI();
            Path path;
            if ("file".equals(uri.getScheme())) {
                path = Paths.get(CraftingManager.class.getResource("/assets/minecraft/advancements").toURI());
            } else {
                if (!"jar".equals(uri.getScheme())) {
                    LOGGER.error("Unsupported scheme " + uri + " trying to list all built-in advancements (NYI?)");
                    this.hasErrored = true;
                    return;
                }

                filesystem = FileSystems.newFileSystem(uri, Collections.emptyMap());
                path = filesystem.getPath("/assets/minecraft/advancements");
            }

            Iterator<Path> iterator = Files.walk(path).iterator();

            while(iterator.hasNext()) {
                Path p = iterator.next();
                if ("json".equals(FilenameUtils.getExtension(p.toString()))) {
                    Path f = path.relativize(p);
                    String s = FilenameUtils.removeExtension(f.toString()).replaceAll("\\\\", "/");
                    ResourceLocation id = new ResourceLocation("minecraft", s);
                    if (!registry.containsKey(id) || isAdvancementDisabled(id)) {
                        try (BufferedReader reader = Files.newBufferedReader(p)) {
                            Advancement.Builder advancement$builder = JsonUtils.fromJson(GSON, reader, Advancement.Builder.class);
                            registry.put(id, advancement$builder);
                        } catch (JsonParseException exc) {
                            LOGGER.error("Parsing error loading built-in advancement " + id, exc);
                            this.hasErrored = true;
                        } catch (IOException exc) {
                            LOGGER.error("Couldn't read advancement " + id + " from " + p, exc);
                            this.hasErrored = true;
                        }
                    }
                }
            }
        } catch (URISyntaxException | IOException exc) {
            LOGGER.error("Couldn't get a list of all built-in advancement files", exc);
            this.hasErrored = true;
        } finally {
            IOUtils.closeQuietly(filesystem);
        }

    }

    private static boolean isAdvancementDisabled(ResourceLocation id) {
        switch (id.toString()) {
            case "minecraft:recipes/building_blocks/string_to_wool": {
                return true;
            }
        }
        return false;
    }
}
