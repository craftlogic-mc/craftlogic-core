package ru.craftlogic.api.screen.toast;

import net.minecraft.client.gui.toasts.IToast;

import java.util.function.Predicate;

public interface AdvancedToast {
    void remove(Predicate<IToast> filter);
}
