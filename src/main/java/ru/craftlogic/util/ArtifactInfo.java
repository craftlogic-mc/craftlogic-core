package ru.craftlogic.util;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class ArtifactInfo {
    public final String groupId, artifactId, extension, classifier, version;

    public ArtifactInfo(String coords) {
        Pattern p = Pattern.compile("([^: ]+):([^: ]+)(:([^: ]*)(:([^: ]+))?)?:([^: ]+)");
        Matcher m = p.matcher(coords);
        if (!m.matches()) {
            throw new IllegalArgumentException("Bad artifact coordinates " + coords
                    + ", expected format is <groupId>:<artifactId>[:<extension>[:<classifier>]]:<version>");
        }
        this.groupId = m.group(1);
        this.artifactId = m.group(2);
        this.extension = get(m.group(4), "jar");
        this.classifier = get(m.group(6), "");
        this.version = m.group(7);
    }

    private static String get(String value, String defaultValue) {
        return (value == null || value.length() <= 0) ? defaultValue : value;
    }

    public String getClassifiedVersion() {
        return !this.classifier.isEmpty() ? this.version + "-" + this.classifier : this.version;
    }

    public String buildPath() {
        StringBuilder path = new StringBuilder("/");
        path.append(this.groupId.replace('.', '/'));
        path.append("/");
        path.append(this.artifactId);
        path.append("/");
        String version = this.getClassifiedVersion();
        path.append(version);
        path.append("/");
        path.append(this.artifactId);
        path.append("-");
        path.append(version);
        path.append(".");
        path.append(this.extension);
        return path.toString();
    }
}