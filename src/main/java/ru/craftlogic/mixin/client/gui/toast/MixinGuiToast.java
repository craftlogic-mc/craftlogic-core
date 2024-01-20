package ru.craftlogic.mixin.client.gui.toast;

import com.google.common.collect.Queues;
import net.minecraft.client.gui.toasts.GuiToast;
import net.minecraft.client.gui.toasts.IToast;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import ru.craftlogic.api.screen.toast.AdvancedToast;

import javax.annotation.Nullable;
import java.util.Deque;
import java.util.function.Predicate;

@Mixin(GuiToast.class)
public class MixinGuiToast implements AdvancedToast {
    @Shadow @Final
    private final Deque<IToast> toastsQueue = Queues.newArrayDeque();
    @Shadow @Final
    public final GuiToast.ToastInstance<?>[] visible = new GuiToast.ToastInstance[5];

    @Override
    public void remove(Predicate<IToast> filter) {
        for (int i = 0; i < visible.length; i++) {
            if (visible[i] != null) {
                IToast toast = visible[i].toast;
                if (filter.test(toast)) {
                    visible[i] = null;
                }
            }
        }
        this.toastsQueue.removeIf(filter);
    }


    @Nullable
    @Override
    public <T extends IToast> T getFirst(Class<? extends T> type) {
        for (GuiToast.ToastInstance<?> o : visible) {
            if (o != null) {
                if (type.isAssignableFrom(o.toast.getClass())) {
                    return (T) o.toast;
                }
            }
        }
        for (IToast toast : toastsQueue) {
            if (type.isAssignableFrom(toast.getClass())) {
                return (T) toast;
            }
        }

        return null;
    }
}
