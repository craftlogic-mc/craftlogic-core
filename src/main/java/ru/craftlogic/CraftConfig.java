package ru.craftlogic;

import net.minecraftforge.common.config.Config;
import net.minecraftforge.common.config.ConfigManager;
import net.minecraftforge.fml.client.event.ConfigChangedEvent;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;

import static ru.craftlogic.api.CraftAPI.MOD_ID;

@Config(modid = MOD_ID)
public class CraftConfig {
    @Config.Comment("Default maven repositories for remote dependencies")
    public static String[] mavenMirrors = {
        "https://repo1.maven.org/maven2"
    };

    public static String banDateFormat = "yyyy-MM-dd HH:mm:ss Z";

    public static Items items = new Items();

    public static class Items {
        @Config.RequiresMcRestart
        @Config.Comment("Enable ability to get a moss from a mossy block")
        public boolean enableMoss = true;

        @Config.RequiresMcRestart
        @Config.Comment("Enable ability to get a stone brick item from block variant & rocks")
        public boolean enableStoneBricks = true;

        @Config.RequiresMcRestart
        @Config.Comment("Enable berries")
        public boolean enableBerries = true;

        @Config.RequiresMcRestart
        @Config.Comment("Enable poor rocks")
        public boolean enableRocks = true;

        @Config.RequiresMcRestart
        @Config.Comment("Enable poor rock drop from stone & cobblestone")
        public boolean enableRocksDrop = false;

        @Config.RequiresMcRestart
        @Config.Comment("Enable ability to break eggs into raw ones by throwing them")
        public boolean enableRawEggs = false;

        @Config.RequiresMcRestart
        @Config.Comment("Enable chain armor crafting")
        public boolean enableChainCrafting = true;
    }

    public static Blocks blocks = new Blocks();

    public static class Blocks {
        @Config.Comment("Enable barrels (composting & liquid storage)")
        @Config.RequiresMcRestart
        public boolean enableBarrels = true;
        @Config.Comment("Enable berry bush spreading")
        public boolean enableBerryBushSpreading = false;
    }

    public static Tweaks tweaks = new Tweaks();

    public static class Tweaks {
        @Config.RangeInt(min = 32, max = 128)
        @Config.Comment("Maximum size of a structure that a structure block can handle")
        public int maxStructureSize = 64;
        @Config.Comment("Reconnect delay on server crash")
        public int crashReconnectDelay = 60;
        @Config.Comment("Chicken egg lay delay (in ticks; used as %delay% + random[0, %delay%])")
        public int chickenEggLayDelay = 6000;

        @Config.Comment("Enable milk bucket tweaks")
        public boolean enableMilkBucketTweaks = true;
        @Config.Comment("Enable cow milking tweaks")
        public boolean enableCowMilkingTweaks = true;
        @Config.Comment("Enable visual snow layer tweaks (for flowers, tallgrass, rocks, etc.)")
        public boolean enableVisualSnowTweaks = true;
        @Config.Comment("Enable hiding full HUD bars")
        public boolean enableHidingFullHUDBars = false;
        @Config.Comment("Enable cobblestone gravity")
        public boolean enableCobblestoneGravity = true;
        @Config.Comment("Enable moss growing on cobblestone")
        public boolean enableCobblestoneMossGrowth = true;
        @Config.Comment("Enable moss decaying on mossy blocks")
        public boolean enableMossDecay = true;
        @Config.Comment("Enable cracked stonebrick gravity")
        public boolean enableCrackedStoneBrickGravity = true;
        @Config.Comment("Enable moss growing on stonebrick")
        public boolean enableStoneBrickMossGrowth = true;
        @Config.Comment("Enable stonebrick cracking")
        public boolean enableStoneBrickCracking = true;
        @Config.Comment("Enable moss spreading from mossy blocks")
        public boolean enableMossSpreading = true;
        @Config.Comment("Enable stone cracking into cobblestone")
        public boolean enableStoneCracking = true;
        @Config.Comment("Enable sticks from leaf")
        public boolean enableSticksFromLeaf = true;
        @Config.Comment("Flowers and mushrooms require shears to collect them")
        public boolean flowersAndMushroomsRequireShears = false;
        @Config.Comment("Enable torch burnout")
        @Config.RequiresMcRestart
        public boolean enableTorchBurning = true;
        @Config.Comment("Enable diagonal fences")
        @Config.RequiresMcRestart
        public boolean enableDiagonalFences = true;
        @Config.Comment("Enable hanging doors")
        @Config.RequiresMcRestart
        public boolean enableHangingDoors = false;
        @Config.Comment("Enable fancy gourd blocks growing stages (pumpkins & melons)")
        @Config.RequiresMcRestart
        public boolean enableFancyGourd = true;
        @Config.Comment("Enable stone unification (removes granite, andesite & diorite)")
        @Config.RequiresWorldRestart
        public boolean enableStoneUnification = true;
        @Config.Comment("Disable roofed (dark) forest")
        @Config.RequiresWorldRestart
        public boolean disableRoofedForest = true;
        @Config.Comment("Disable log breaking by hand")
        @Config.RequiresWorldRestart
        public boolean disableHandLogBreaking = false;
        @Config.Comment("Disable damage indicator particles")
        @Config.RequiresWorldRestart
        public boolean disableDamageParticles = false;
        @Config.Comment("Keep other dimensions from unloading to prevent load/unload lag")
        @Config.RequiresWorldRestart
        public boolean keepDimensionsLoaded = true;
    }

    @Mod.EventBusSubscriber(modid = MOD_ID)
    private static class EventHandler {
        @SubscribeEvent
        public static void onConfigChangedEvent(final ConfigChangedEvent.OnConfigChangedEvent event) {
            if (event.getModID().equals(MOD_ID)) {
                ConfigManager.sync(MOD_ID, Config.Type.INSTANCE);
            }
        }
    }
}
