package ru.craftlogic.api.world;

import net.minecraft.command.ICommandSender;

public interface LocatableCommandSender extends CommandSender, Locatable {
    @Override
    default Location getLocation() {
        ICommandSender handle = unwrap();
        return new Location(handle.getEntityWorld(), handle.getPositionVector());
    }

    default World getWorld() {
        net.minecraft.world.World nmw = unwrap().getEntityWorld();
        return getServer().getWorldManager().get(Dimension.fromVanilla(nmw.provider.getDimensionType()));
    }

    default String getWorldName() {
        return getWorld().getName();
    }
}
