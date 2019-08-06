package ru.craftlogic.mixin.block;

import net.minecraft.block.Block;
import net.minecraft.block.BlockDirt;
import net.minecraft.block.BlockDirt.DirtType;
import net.minecraft.block.BlockGrass;
import net.minecraft.block.material.Material;
import net.minecraft.block.state.IBlockState;
import net.minecraft.init.Blocks;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Overwrite;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import ru.craftlogic.api.world.Location;

import java.util.Random;

@Mixin(BlockGrass.class)
public class MixinBlockGrass extends Block {
    public MixinBlockGrass(Material material) {
        super(material);
    }

    /**
     * @author Radviger
     * @reason Weed growth
     */
    @Overwrite
    public void updateTick(World world, BlockPos pos, IBlockState state, Random random) {
        if (!world.isRemote) {
            Location location = new Location(world, pos);
            if (location.isAreaLoaded(3)) {
                Location up = location.offset(EnumFacing.UP);

                if (up.getLightFromNeighbors() < 4 && up.getBlockLightOpacity() > 2) {
                    location.setBlock(Blocks.DIRT);
                } else {
                    if (up.getLightFromNeighbors() >= 9) {
                        for (int i = 0; i < 4; ++i) {
                            Location r = location.randomize(random, 3);
                            Location rup = r.offset(EnumFacing.UP);

                            if (r.isHeightValid() && r.isBlockLoaded()) {
                                if (this.isDirt(r) && rup.getLightFromNeighbors() >= 4 && rup.getBlockLightOpacity() <= 2) {
                                    r.setBlock(Blocks.GRASS);
                                }
                            }
                        }
                    }
                }
            }
        }
    }

    private boolean isDirt(Location location) {
        return location.isSameBlock(Blocks.DIRT) && location.getBlockProperty(BlockDirt.VARIANT) == DirtType.DIRT;
    }
}
