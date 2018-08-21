package ru.craftlogic.coremod;

import net.minecraft.launchwrapper.LaunchClassLoader;
import net.minecraftforge.fml.relauncher.CoreModManager;
import net.minecraftforge.fml.relauncher.IFMLLoadingPlugin;
import net.minecraftforge.fml.relauncher.libraries.LibraryManager;
import net.minecraftforge.fml.relauncher.libraries.Repository;
import org.apache.commons.io.IOUtils;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.spongepowered.asm.launch.MixinBootstrap;
import org.spongepowered.asm.mixin.Mixins;

import javax.annotation.Nullable;
import java.io.File;
import java.io.FileOutputStream;
import java.io.InputStream;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.List;
import java.util.Map;

@IFMLLoadingPlugin.MCVersion("{@mc:version}")
@IFMLLoadingPlugin.Name("{@mod:id}-coremod")
@IFMLLoadingPlugin.SortingIndex(1002)
public class CraftLogicCoremod implements IFMLLoadingPlugin {
    private static final Logger LOGGER = LogManager.getLogger("CLC");

    private static final String GROOVY_VERSION = "2.4.4";
    private static final String MIXIN_VERSION = "0.7.7-SNAPSHOT";

    public CraftLogicCoremod() {
        if (!MIXIN_VERSION.startsWith(MixinBootstrap.VERSION)) {
            LOGGER.warn("Your classpath contains different version of sponge-mixin. Things may go wrong!");
        }

        MixinBootstrap.init();
        Mixins.addConfiguration("mixins.craftlogic.json");
    }

    @Override
    public String[] getASMTransformerClass() {
        return new String[0];
    }

    @Override
    public String getModContainerClass() {
        return "ru.craftlogic.coremod.CraftModContainer";
    }

    @Nullable
    @Override
    public String getSetupClass() {
        return null;
    }

    @Override
    public void injectData(Map<String, Object> data) {
        List<String> ignoredMods = CoreModManager.getIgnoredMods();
        ignoredMods.add("groovy-"+GROOVY_VERSION+".jar");
        ignoredMods.add("groovy-all-"+GROOVY_VERSION+".jar");

        Repository repo = LibraryManager.getDefaultRepo();

        String path = "/org/codehaus/groovy/groovy-all/"+GROOVY_VERSION+"/groovy-all-"+GROOVY_VERSION+".jar";
        File lib = repo.getFile(path);

        if (!lib.exists()) {
            LOGGER.info("Downloading groovy runtime library v"+GROOVY_VERSION);
            lib.getParentFile().mkdirs();
            try {
                URL url = new URL("http://central.maven.org/maven2" + path);
                try(InputStream is = url.openConnection().getInputStream();
                    FileOutputStream out = new FileOutputStream(lib)){

                    IOUtils.copyLarge(is, out);
                }
                LOGGER.info("Downloading done!");
            } catch (Exception e) {
                LOGGER.fatal("Failed to download groovy runtime library! Aborting server loading", e);
                throw new RuntimeException("Failed to download groovy runtime library", e);
            }
        }

        LaunchClassLoader cl = (LaunchClassLoader) this.getClass().getClassLoader();
        try {
            cl.addURL(lib.toURI().toURL());
            LOGGER.info("Successfully added groovy to the classpath");
        } catch (MalformedURLException e) {
            throw new RuntimeException(e);
        }

        cl.addTransformerExclusion("ru.craftlogic.coremod.asm.");
        cl.addTransformerExclusion("groovy.");
        cl.addTransformerExclusion("scala.");
        cl.addTransformerExclusion("org.codehaus.groovy.");
        cl.addTransformerExclusion("groovyjarjarantlr.");
        cl.addTransformerExclusion("groovyjarjarasm.");
        cl.addTransformerExclusion("groovyjarjarcommonscli.");
    }

    @Override
    public String getAccessTransformerClass() {
        return null;
    }

}
