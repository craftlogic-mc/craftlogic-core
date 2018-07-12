package ru.craftlogic.common.plant;

import net.minecraft.block.Block;
import net.minecraft.init.Blocks;
import net.minecraft.util.EnumFacing;
import ru.craftlogic.api.plants.Plant;
import ru.craftlogic.api.plants.PlantSoil;
import ru.craftlogic.api.world.Location;

import java.util.Random;

public class PlantRedMushroom extends Plant {
    public PlantRedMushroom(Location location) {}

    @Override
    public Block getPlantBlock() {
        return Blocks.RED_MUSHROOM;
    }

    @Override
    public void randomTick(Random random) {
        PlantSoil soil = getSoil();
        Location sprig = getLocation().offset(EnumFacing.UP);
        if (soil.getWater() >= 300 || soil.getWater() >= 200 && random.nextInt(3) == 0) {
            if (sprig.setBlockIfPossible(getPlantBlock())) {
                soil.drainWater(300, false);
            }
        }
    }
}
