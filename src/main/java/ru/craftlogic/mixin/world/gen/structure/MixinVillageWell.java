package ru.craftlogic.mixin.world.gen.structure;

import net.minecraft.block.BlockStoneSlab;
import net.minecraft.block.state.IBlockState;
import net.minecraft.init.Blocks;
import net.minecraft.world.World;
import net.minecraft.world.gen.structure.StructureBoundingBox;
import net.minecraft.world.gen.structure.StructureVillagePieces;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Overwrite;

import java.util.Random;

@Mixin(StructureVillagePieces.Well.class)
public abstract class MixinVillageWell extends StructureVillagePieces.Village {
    @Overwrite
    public boolean addComponentParts(World world, Random rand, StructureBoundingBox bounding) {
        if (this.averageGroundLvl < 0) {
            this.averageGroundLvl = this.getAverageGroundLevel(world, bounding);
            if (this.averageGroundLvl < 0) {
                return true;
            }

            this.boundingBox.offset(0, this.averageGroundLvl - this.boundingBox.maxY + 3, 0);
        }

        IBlockState cobble = this.getBiomeSpecificBlockState(Blocks.COBBLESTONE.getDefaultState());
        IBlockState slab = this.getBiomeSpecificBlockState(Blocks.STONE_SLAB.getDefaultState().withProperty(BlockStoneSlab.VARIANT, BlockStoneSlab.EnumType.COBBLESTONE));
        IBlockState bricks = this.getBiomeSpecificBlockState(Blocks.STONEBRICK.getDefaultState());
        IBlockState fence = this.getBiomeSpecificBlockState(Blocks.COBBLESTONE_WALL.getDefaultState());
        this.fillWithBlocks(world, bounding, 1, 0, 1, 4, 12, 4, cobble, Blocks.FLOWING_WATER.getDefaultState(), false);
        this.setBlockState(world, Blocks.AIR.getDefaultState(), 2, 12, 2, bounding);
        this.setBlockState(world, Blocks.AIR.getDefaultState(), 3, 12, 2, bounding);
        this.setBlockState(world, Blocks.AIR.getDefaultState(), 2, 12, 3, bounding);
        this.setBlockState(world, Blocks.AIR.getDefaultState(), 3, 12, 3, bounding);
        this.setBlockState(world, fence, 1, 13, 1, bounding);
        this.setBlockState(world, fence, 1, 14, 1, bounding);

        this.setBlockState(world, slab, 2, 12, 1, bounding);
        this.setBlockState(world, slab, 3, 12, 1, bounding);
        this.setBlockState(world, slab, 2, 12, 4, bounding);
        this.setBlockState(world, slab, 3, 12, 4, bounding);
        this.setBlockState(world, slab, 1, 12, 2, bounding);
        this.setBlockState(world, slab, 1, 12, 3, bounding);
        this.setBlockState(world, slab, 4, 12, 2, bounding);
        this.setBlockState(world, slab, 4, 12, 3, bounding);


        this.setBlockState(world, fence, 4, 13, 1, bounding);
        this.setBlockState(world, fence, 4, 14, 1, bounding);
        this.setBlockState(world, fence, 1, 13, 4, bounding);
        this.setBlockState(world, fence, 1, 14, 4, bounding);
        this.setBlockState(world, fence, 4, 13, 4, bounding);
        this.setBlockState(world, fence, 4, 14, 4, bounding);
        this.fillWithBlocks(world, bounding, 1, 15, 1, 4, 15, 4, bricks, bricks, false);

        /*for(int i = 0; i <= 5; ++i) {
            for(int j = 0; j <= 5; ++j) {
                if (j == 0 || j == 5 || i == 0 || i == 5) {
                    this.setBlockState(world, cobble, j, 11, i, bounding);
                    this.clearCurrentPositionBlocksUpwards(world, j, 12, i, bounding);
                }
            }
        }*/

        return true;
    }
}
