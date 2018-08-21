package ru.craftlogic.api.block;

import ru.craftlogic.api.world.Location;

public interface Mossable {
    boolean isMossy(Location location);
    boolean growMoss(Location location);
}
