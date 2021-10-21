package ru.craftlogic.coremod.transformers;

import net.minecraft.launchwrapper.LaunchClassLoader;
import net.minecraftforge.fml.relauncher.CoreModManager;
import net.minecraftforge.fml.relauncher.libraries.LibraryManager;
import net.minecraftforge.fml.relauncher.libraries.Repository;
import org.apache.commons.io.IOUtils;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.objectweb.asm.ClassReader;
import org.objectweb.asm.tree.AnnotationNode;
import org.objectweb.asm.tree.ClassNode;
import ru.craftlogic.CraftConfig;
import ru.craftlogic.api.asm.ObfuscatedClassTransformer;
import ru.craftlogic.util.ArtifactInfo;
import ru.craftlogic.util.ReflectiveUsage;

import java.io.File;
import java.io.FileOutputStream;
import java.io.InputStream;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.*;

import static ru.craftlogic.api.asm.ObfuscatedClassTransformer.getAnnotationParameters;
import static ru.craftlogic.api.asm.ObfuscatedClassTransformer.getAnnotationsByType;

@ReflectiveUsage
public class RemoteDependencyTransformer implements ObfuscatedClassTransformer {
    private static final Set<String> LOADED_DEPENDENCIES = new HashSet<>();
    private static final Logger LOGGER = LogManager.getLogger("RemoteDependencyTransformer");

    @Override
    public byte[] transform(String name, String transformedName, byte[] basicClass) {
        if (basicClass != null) {
            ClassReader reader = new ClassReader(basicClass);
            ClassNode classNode = new ClassNode();
            reader.accept(classNode, 0);
            List<AnnotationNode> annotations = getAnnotationsByType(
                classNode.visibleAnnotations,
                "ru/craftlogic/api/dependency/RemoteDependency",
                "ru/craftlogic/api/dependency/RemoteDependencies"
            );
            for (AnnotationNode annotation : annotations) {
                Map<String, Object> params = getAnnotationParameters(annotation);
                loadRemoteDependency(
                    (String[]) params.getOrDefault("mirrors", new String[0]),
                    (String) params.get("value"),
                    (String) params.get("checkClass"),
                    (String[]) params.getOrDefault("transformerExclusions", new String[0])
                );
            }
        }
        return basicClass;
    }

    private static void loadRemoteDependency(String[] userMirrors, String value, String checkClass, String[] transformerExclusions) {
        ArtifactInfo info = new ArtifactInfo(value);

        LaunchClassLoader cl = (LaunchClassLoader) RemoteDependencyTransformer.class.getClassLoader();
        if (cl.findResource(checkClass.replace(',', '/').concat(".class")) != null) {
            return;
        }

        String fileName = info.artifactId + "-" + info.getClassifiedVersion() + "." + info.extension;
        if (LOADED_DEPENDENCIES.contains(fileName)) {
            return;
        }
        List<String> ignoredMods = CoreModManager.getIgnoredMods();
        ignoredMods.add(fileName);

        Repository repo = LibraryManager.getDefaultRepo();

        String path = info.buildPath();
        File lib = repo.getFile(path);

        if (!lib.exists()) {
            LOGGER.info("Downloading remote dependency: " + info.artifactId + " v" + info.version);
            lib.getParentFile().mkdirs();
            String[] mirrors = concat(userMirrors, CraftConfig.mavenMirrors);
            int errorCount = loadFileFromMirrors(mirrors, path, lib);
            if (errorCount > mirrors.length) {
                throw new IllegalStateException("Failed to download remote dependency! (" + errorCount + " tries total) Aborting server loading");
            } else {
                LOGGER.info("Downloading done!");
            }
        }
        try {
            cl.addURL(lib.toURI().toURL());
            LOADED_DEPENDENCIES.add(fileName);
            LOGGER.info("Successfully added remote dependency to the classpath");
        } catch (MalformedURLException e) {
            throw new RuntimeException(e);
        }

        for (String transformerExclusion : transformerExclusions) {
            cl.addTransformerExclusion(transformerExclusion);
        }
    }

    private static int loadFileFromMirrors(String[] mirrors, String path, File lib) {
        int errorCounter = 0;
        for (String host : mirrors) {
            try {
                LOGGER.info(host + path);
                URL url = new URL(host + path);
                try (InputStream is = url.openConnection().getInputStream();
                     FileOutputStream out = new FileOutputStream(lib)){

                    IOUtils.copyLarge(is, out);
                }
                return errorCounter;
            } catch (Exception e) {
                LOGGER.warn("Failed: " + e.toString());
                errorCounter++;
            }
        }
        return errorCounter + 1;
    }

    public static <T> T[] concat(T[] first, T[] second) {
        T[] result = Arrays.copyOf(first, first.length + second.length);
        System.arraycopy(second, 0, result, first.length, second.length);
        return result;
    }
}
