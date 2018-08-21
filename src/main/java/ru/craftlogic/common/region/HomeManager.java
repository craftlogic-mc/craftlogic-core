package ru.craftlogic.common.region;

import com.google.gson.JsonObject;
import org.apache.logging.log4j.Logger;
import ru.craftlogic.api.util.ConfigurableManager;

import java.nio.file.Path;

public class HomeManager extends ConfigurableManager {
    private final RegionManager regionManager;

    public HomeManager(RegionManager regionManager, Path configFile, Logger logger) {
        super(regionManager.getServer(), configFile, logger);
        this.regionManager = regionManager;
    }

    @Override
    protected void load(JsonObject config) {

    }

    @Override
    protected void save(JsonObject config) {

    }
}
