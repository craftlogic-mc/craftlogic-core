package ru.craftlogic.api.block;

import net.minecraft.block.state.IBlockState;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.IBlockAccess;
import net.minecraftforge.common.EnumPlantType;
import net.minecraftforge.common.IPlantable;

public interface Plant extends IPlantable {
    @Override
    default EnumPlantType getPlantType(IBlockAccess blockAccessor, BlockPos pos) {
        return this.getPlantType(blockAccessor, pos, blockAccessor.getBlockState(pos)).forgeVariant;
    }

    PlantType getPlantType(IBlockAccess blockAccessor, BlockPos pos, IBlockState state);

    enum PlantType {
        PLAINS(EnumPlantType.Plains),
        DESERT(EnumPlantType.Desert),
        BEACH(EnumPlantType.Beach),
        CAVE(EnumPlantType.Cave),
        WATER(EnumPlantType.Water),
        NETHER(EnumPlantType.Nether),
        CROP(EnumPlantType.Crop);

        final EnumPlantType forgeVariant;

        PlantType(EnumPlantType forgeVariant) {
            this.forgeVariant = forgeVariant;
        }
    }
}
