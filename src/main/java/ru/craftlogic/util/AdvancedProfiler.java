package ru.craftlogic.util;

import net.minecraft.profiler.Profiler;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.util.*;
import java.util.function.Supplier;

public class AdvancedProfiler extends Profiler {
    private static final Logger LOGGER = LogManager.getLogger();
    private final List<String> sections = new ArrayList<>();
    private final List<Long> timestamps = new ArrayList<>();
    private final long warnTime;
    private String currentSection = "";
    private final Map<String, Long> profilingData = new HashMap<>();

    public AdvancedProfiler(long warnTime) {
        this.warnTime = warnTime;
    }

    public void clear() {
        this.profilingData.clear();
        this.currentSection = "";
        this.sections.clear();
    }

    @Override
    public void startSection(String name) {
        if (this.profilingEnabled) {
            if (!this.currentSection.isEmpty()) {
                this.currentSection = this.currentSection + ".";
            }

            this.currentSection = this.currentSection + name;
            this.sections.add(this.currentSection);
            this.timestamps.add(System.nanoTime());
        }

    }

    public void startSection(Supplier<String> nameSupplier) {
        if (this.profilingEnabled) {
            this.startSection(nameSupplier.get());
        }

    }

    @Override
    public void endSection() {
        if (this.profilingEnabled) {
            long i = System.nanoTime();
            long j = this.timestamps.remove(this.timestamps.size() - 1);
            this.sections.remove(this.sections.size() - 1);
            long k = i - j;
            if (this.profilingData.containsKey(this.currentSection)) {
                this.profilingData.put(this.currentSection, this.profilingData.get(this.currentSection) + k);
            } else {
                this.profilingData.put(this.currentSection, k);
            }

            if (k > this.warnTime) {
                LOGGER.warn("Something's taking too long! '{}' took approx {} ms", this.currentSection, (double)k / 1000000D);
            }

            this.currentSection = this.sections.isEmpty() ? "" : this.sections.get(this.sections.size() - 1);
        }
    }

    @Override
    public List<AdvancedProfiler.Result> getProfilingData(String name) {
        if (!this.profilingEnabled) {
            return Collections.emptyList();
        } else {
            long rootTime = this.profilingData.getOrDefault("root", 0L);
            long sectionTime = this.profilingData.getOrDefault(name, -1L);

            List<AdvancedProfiler.Result> results = new ArrayList<>();
            if (!name.isEmpty()) {
                name += ".";
            }

            long k = 0L;

            for (String s : this.profilingData.keySet()) {
                if (s.length() > name.length() && s.startsWith(name) && s.indexOf(".", name.length() + 1) < 0) {
                    k += this.profilingData.get(s);
                }
            }

            float f = (float)k;
            if (k < sectionTime) {
                k = sectionTime;
            }

            if (rootTime < k) {
                rootTime = k;
            }

            for (String s3 : this.profilingData.keySet()) {
                if (s3.length() > name.length() && s3.startsWith(name) && s3.indexOf(".", name.length() + 1) < 0) {
                    long l = this.profilingData.get(s3);
                    double d0 = (double) l * 100D / (double) k;
                    double d1 = (double) l * 100D / (double) rootTime;
                    String s2 = s3.substring(name.length());
                    results.add(new Result(s2, d0, d1));
                }
            }

            this.profilingData.replaceAll((s, v) -> this.profilingData.get(s) * 999L / 1000L);

            if ((float)k > f) {
                results.add(new AdvancedProfiler.Result("unspecified", (double)((float)k - f) * 100D / (double)k, (double)((float)k - f) * 100D / (double)rootTime));
            }

            Collections.sort(results);
            results.add(0, new AdvancedProfiler.Result(name, 100D, (double)k * 100D / (double)rootTime));
            return results;
        }
    }

    @Override
    public void endStartSection(String name) {
        this.endSection();
        this.startSection(name);
    }

    @Override
    public String getNameOfLastSection() {
        return this.sections.isEmpty() ? "[UNKNOWN]" : this.sections.get(this.sections.size() - 1);
    }

    public void endStartSection(Supplier<String> nameSupplier) {
        this.endSection();
        this.startSection(nameSupplier);
    }
}

