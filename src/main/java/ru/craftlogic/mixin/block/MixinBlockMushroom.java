package ru.craftlogic.mixin.block;

import net.minecraft.block.*;
import net.minecraft.block.material.Material;
import net.minecraft.block.state.IBlockState;
import net.minecraft.init.Blocks;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Overwrite;

@Mixin(BlockMushroom.class)
public class MixinBlockMushroom extends BlockBush {
    public MixinBlockMushroom(Material material) {
        super(material);
    }

    @Override
    public SoundType getSoundType() {
        return SoundType.CLOTH;
    }

    /**
     * @author Radviger
     * @reason Only place mushrooms over soil blocks
     */
    @Overwrite
    public boolean canBlockStay(World world, BlockPos pos, IBlockState state) {
        if (pos.getY() >= 0 && pos.getY() < 256) {
            IBlockState soil = world.getBlockState(pos.down());
            Block block = soil.getBlock();
            return block == Blocks.MYCELIUM
                || block == Blocks.DIRT && soil.getValue(BlockDirt.VARIANT) == BlockDirt.DirtType.PODZOL
                || block == Blocks.GRASS;
        }
        return false;
    }
}
