package ru.craftlogic.util;

import java.nio.file.Path;
import java.util.function.Predicate;

public class FileExtensionFilter implements Predicate<Path> {
    private final String extension;

    public FileExtensionFilter(String extension) {
        this.extension = extension;
    }

    @Override
    public boolean test(Path path) {
        return path.getFileName().toString().endsWith("." + this.extension);
    }
}
