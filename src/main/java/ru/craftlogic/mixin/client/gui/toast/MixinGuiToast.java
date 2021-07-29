package ru.craftlogic.mixin.client.gui.toast;

import com.google.common.collect.Queues;
import net.minecraft.client.gui.toasts.GuiToast;
import net.minecraft.client.gui.toasts.IToast;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import ru.craftlogic.api.screen.toast.AdvancedToast;
import ru.craftlogic.util.Reflection;

import javax.annotation.Nullable;
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


    @Nullable
    @Override
    public <T extends IToast> T getFirst(Class<? extends T> type) {
        Object[] visible = Reflection.getField(GuiToast.class, (GuiToast) (Object) this, "visible", "field_191791_g");
        if (visible != null) {
            for (Object o : visible) {
                if (o != null) {
                    IToast toast = (IToast) Reflection.getField((Class) o.getClass(), o, "toast", "field_193688_b");
                    if (type.isAssignableFrom(toast.getClass())) {
                        return (T) toast;
                    }
                }
            }
        }
        Deque<Object> queue = Reflection.getField(GuiToast.class, (GuiToast) (Object) this, "toastsQueue", "field_191792_h");
        if (queue != null) {
            for (Object o : queue) {
                IToast toast = (IToast) Reflection.getField((Class) o.getClass(), o, "toast", "field_193688_b");
                if (type.isAssignableFrom(toast.getClass())) {
                    return (T) toast;
                }
            }
        }

        return null;
    }

    @Mixin(targets = "net/minecraft/client/gui/toasts/GuiToast$ToastInstance")
    static class MixinToastInstance {

    }
}
