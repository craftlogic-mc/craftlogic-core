package ru.craftlogic.common.script;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonObject;
import com.mojang.authlib.GameProfile;
import groovy.lang.Binding;
import groovy.lang.GroovyShell;
import groovy.lang.GroovySystem;
import net.minecraft.server.MinecraftServer;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.text.TextFormatting;
import net.minecraft.world.GameType;
import net.minecraftforge.fml.common.eventhandler.EventPriority;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.codehaus.groovy.control.CompilerConfiguration;
import org.codehaus.groovy.control.ErrorCollector;
import org.codehaus.groovy.control.MultipleCompilationErrorsException;
import org.codehaus.groovy.control.customizers.ImportCustomizer;
import ru.craftlogic.api.server.Server;
import ru.craftlogic.api.util.ConfigurableManager;
import ru.craftlogic.api.world.Dimension;
import ru.craftlogic.common.script.impl.ScriptFile;
import ru.craftlogic.common.script.impl.ScriptContainer;
import ru.craftlogic.common.script.impl.ScriptShell;
import ru.craftlogic.common.script.internal.CustomMetaClassCreationHandle;

import java.io.IOException;
import java.io.Reader;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.HashMap;
import java.util.Iterator;
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

    private GroovyShell compiler, shell;
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
            boolean obfuscated = false;
            try {
                MinecraftServer.class.getDeclaredMethod("getServer");
            } catch(NoSuchMethodException e) {
                obfuscated = true;
            }
            GroovySystem.getMetaClassRegistry().setMetaClassCreationHandle(new CustomMetaClassCreationHandle(obfuscated));

            CompilerConfiguration shellConfig = new CompilerConfiguration();
            shellConfig.setScriptBaseClass(ScriptShell.class.getName());

            this.shell = this.makeShell(new Binding(), shellConfig);

            try {
                Set<Path> scriptCandidates = Files
                    .list(this.scriptsDir)
                    .filter(f -> f.endsWith(".gs"))
                    .collect(Collectors.toSet());

                CompilerConfiguration compilerConfig = new CompilerConfiguration();
                compilerConfig.setScriptBaseClass(ScriptFile.class.getName());

                this.compiler = this.makeShell(new Binding(), compilerConfig);

                for (Path candidate : scriptCandidates) {
                    this.loadScript(candidate, false, true);
                }
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }

    private GroovyShell makeShell(Binding scriptProperties, CompilerConfiguration compilerConfig) {
        this.bind(scriptProperties);

        compilerConfig.setSourceEncoding("UTF-8");

        ImportCustomizer imports = new ImportCustomizer();
        imports.addImport("Dimension", Dimension.class.getName());
        imports.addImport("Priority", EventPriority.class.getName());
        imports.addImport("Profile", GameProfile.class.getName());
        imports.addImport("Facing", EnumFacing.class.getName());
        imports.addImport("TextFormatting", TextFormatting.class.getName());

        compilerConfig.addCompilationCustomizers(imports);

        return new GroovyShell(scriptProperties, compilerConfig);
    }

    private void bind(Binding binding) {
        for (EnumFacing facing : EnumFacing.values()) {
            binding.setVariable(facing.getName().toUpperCase(), facing);
        }
        for (TextFormatting formatting : TextFormatting.values()) {
            binding.setVariable(formatting.getFriendlyName().toUpperCase(), formatting);
        }
        for (GameType mode : GameType.values()) {
            if (mode != GameType.NOT_SET) {
                binding.setVariable(mode.getName().toUpperCase(), mode);
            }
        }
    }

    @Override
    public void save(JsonObject root) {


    }

    public void unload() {
        Iterator<Map.Entry<String, ScriptContainer>> iterator = this.loadedScripts.entrySet().iterator();
        while (iterator.hasNext()) {
            Map.Entry<String, ScriptContainer> entry = iterator.next();
            String id = entry.getKey();
            ScriptContainer container = entry.getValue();
            iterator.remove();
            long start = System.currentTimeMillis();
            container.unload();
            LOGGER.info("Unloaded script '{}' (Took {} ms)", id, System.currentTimeMillis() - start);
        }
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
        if (this.loadedScripts.containsKey(id)) {
            if (!reload) {
                return null;
            } else {
                this.unloadScript(id);
            }
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
        ScriptFile script = this.compile(id, Files.newBufferedReader(path));
        if (script != null) {
            LOGGER.info("Successfully compiled script '{}' (Took {} ms)", id, System.currentTimeMillis() - start);
            ScriptContainer container = new ScriptContainer(this, id, info, script);
            if (run) {
                container.run();
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

    private ScriptFile compile(String id, Reader reader) {
        try {
            return (ScriptFile) this.compiler.parse(reader, id + ".gs");
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

    public GroovyShell getCompiler() {
        return compiler;
    }

    public GroovyShell getShell() {
        return shell;
    }

    public Server getServer() {
        return server;
    }
}
