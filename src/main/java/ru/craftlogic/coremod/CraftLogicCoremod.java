package ru.craftlogic.coremod;

import net.minecraft.launchwrapper.LaunchClassLoader;
import net.minecraftforge.fml.relauncher.CoreModManager;
import net.minecraftforge.fml.relauncher.IFMLLoadingPlugin;
import org.apache.commons.io.IOUtils;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.spongepowered.asm.launch.MixinBootstrap;
import org.spongepowered.asm.mixin.Mixins;
import ru.survivaltime.launcher.Cert;

import javax.annotation.Nullable;
import java.io.File;
import java.io.FileOutputStream;
import java.io.InputStream;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.List;
import java.util.Map;

@IFMLLoadingPlugin.MCVersion("1.12.2")
@IFMLLoadingPlugin.Name("craftlogic-coremod")
@IFMLLoadingPlugin.SortingIndex(1002)
public class CraftLogicCoremod implements IFMLLoadingPlugin {
    private static final Logger LOGGER = LogManager.getLogger("CLC");

    public CraftLogicCoremod() {
        Cert.setup();
        MixinBootstrap.init();
        Mixins.addConfiguration("mixins.craftlogic.json");
    }

    @Override
    public String[] getASMTransformerClass() {
        return new String[] {
            //getTransformer("TransformerEntityAnimals")
        };
    }

    private String getTransformer(String name) {
        return "ru.craftlogic.coremod.asm." + name;
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
        String groovyVersion = "2.4.4";

        List<String> ignoredMods = CoreModManager.getIgnoredMods();
        ignoredMods.add("groovy-"+groovyVersion+".jar");
        ignoredMods.add("groovy-all-"+groovyVersion+".jar");

        File mcDataDir = (File) data.get("mcLocation");


        String filename = "org/codehaus/groovy/groovy-all/"+groovyVersion+"/groovy-all-"+groovyVersion+".jar";

        File lib = new File(mcDataDir, "libraries/" + filename);
        if (!lib.exists()) {
            LOGGER.info("Downloading groovy runtime library v"+groovyVersion);
            lib.getParentFile().mkdirs();
            try {
                URL url = new URL("http://central.maven.org/maven2/" + filename);
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
