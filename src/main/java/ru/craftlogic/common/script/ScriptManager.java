package ru.craftlogic.common.script;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonObject;
import groovy.lang.Binding;
import groovy.lang.GroovyShell;
import net.minecraft.init.Blocks;
import net.minecraft.init.Items;
import net.minecraftforge.fml.common.eventhandler.EventPriority;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.codehaus.groovy.control.CompilerConfiguration;
import org.codehaus.groovy.control.ErrorCollector;
import org.codehaus.groovy.control.MultipleCompilationErrorsException;
import org.codehaus.groovy.control.customizers.ImportCustomizer;
import ru.craftlogic.api.Server;
import ru.craftlogic.api.util.ConfigurableManager;

import java.io.IOException;
import java.io.Reader;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.HashMap;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

public class ScriptManager extends ConfigurableManager {
    private static final Logger LOGGER = LogManager.getLogger("ScriptManager");
    private static final Gson GSON = new GsonBuilder().setPrettyPrinting().create();

    private final Server server;
    private final Path scriptsFile;
    private final Path scriptsDir;

    private boolean enabled;

    private Binding scriptProperties;
    private CompilerConfiguration compilerConfig;
    private GroovyShell shell;
    private Map<String, ScriptContainer> loadedScripts = new HashMap<>();

    public ScriptManager(Server server) {
        this.server = server;
        this.scriptsFile = server.getDataDirectory().resolve("scripts.json");
        this.scriptsDir = server.getDataDirectory().resolve("scripts/");
        if (!Files.exists(this.scriptsDir)) {
            try {
                Files.createDirectory(this.scriptsDir);
            } catch (IOException e) {
                LOGGER.error("Directory creation failed", e);
            }
        }
    }

    public boolean isEnabled() {
        return enabled;
    }

    @Override
    public Path getConfigFile() {
        return this.scriptsFile;
    }

    @Override
    public Logger getLogger() {
        return LOGGER;
    }

    @Override
    public void load(JsonObject root) {
        this.enabled = root.has("enabled") && root.get("enabled").getAsBoolean();
        if (this.enabled) {
            try {
                Set<Path> scriptCandidates = Files
                    .list(this.scriptsDir)
                    .filter(f -> f.endsWith(".gs"))
                    .collect(Collectors.toSet());

                CompilerConfiguration compilerConfig = new CompilerConfiguration();
                compilerConfig.setScriptBaseClass(Script.class.getName());
                compilerConfig.setSourceEncoding("UTF-8");

                ImportCustomizer imports = new ImportCustomizer();
                imports.addImports(
                    EventPriority.class.getName(),
                    Items.class.getName(),
                    Blocks.class.getName()
                );

                compilerConfig.addCompilationCustomizers(imports);
                this.compilerConfig = compilerConfig;
                this.scriptProperties = new Binding();
                this.shell = new GroovyShell(this.scriptProperties, this.compilerConfig);

                for (Path candidate : scriptCandidates) {
                    this.loadScript(candidate, false, true);
                }
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }

    @Override
    public void save(JsonObject root) {


    }

    public void unload() {
        this.loadedScripts.forEach((id, value) -> {
            long start = System.currentTimeMillis();
            if (this.unloadScript(id)) {
                LOGGER.info("Unloaded script '{}' (Took {} ms)", id, System.currentTimeMillis() - start);
            }
        });
    }

    public ScriptContainer loadScript(Path path, boolean reload, boolean run) throws IOException {
        String id = path.getFileName().toString();
        id = id.substring(0, id.lastIndexOf("."));
        return this.loadScript(id, path, reload, run);
    }

    public ScriptContainer loadScript(String id, boolean reload, boolean run) throws IOException {
        Path path = this.scriptsDir.resolve(id + ".gs");
        return this.loadScript(id, path, reload, run);
    }

    public ScriptContainer loadScript(String id, Path path, boolean reload, boolean run) throws IOException {
        if (!reload && this.loadedScripts.containsKey(id)) {
            return null;
        }
        if (!Files.exists(path)) {
            return null;
        }

        JsonObject info;
        Path infoFile = this.scriptsDir.resolve(id + ".json");
        if (Files.exists(infoFile) && Files.isRegularFile(infoFile)) {
            info = GSON.fromJson(Files.newBufferedReader(infoFile), JsonObject.class);
        } else {
            info = new JsonObject();
        }
        long start = System.currentTimeMillis();
        Script script = this.compile(id, Files.newBufferedReader(path));
        if (script != null) {
            LOGGER.info("Successfully compiled script '{}' (Took {} ms)", id, System.currentTimeMillis() - start);
            ScriptContainer container = new ScriptContainer(this, id, info, script);
            if (run) {
                container.script.run();
            }
            container.load();
            this.loadedScripts.put(id, container);
            return container;
        } else {
            return null;
        }
    }

    public boolean unloadScript(String id) {
        ScriptContainer container = this.loadedScripts.remove(id);
        if (container != null) {
            container.unload();
            return true;
        } else {
            return false;
        }
    }

    private Script compile(String id, Reader reader) {
        try {
            return (Script) this.shell.parse(reader, id + ".gs");
        } catch (MultipleCompilationErrorsException exc) {
            ErrorCollector collector = exc.getErrorCollector();
            int count = collector.getErrorCount();
            StringBuilder cause = new StringBuilder();
            for (int i = 0; i < count; i++) {
                Exception e = collector.getException(i);
                Throwable t = e.getCause();
                cause.append("\n CAUSE -> ").append(t != null ? t.getLocalizedMessage() : e.getLocalizedMessage());
            }
            LOGGER.error("Failed to compile script '{}' due to an error" + (count > 1 ? "s ("+count+" total)" : "") + ": {}", id, cause);
        } catch (Exception exc) {
            LOGGER.error("Failed to compile script '{}': ", id, exc);
        }
        return null;
    }

    public Set<String> getAllLoadedScripts() {
        return this.loadedScripts.keySet();
    }

    public boolean isLoaded(String id) {
        return this.loadedScripts.containsKey(id);
    }

    public GroovyShell getShell() {
        return shell;
    }

    public Server getServer() {
        return server;
    }
}
