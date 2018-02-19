package ru.craftlogic.api.util;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonObject;
import org.apache.logging.log4j.Logger;

import java.io.IOException;
import java.io.Reader;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;

public interface JsonConfiguration {
    Gson GSON = new GsonBuilder().setPrettyPrinting().create();

    Path getConfigFile();
    Logger getLogger();
    boolean isDirty();
    void setDirty(boolean dirty);

    default void load() throws IOException {
        Path configFile = this.getConfigFile();
        JsonObject root = null;
        if (!Files.exists(configFile)) {
            Files.createFile(configFile);
            getLogger().warn("Configuration file is missing! Creating empty one...");
        } else {
            try (Reader reader = Files.newBufferedReader(configFile)) {
                root = GSON.fromJson(reader, JsonObject.class);
            }
        }
        if (root == null) {
            root = new JsonObject();
        }
        this.load0(root);
        if (this.isDirty()) {
            this.setDirty(false);
            Files.write(configFile, GSON.toJson(root).getBytes(StandardCharsets.UTF_8));
        }
    }

    void load0(JsonObject root);

    default void save() throws IOException {
        this.save(false);
    }

    default void save(boolean force) throws IOException {
        if (force || this.isDirty()) {
            JsonObject root = new JsonObject();
            this.save0(root);
            Files.write(this.getConfigFile(), GSON.toJson(root).getBytes(StandardCharsets.UTF_8));
        }
    }

    void save0(JsonObject root);
}
