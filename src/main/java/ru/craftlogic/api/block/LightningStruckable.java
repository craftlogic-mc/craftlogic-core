package ru.craftlogic.api.block;

import net.minecraft.entity.effect.EntityLightningBolt;
import ru.craftlogic.api.world.Location;

public interface LightningStruckable {
    void onStruckByLightning(Location location, EntityLightningBolt bolt, boolean effectOnly);
}
