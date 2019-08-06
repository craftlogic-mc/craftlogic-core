package ru.craftlogic.api.trees;

import com.ferreusveritas.dynamictrees.trees.TreeFamily;
import net.minecraft.util.ResourceLocation;
import ru.craftlogic.api.CraftAPI;

public abstract class TreeBase extends TreeFamily {
    protected TreeBase(ResourceLocation name) {
        super(name);
    }

    protected TreeBase(String name) {
        super(CraftAPI.wrapWithActiveModId(name, CraftAPI.MOD_ID));
    }
}
