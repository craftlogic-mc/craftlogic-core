package ru.craftlogic.mixin.block;

import net.minecraft.block.BlockBush;
import net.minecraft.block.BlockMushroom;
import net.minecraft.block.SoundType;
import net.minecraft.block.material.Material;
import net.minecraft.block.state.IBlockState;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Overwrite;
import ru.craftlogic.api.plants.PlantSoil;
import ru.craftlogic.api.world.TileEntities;

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
     */
    @Overwrite
    public boolean canBlockStay(World world, BlockPos pos, IBlockState state) {
        if (pos.getY() >= 0 && pos.getY() < 256) {
            PlantSoil soil = TileEntities.getTileEntity(world, pos.down(), PlantSoil.class);
            return soil != null;
            /*IBlockState soil = world.getBlockState(pos.down());
            if (soil.getBlock() == Blocks.MYCELIUM) {
                return true;
            } else if (soil.getBlock() == Blocks.DIRT && soil.getValue(BlockDirt.VARIANT) == BlockDirt.DirtType.PODZOL) {
                return true;
            } else {
                return world.getLight(pos) < 13 && soil.getBlock().canSustainPlant(soil, world, pos.down(), EnumFacing.UP, this);
            }*/
        }
        return false;
    }
}
