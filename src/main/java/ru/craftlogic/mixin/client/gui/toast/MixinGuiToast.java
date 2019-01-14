package ru.craftlogic.mixin.client.gui.toast;

import com.google.common.collect.Queues;
import net.minecraft.client.gui.toasts.GuiToast;
import net.minecraft.client.gui.toasts.IToast;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import ru.craftlogic.api.screen.toast.AdvancedToast;
import ru.craftlogic.util.Reflection;

import java.util.Deque;
import java.util.function.Predicate;

@Mixin(GuiToast.class)
public class MixinGuiToast implements AdvancedToast {
    @Shadow @Final
    private final Deque<IToast> toastsQueue = Queues.newArrayDeque();

    @Override
    public void remove(Predicate<IToast> filter) {
        Object[] visible = Reflection.getField(GuiToast.class, (GuiToast) (Object) this, "visible", "field_191791_g");
        if (visible != null) {
            for (int i = 0; i < visible.length; i++) {
                if (visible[i] != null) {
                    IToast toast = (IToast) Reflection.getField((Class)visible[i].getClass(), visible[i], "toast", "field_193688_b");
                    if (filter.test(toast)) {
                        visible[i] = null;
                    }
                }
            }
        }
        this.toastsQueue.removeIf(filter);
    }
}
