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
        "http://central.maven.org/maven2"
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
        @Config.Comment("Enable ability to get a rock item from stone & cobblestone")
        public boolean enableRocks = true;

        @Config.RequiresMcRestart
        @Config.Comment("Enable ability to break eggs into raw ones by throwing them")
        public boolean enableRawEggs = true;

        @Config.RequiresMcRestart
        @Config.Comment("Enable chain armor crafting")
        public boolean enableChainCrafting = true;
    }

    public static Blocks blocks = new Blocks();

    public static class Blocks {
        @Config.Comment("Enable barrels (composting & liquid storage)")
        @Config.RequiresMcRestart
        public boolean enableBarrels = true;
    }

    public static Tweaks tweaks = new Tweaks();

    public static class Tweaks {
        @Config.RangeInt(min = 32, max = 128)
        @Config.Comment("Maximum size of a structure that a structure block can handle")
        public int maxStructureSize = 64;
        @Config.Comment("Reconnect delay on server crash")
        public int crashReconnectDelay = 60;

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
        @Config.Comment("Enable torch burnout")
        @Config.RequiresMcRestart
        public boolean enableTorchBurning = true;
        @Config.Comment("Enable diagonal fences")
        @Config.RequiresMcRestart
        public boolean enableDiagonalFences = true;
        @Config.Comment("Enable fancy gourd blocks growing stages (pumpkins & melons)")
        @Config.RequiresMcRestart
        public boolean enableFancyGourd = true;
        @Config.Comment("Enable stone unification (removes granite, andesite & diorite)")
        @Config.RequiresWorldRestart
        public boolean enableStoneUnification = true;
        @Config.Comment("Disable roofed (dark) forest")
        @Config.RequiresWorldRestart
        public boolean disableRoofedForest = true;
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
