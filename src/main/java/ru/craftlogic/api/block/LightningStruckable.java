package ru.craftlogic.api.block;

import net.minecraft.block.state.IBlockState;
import net.minecraft.entity.effect.EntityLightningBolt;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;

public interface LightningStruckable {
    void onStruckByLightning(World world, BlockPos pos, IBlockState state, EntityLightningBolt lightningBolt);
}
