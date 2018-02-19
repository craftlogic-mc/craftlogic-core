package ru.craftlogic.common;

import net.minecraft.entity.Entity;
import net.minecraft.entity.effect.EntityLightningBolt;
import net.minecraft.world.World;
import net.minecraftforge.event.entity.living.LivingSpawnEvent;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;
import ru.craftlogic.api.block.LightningStruckable;
import ru.craftlogic.api.world.Location;

public class CraftEventListener {
    @SubscribeEvent
    public void onEntitySpawn(LivingSpawnEvent event) {
        World world = event.getWorld();
        Entity entity = event.getEntity();
        Location location = new Location(entity);
        if (entity instanceof EntityLightningBolt) {
            System.out.println("Bolt: " + location);
            EntityLightningBolt lightningBolt = (EntityLightningBolt)entity;
            if (location.isBlockLoaded()) {
                LightningStruckable struckable = location.getBlock(LightningStruckable.class);
                if (struckable != null) {
                    struckable.onStruckByLightning(world, location, location.getBlockState(), lightningBolt);
                }
            }
        }
    }
}
