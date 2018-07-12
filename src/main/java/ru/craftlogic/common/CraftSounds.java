package ru.craftlogic.common;

import net.minecraft.util.SoundEvent;

import static ru.craftlogic.CraftLogic.registerSoundEvent;

public class CraftSounds {
    public static SoundEvent FURNACE_VENT_OPEN, FURNACE_VENT_CLOSE, FURNACE_HOT_LOOP;

    static void init() {
        FURNACE_VENT_OPEN = registerSoundEvent("furnace.vent.open");
        FURNACE_VENT_CLOSE = registerSoundEvent("furnace.vent.close");
        FURNACE_HOT_LOOP = registerSoundEvent("furnace.hot.loop");
    }
}
