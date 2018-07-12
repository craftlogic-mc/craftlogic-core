package ru.craftlogic.api.block;

import net.minecraft.util.math.BlockPos;
import net.minecraft.world.IBlockAccess;
import net.minecraftforge.common.EnumPlantType;
import net.minecraftforge.common.IPlantable;
import ru.craftlogic.api.world.Location;
import ru.craftlogic.api.world.LocationReadOnly;

public interface Plantable extends IPlantable {
    @Override
    default EnumPlantType getPlantType(IBlockAccess blockAccessor, BlockPos pos) {
        return this.getPlantType(new LocationReadOnly(blockAccessor, pos, null));
    }

    EnumPlantType getPlantType(Location location);
}
