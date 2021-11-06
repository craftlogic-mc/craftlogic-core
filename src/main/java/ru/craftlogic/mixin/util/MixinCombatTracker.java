package ru.craftlogic.mixin.util;

import net.minecraft.util.CombatEntry;
import net.minecraft.util.CombatTracker;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import ru.craftlogic.api.util.AdvancedCombatTracker;

import java.util.List;

@Mixin(CombatTracker.class)
public class MixinCombatTracker implements AdvancedCombatTracker {
    @Shadow private boolean inCombat;
    @Shadow private boolean takingDamage;
    @Shadow private int lastDamageTime;
    @Shadow private int combatStartTime;
    @Shadow private int combatEndTime;
    @Shadow @Final private List<CombatEntry> combatEntries;

    @Override
    public boolean isInCombat() {
        return inCombat;
    }

    @Override
    public boolean isTakingDamage() {
        return takingDamage;
    }

    @Override
    public int getLastDamageTime() {
        return lastDamageTime;
    }

    @Override
    public int getCombatStartTime() {
        return combatStartTime;
    }

    @Override
    public int getCombatEndTime() {
        return combatEndTime;
    }

    @Override
    public List<CombatEntry> getCombatEntries() {
        return combatEntries;
    }
}
