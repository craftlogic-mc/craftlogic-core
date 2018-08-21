package ru.craftlogic.api;

import net.minecraftforge.fml.common.registry.GameRegistry;
import net.minecraftforge.fml.relauncher.Side;
import ru.craftlogic.api.barrel.BarrelModeCompost;
import ru.craftlogic.api.barrel.BarrelModeFluid;
import ru.craftlogic.api.barrel.BarrelModeType;

import javax.annotation.Nonnull;

public class CraftBarrelModes {
    public static final BarrelModeType COMPOST = new BarrelModeType("compost", BarrelModeCompost::new);
    public static final BarrelModeType FLUID = new BarrelModeType("fluid", BarrelModeFluid::new);

    static void init(Side side) {
        registerBarrelMode(COMPOST);
        registerBarrelMode(FLUID);
    }

    public static <M extends BarrelModeType> M registerBarrelMode(@Nonnull M mode) {
        GameRegistry.findRegistry(BarrelModeType.class).register(mode);
        return mode;
    }
}
