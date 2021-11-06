package ru.craftlogic.api.util;

import net.minecraft.util.CombatEntry;

import java.util.List;

public interface AdvancedCombatTracker {
    boolean isInCombat();
    boolean isTakingDamage();
    int getLastDamageTime();
    int getCombatStartTime();
    int getCombatEndTime();
    List<CombatEntry> getCombatEntries();
}
