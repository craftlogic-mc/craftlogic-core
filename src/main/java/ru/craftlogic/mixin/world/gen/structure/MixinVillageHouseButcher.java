package ru.craftlogic.mixin.world.gen.structure;

import net.minecraft.block.BlockStairs;
import net.minecraft.block.material.Material;
import net.minecraft.block.state.IBlockState;
import net.minecraft.init.Blocks;
import net.minecraft.util.EnumFacing;
import net.minecraft.world.World;
import net.minecraft.world.gen.structure.StructureBoundingBox;
import net.minecraft.world.gen.structure.StructureVillagePieces;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Overwrite;

import java.util.Random;

@Mixin(StructureVillagePieces.Hall.class)
public abstract class MixinVillageHouseButcher extends StructureVillagePieces.Village {
    @Overwrite
    public boolean addComponentParts(World world, Random rand, StructureBoundingBox bounding) {
        if (this.averageGroundLvl < 0) {
            this.averageGroundLvl = this.getAverageGroundLevel(world, bounding);
            if (this.averageGroundLvl < 0) {
                return true;
            }

            this.boundingBox.offset(0, this.averageGroundLvl - this.boundingBox.maxY + 7 - 1, 0);
        }

        IBlockState cobble = this.getBiomeSpecificBlockState(Blocks.COBBLESTONE.getDefaultState());
        IBlockState bricks = this.getBiomeSpecificBlockState(Blocks.STONEBRICK.getDefaultState());
        IBlockState nStairs = this.getBiomeSpecificBlockState(Blocks.OAK_STAIRS.getDefaultState().withProperty(BlockStairs.FACING, EnumFacing.NORTH));
        IBlockState sStairs = this.getBiomeSpecificBlockState(Blocks.OAK_STAIRS.getDefaultState().withProperty(BlockStairs.FACING, EnumFacing.SOUTH));
        IBlockState wStairs = this.getBiomeSpecificBlockState(Blocks.OAK_STAIRS.getDefaultState().withProperty(BlockStairs.FACING, EnumFacing.WEST));
        IBlockState planks = this.getBiomeSpecificBlockState(Blocks.PLANKS.getDefaultState());
        IBlockState log = this.getBiomeSpecificBlockState(Blocks.LOG.getDefaultState());
        IBlockState fence = this.getBiomeSpecificBlockState(Blocks.OAK_FENCE.getDefaultState());
        this.fillWithBlocks(world, bounding, 1, 1, 1, 7, 4, 4, Blocks.AIR.getDefaultState(), Blocks.AIR.getDefaultState(), false);
        this.fillWithBlocks(world, bounding, 2, 1, 6, 8, 4, 10, Blocks.AIR.getDefaultState(), Blocks.AIR.getDefaultState(), false);
        this.fillWithBlocks(world, bounding, 2, 0, 6, 8, 0, 10, Blocks.DIRT.getDefaultState(), Blocks.DIRT.getDefaultState(), false);
        this.setBlockState(world, bricks, 6, 0, 6, bounding);
        this.fillWithBlocks(world, bounding, 2, 1, 6, 2, 1, 10, fence, fence, false);
        this.fillWithBlocks(world, bounding, 8, 1, 6, 8, 1, 10, fence, fence, false);
        this.fillWithBlocks(world, bounding, 3, 1, 10, 7, 1, 10, fence, fence, false);
        this.fillWithBlocks(world, bounding, 1, 0, 1, 7, 0, 4, planks, planks, false);
        this.fillWithBlocks(world, bounding, 0, 0, 0, 0, 3, 5, bricks, bricks, false);
        this.fillWithBlocks(world, bounding, 8, 0, 0, 8, 3, 5, bricks, bricks, false);
        this.fillWithBlocks(world, bounding, 1, 0, 0, 7, 1, 0, bricks, bricks, false);
        this.fillWithBlocks(world, bounding, 1, 0, 5, 7, 1, 5, bricks, bricks, false);
        this.fillWithBlocks(world, bounding, 1, 2, 0, 7, 3, 0, planks, planks, false);
        this.fillWithBlocks(world, bounding, 1, 2, 5, 7, 3, 5, planks, planks, false);
        this.fillWithBlocks(world, bounding, 0, 4, 1, 8, 4, 1, planks, planks, false);
        this.fillWithBlocks(world, bounding, 0, 4, 4, 8, 4, 4, planks, planks, false);
        this.fillWithBlocks(world, bounding, 0, 5, 2, 8, 5, 3, planks, planks, false);
        this.setBlockState(world, planks, 0, 4, 2, bounding);
        this.setBlockState(world, planks, 0, 4, 3, bounding);
        this.setBlockState(world, planks, 8, 4, 2, bounding);
        this.setBlockState(world, planks, 8, 4, 3, bounding);

        for(int k = -1; k <= 2; ++k) {
            for(int l = 0; l <= 8; ++l) {
                this.setBlockState(world, nStairs, l, 4 + k, k, bounding);
                this.setBlockState(world, sStairs, l, 4 + k, 5 - k, bounding);
            }
        }

        this.setBlockState(world, log, 0, 2, 1, bounding);
        this.setBlockState(world, log, 0, 2, 4, bounding);
        this.setBlockState(world, log, 8, 2, 1, bounding);
        this.setBlockState(world, log, 8, 2, 4, bounding);
        this.setBlockState(world, Blocks.GLASS_PANE.getDefaultState(), 0, 2, 2, bounding);
        this.setBlockState(world, Blocks.GLASS_PANE.getDefaultState(), 0, 2, 3, bounding);
        this.setBlockState(world, Blocks.GLASS_PANE.getDefaultState(), 8, 2, 2, bounding);
        this.setBlockState(world, Blocks.GLASS_PANE.getDefaultState(), 8, 2, 3, bounding);
        this.setBlockState(world, Blocks.GLASS_PANE.getDefaultState(), 2, 2, 5, bounding);
        this.setBlockState(world, Blocks.GLASS_PANE.getDefaultState(), 3, 2, 5, bounding);
        this.setBlockState(world, Blocks.GLASS_PANE.getDefaultState(), 5, 2, 0, bounding);
        this.setBlockState(world, Blocks.GLASS_PANE.getDefaultState(), 6, 2, 5, bounding);
        this.setBlockState(world, fence, 2, 1, 3, bounding);
        this.setBlockState(world, Blocks.WOODEN_PRESSURE_PLATE.getDefaultState(), 2, 2, 3, bounding);
        this.setBlockState(world, planks, 1, 1, 4, bounding);
        this.setBlockState(world, nStairs, 2, 1, 4, bounding);
        this.setBlockState(world, wStairs, 1, 1, 3, bounding);
        this.fillWithBlocks(world, bounding, 5, 0, 1, 7, 0, 3, Blocks.DOUBLE_STONE_SLAB.getDefaultState(), Blocks.DOUBLE_STONE_SLAB.getDefaultState(), false);
        this.setBlockState(world, Blocks.DOUBLE_STONE_SLAB.getDefaultState(), 6, 1, 1, bounding);
        this.setBlockState(world, Blocks.DOUBLE_STONE_SLAB.getDefaultState(), 6, 1, 2, bounding);
        this.setBlockState(world, Blocks.AIR.getDefaultState(), 2, 1, 0, bounding);
        this.setBlockState(world, Blocks.AIR.getDefaultState(), 2, 2, 0, bounding);
        this.placeTorch(world, EnumFacing.NORTH, 2, 3, 1, bounding);
        this.createVillageDoor(world, bounding, rand, 2, 1, 0, EnumFacing.NORTH);
        if (this.getBlockStateFromPos(world, 2, 0, -1, bounding).getMaterial() == Material.AIR && this.getBlockStateFromPos(world, 2, -1, -1, bounding).getMaterial() != Material.AIR) {
            this.setBlockState(world, nStairs, 2, 0, -1, bounding);
            if (this.getBlockStateFromPos(world, 2, -1, -1, bounding).getBlock() == Blocks.GRASS_PATH) {
                this.setBlockState(world, Blocks.GRASS.getDefaultState(), 2, -1, -1, bounding);
            }
        }

        this.setBlockState(world, Blocks.AIR.getDefaultState(), 6, 1, 5, bounding);
        this.setBlockState(world, Blocks.AIR.getDefaultState(), 6, 2, 5, bounding);
        this.placeTorch(world, EnumFacing.SOUTH, 6, 3, 4, bounding);
        this.createVillageDoor(world, bounding, rand, 6, 1, 5, EnumFacing.SOUTH);

        for(int z = 0; z < 5; ++z) {
            for(int x = 0; x < 9; ++x) {
                this.clearCurrentPositionBlocksUpwards(world, x, 7, z, bounding);
                this.replaceAirAndLiquidDownwards(world, cobble, x, -1, z, bounding);
            }
        }

        this.spawnVillagers(world, bounding, 4, 1, 2, 2);
        return true;
    }
}
