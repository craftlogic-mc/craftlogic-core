package ru.craftlogic.api.util;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonObject;
import org.apache.logging.log4j.Logger;
import ru.craftlogic.api.Server;
import ru.craftlogic.common.command.CommandManager;

import java.io.*;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;

public abstract class ConfigurableManager {
    protected static final Gson GSON = new GsonBuilder().setPrettyPrinting().create();

    protected final Server server;
    protected final Path configFile;
    protected final Logger logger;

    private boolean needsSave = false;
    private JsonObject config;

    public ConfigurableManager(Server server, Path configFile, Logger logger) {
        this.server = server;
        this.configFile = configFile;
        this.logger = logger;
    }

    public final Server getServer() {
        return server;
    }

    protected Path getConfigFile() {
        return this.configFile;
    }

    protected Logger getLogger() {
        return this.logger;
    }

    protected String getDefaultConfig() {
        return "/assets/craftlogic/config/" + getConfigFile().getFileName().toString();
    }

    public boolean isDirty() {
        return this.needsSave;
    }

    public void setDirty(boolean dirty) {
        this.needsSave = dirty;
    }

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

    public final void save() throws IOException {
        this.save(false);
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

    public void registerCommands(CommandManager commandManager) {}
}
