package ru.craftlogic.api.screen;

import net.minecraft.util.ResourceLocation;
import ru.craftlogic.api.screen.element.ElementButton;
import ru.craftlogic.api.screen.element.ElementCheckBox;
import ru.craftlogic.api.screen.element.ElementGradient;
import ru.craftlogic.api.screen.element.ElementLabel;

import java.util.Collection;
import java.util.HashMap;
import java.util.Map;
import java.util.Set;
import java.util.function.BiFunction;

import static ru.craftlogic.api.CraftAPI.MOD_ID;
import static ru.craftlogic.api.CraftAPI.wrapWithActiveModId;

public final class Elements {
    private static final Map<ResourceLocation, BiFunction<ElementContainer, Map<String, Object>, Element>> REGISTRY = new HashMap<>();

    private static boolean init;

    public static void register(String id, BiFunction<ElementContainer, Map<String, Object>, Element> factory) {
        register(wrapWithActiveModId(id, MOD_ID), factory);
    }

    public static void register(ResourceLocation id, BiFunction<ElementContainer, Map<String, Object>, Element> factory) {
        if (REGISTRY.containsKey(id)) {
            throw new IllegalStateException("Element with id " + id + " is already registered!");
        }
        REGISTRY.put(id, factory);
    }

    public static BiFunction<ElementContainer, Map<String, Object>, Element> get(ResourceLocation id) {
        return REGISTRY.get(id);
    }

    public static <E extends Element> E get(ResourceLocation id, ElementContainer container, Map<String, Object> args) {
        return REGISTRY.containsKey(id) ? (E) get(id).apply(container, args) : null;
    }

    public static Set<ResourceLocation> keySet() {
        return REGISTRY.keySet();
    }

    public static Collection<BiFunction<ElementContainer, Map<String, Object>, Element>> values() {
        return REGISTRY.values();
    }

    public static void init() {
        if (init) {
            throw new IllegalStateException("Already initialized!");
        }
        init = true;

        register("button", ElementButton::new);
        register("checkbox", ElementCheckBox::new);
        register("label", ElementLabel::new);
        register("gradient", ElementGradient::new);
    }
}
