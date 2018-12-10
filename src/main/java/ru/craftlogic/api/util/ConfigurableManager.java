package ru.craftlogic.api.util;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonObject;
import org.apache.logging.log4j.Logger;
import ru.craftlogic.api.CraftAPI;
import ru.craftlogic.api.server.Server;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.io.Reader;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;

public abstract class ConfigurableManager extends ServerManager {
    protected static final Gson GSON = new GsonBuilder().setPrettyPrinting().create();

    protected final Path configFile;

    private boolean needsSave = false;
    private JsonObject config;

    protected ConfigurableManager(Server server, Path configFile, Logger logger) {
        super(server, logger);
        this.configFile = configFile;
    }

    protected Path getConfigFile() {
        return this.configFile;
    }

    protected String getModId() {
        return CraftAPI.MOD_ID;
    }

    protected String getDefaultConfig() {
        return "/assets/" + getModId() + "/config/" + getConfigFile().getFileName().toString();
    }

    public boolean isDirty() {
        return this.needsSave;
    }

    public void setDirty(boolean dirty) {
        this.needsSave = dirty;
    }

    @Override
    public final void load() throws IOException {
        Path configFile = this.getConfigFile();
        JsonObject root = null;
        if (!Files.exists(configFile)) {
            if (!Files.exists(configFile.getParent())) {
                Files.createDirectories(configFile.getParent());
            }
            Files.createFile(configFile);
            getLogger().warn("Configuration file is missing! Creating empty one...");
            String defaultConfig = getDefaultConfig();
            if (defaultConfig != null) {
                try (InputStream is = getClass().getResourceAsStream(defaultConfig)) {
                    if (is != null) {
                        try (OutputStream os = Files.newOutputStream(configFile)) {
                            byte[] buffer = new byte[8192];
                            int read;
                            while ((read = is.read(buffer, 0, 8192)) >= 0) {
                                os.write(buffer, 0, read);
                            }
                        }
                        getLogger().info("Loaded defaults!");
                        try (Reader reader = Files.newBufferedReader(configFile)) {
                            root = GSON.fromJson(reader, JsonObject.class);
                        }
                    }
                }
            }
        } else {
            try (Reader reader = Files.newBufferedReader(configFile)) {
                root = GSON.fromJson(reader, JsonObject.class);
            }
        }
        if (root == null) {
            root = new JsonObject();
        }
        this.config = root;
        this.load(this.config);
        if (this.isDirty()) {
            this.setDirty(false);
            Files.write(configFile, GSON.toJson(this.config).getBytes(StandardCharsets.UTF_8));
        }
    }

    protected abstract void load(JsonObject config);

    @Override
    public final void save() throws IOException {
        this.save(false);
    }

    @Override
    public void unload() throws Exception {
        this.save(true);
    }

    public final void save(boolean force) throws IOException {
        if (force || this.isDirty()) {
            JsonObject root = this.config;
            if (root == null) {
                root = new JsonObject();
            }
            this.save(root);
            this.config = root;
            Path configFile = this.getConfigFile();
            if (!Files.exists(configFile.getParent())) {
                Files.createDirectories(configFile.getParent());
            }
            Files.write(configFile, GSON.toJson(root).getBytes(StandardCharsets.UTF_8));
        }
    }

    protected abstract void save(JsonObject config);
}
