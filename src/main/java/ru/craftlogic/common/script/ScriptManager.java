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
import net.minecraft.util.JsonUtils;
import net.minecraft.util.text.TextFormatting;
import net.minecraft.world.GameType;
import net.minecraftforge.fml.common.eventhandler.EventPriority;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.codehaus.groovy.control.CompilerConfiguration;
import org.codehaus.groovy.control.ErrorCollector;
import org.codehaus.groovy.control.MultipleCompilationErrorsException;
import org.codehaus.groovy.control.customizers.ImportCustomizer;
import ru.craftlogic.api.Server;
import ru.craftlogic.api.util.ConfigurableManager;
import ru.craftlogic.api.world.Dimension;
import ru.craftlogic.common.command.CommandManager;
import ru.craftlogic.common.script.impl.ScriptContainer;
import ru.craftlogic.common.script.impl.ScriptContainerFile;
import ru.craftlogic.common.script.impl.ScriptFile;
import ru.craftlogic.common.script.impl.ScriptShell;
import ru.craftlogic.common.script.internal.CustomMetaClassCreationHandle;

import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.Reader;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.*;
import java.util.stream.Collectors;

public class ScriptManager extends ConfigurableManager {
    private static final Logger LOGGER = LogManager.getLogger("ScriptManager");
    private static final Gson GSON = new GsonBuilder().setPrettyPrinting().create();

    private final Path scriptsDir;

    private boolean enabled;

    private GroovyShell compiler, shell;
    private Map<String, ScriptContainerFile> loadedScripts = new HashMap<>();

    public ScriptManager(Server server, Path settingsDirectory) {
        super(server, settingsDirectory.resolve("scripts.json"), LOGGER);

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
    public void registerCommands(CommandManager commandManager) {
        commandManager.registerCommandContainer(ScriptCommands.class);
    }

    @Override
    public void load(JsonObject config) {
        this.enabled = JsonUtils.getBoolean(config, "enabled", false);
        if (this.enabled) {
            boolean obfuscated = false;
            try {
                MinecraftServer.class.getDeclaredMethod("getServer");
            } catch(NoSuchMethodException e) {
                obfuscated = true;
            }
            GroovySystem.getMetaClassRegistry().setMetaClassCreationHandle(new CustomMetaClassCreationHandle(obfuscated));

            Events.init();

            CompilerConfiguration shellConfig = new CompilerConfiguration();
            shellConfig.setScriptBaseClass(ScriptShell.class.getName());

            this.shell = makeShell(new Binding(), shellConfig);

            try {
                Set<Path> scriptCandidates = Files
                    .list(this.scriptsDir)
                    .filter(f -> f.toString().endsWith(".gs"))
                    .collect(Collectors.toSet());

                CompilerConfiguration compilerConfig = new CompilerConfiguration();
                compilerConfig.setScriptBaseClass(ScriptFile.class.getName());

                Binding binding = new Binding();
                binding.setProperty("$server", this.server);

                this.compiler = makeShell(binding, compilerConfig);

                for (Path candidate : scriptCandidates) {
                    this.loadScript(candidate, false, true);
                }
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }

    public static GroovyShell makeShell(Binding scriptProperties, CompilerConfiguration compilerConfig) {
        bind(scriptProperties);

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

    private static void bind(Binding binding) {
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
    public void save(JsonObject config) {
        config.addProperty("enabled", this.enabled);
    }

    public void unload() {
        Iterator<Map.Entry<String, ScriptContainerFile>> iterator = this.loadedScripts.entrySet().iterator();
        while (iterator.hasNext()) {
            Map.Entry<String, ScriptContainerFile> entry = iterator.next();
            String id = entry.getKey();
            ScriptContainerFile container = entry.getValue();
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
            throw new FileNotFoundException(id + ".gs");
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
            ScriptContainerFile container = new ScriptContainerFile(this, id, info, script);
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
        ScriptContainerFile container = this.loadedScripts.remove(id);
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

    public Collection<ScriptContainerFile> getAllLoadedScripts() {
        return this.loadedScripts.values();
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
}
