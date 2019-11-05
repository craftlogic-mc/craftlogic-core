package ru.craftlogic.mixin.block;

import net.minecraft.block.BlockBush;
import net.minecraft.block.BlockTallGrass;
import net.minecraft.block.state.IBlockState;
import net.minecraft.init.Blocks;
import net.minecraft.init.Items;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.NonNullList;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.IBlockAccess;
import net.minecraft.world.World;
import net.minecraft.world.biome.Biome;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Overwrite;
import ru.craftlogic.api.CraftItems;
import ru.craftlogic.api.world.Location;

import javax.annotation.Nonnull;
import java.util.Random;

@Mixin(BlockTallGrass.class)
public class MixinBlockTallGrass extends BlockBush {
    @Override
    public void updateTick(World world, BlockPos pos, IBlockState state, Random rand) {
        super.updateTick(world, pos, state, rand);
        Location location = new Location(world, pos);
        if (world.isRemote && location.isAreaLoaded(3)) {
            Location up = location.offset(EnumFacing.UP);
            if (up.getLightFromNeighbors() < 4 && up.getBlockLightOpacity() > 2) {
                location.setBlockToAir();
            } else {
                if (up.getLightFromNeighbors() >= 9) {
                    for (int i = 0; i < 4; ++i) {
                        Location r = location.randomize(rand, 3);
                        Biome biome = r.getBiome();
                        if (rand.nextInt(100 + (biome.getEnableSnow() ? 200 : 0)) == 0) {
                            Location rup = r.offset(EnumFacing.UP);
                            Location rdown = r.offset(EnumFacing.DOWN);

                            if (r.isHeightValid() && r.isBlockLoaded() && rdown.isSameBlock(Blocks.GRASS)) {
                                if (rup.getLightFromNeighbors() >= 4 && rup.getBlockLightOpacity() <= 2 && r.canBlockBePlaced(Blocks.TALLGRASS)) {
                                    r.setBlockState(state);
                                }
                            }
                        }
                    }
                }
            }
        }
    }

    /**
     * @author Radviger
     * @reason Grass drop
     */
    @Overwrite
    public Item getItemDropped(IBlockState state, Random random, int fortune) {
        return Items.AIR;
    }

    /**
     * @author Radviger
     * @reason No seeds drop from grass
     */
    @Overwrite(remap = false)
    public void getDrops(NonNullList<ItemStack> drops, IBlockAccess blockAccessor, BlockPos pos, IBlockState state, int fortune) {}

    /**
     * @author Radviger
     * @reason Grass drop
     */
    @Nonnull
    @Overwrite(remap = false)
    public NonNullList<ItemStack> onSheared(@Nonnull ItemStack item, IBlockAccess world, BlockPos pos, int fortune) {
        return NonNullList.withSize(1, new ItemStack(CraftItems.GRASS));
    }
}
