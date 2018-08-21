package ru.craftlogic.mixin.world.gen.structure;

import net.minecraft.block.BlockLadder;
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

@Mixin(StructureVillagePieces.Church.class)
public abstract class MixinVillageHouseChurch extends StructureVillagePieces.Village {
    /**
     * @author Radviger
     * @reason Fallable cobblestone
     */
    @Overwrite
    public boolean addComponentParts(World world, Random rand, StructureBoundingBox bounding) {
        if (this.averageGroundLvl < 0) {
            this.averageGroundLvl = this.getAverageGroundLevel(world, bounding);
            if (this.averageGroundLvl < 0) {
                return true;
            }

            this.boundingBox.offset(0, this.averageGroundLvl - this.boundingBox.maxY + 12 - 1, 0);
        }

        IBlockState cobble = Blocks.COBBLESTONE.getDefaultState();
        IBlockState bricks = Blocks.STONEBRICK.getDefaultState();
        IBlockState nStairs = this.getBiomeSpecificBlockState(Blocks.STONE_BRICK_STAIRS.getDefaultState().withProperty(BlockStairs.FACING, EnumFacing.NORTH));
        IBlockState wStairs = this.getBiomeSpecificBlockState(Blocks.STONE_BRICK_STAIRS.getDefaultState().withProperty(BlockStairs.FACING, EnumFacing.WEST));
        IBlockState eStairs = this.getBiomeSpecificBlockState(Blocks.STONE_BRICK_STAIRS.getDefaultState().withProperty(BlockStairs.FACING, EnumFacing.EAST));
        this.fillWithBlocks(world, bounding, 1, 1, 1, 3, 3, 7, Blocks.AIR.getDefaultState(), Blocks.AIR.getDefaultState(), false);
        this.fillWithBlocks(world, bounding, 1, 5, 1, 3, 9, 3, Blocks.AIR.getDefaultState(), Blocks.AIR.getDefaultState(), false);
        this.fillWithBlocks(world, bounding, 1, 0, 0, 3, 0, 8, bricks, bricks, false);
        this.fillWithBlocks(world, bounding, 1, 1, 0, 3, 10, 0, bricks, bricks, false);
        this.fillWithBlocks(world, bounding, 0, 1, 1, 0, 10, 3, bricks, bricks, false);
        this.fillWithBlocks(world, bounding, 4, 1, 1, 4, 10, 3, bricks, bricks, false);
        this.fillWithBlocks(world, bounding, 0, 0, 4, 0, 4, 7, bricks, bricks, false);
        this.fillWithBlocks(world, bounding, 4, 0, 4, 4, 4, 7, bricks, bricks, false);
        this.fillWithBlocks(world, bounding, 1, 1, 8, 3, 4, 8, bricks, bricks, false);
        this.fillWithBlocks(world, bounding, 1, 5, 4, 3, 10, 4, bricks, bricks, false);
        this.fillWithBlocks(world, bounding, 1, 5, 5, 3, 5, 7, bricks, bricks, false);
        this.fillWithBlocks(world, bounding, 0, 9, 0, 4, 9, 4, bricks, bricks, false);
        this.fillWithBlocks(world, bounding, 0, 4, 0, 4, 4, 4, bricks, bricks, false);
        this.setBlockState(world, bricks, 0, 11, 2, bounding);
        this.setBlockState(world, bricks, 4, 11, 2, bounding);
        this.setBlockState(world, bricks, 2, 11, 0, bounding);
        this.setBlockState(world, bricks, 2, 11, 4, bounding);
        this.setBlockState(world, bricks, 1, 1, 6, bounding);
        this.setBlockState(world, bricks, 1, 1, 7, bounding);
        this.setBlockState(world, bricks, 2, 1, 7, bounding);
        this.setBlockState(world, bricks, 3, 1, 6, bounding);
        this.setBlockState(world, bricks, 3, 1, 7, bounding);
        this.setBlockState(world, nStairs, 1, 1, 5, bounding);
        this.setBlockState(world, nStairs, 2, 1, 6, bounding);
        this.setBlockState(world, nStairs, 3, 1, 5, bounding);
        this.setBlockState(world, wStairs, 1, 2, 7, bounding);
        this.setBlockState(world, eStairs, 3, 2, 7, bounding);
        this.setBlockState(world, Blocks.GLASS_PANE.getDefaultState(), 0, 2, 2, bounding);
        this.setBlockState(world, Blocks.GLASS_PANE.getDefaultState(), 0, 3, 2, bounding);
        this.setBlockState(world, Blocks.GLASS_PANE.getDefaultState(), 4, 2, 2, bounding);
        this.setBlockState(world, Blocks.GLASS_PANE.getDefaultState(), 4, 3, 2, bounding);
        this.setBlockState(world, Blocks.GLASS_PANE.getDefaultState(), 0, 6, 2, bounding);
        this.setBlockState(world, Blocks.GLASS_PANE.getDefaultState(), 0, 7, 2, bounding);
        this.setBlockState(world, Blocks.GLASS_PANE.getDefaultState(), 4, 6, 2, bounding);
        this.setBlockState(world, Blocks.GLASS_PANE.getDefaultState(), 4, 7, 2, bounding);
        this.setBlockState(world, Blocks.GLASS_PANE.getDefaultState(), 2, 6, 0, bounding);
        this.setBlockState(world, Blocks.GLASS_PANE.getDefaultState(), 2, 7, 0, bounding);
        this.setBlockState(world, Blocks.GLASS_PANE.getDefaultState(), 2, 6, 4, bounding);
        this.setBlockState(world, Blocks.GLASS_PANE.getDefaultState(), 2, 7, 4, bounding);
        this.setBlockState(world, Blocks.GLASS_PANE.getDefaultState(), 0, 3, 6, bounding);
        this.setBlockState(world, Blocks.GLASS_PANE.getDefaultState(), 4, 3, 6, bounding);
        this.setBlockState(world, Blocks.GLASS_PANE.getDefaultState(), 2, 3, 8, bounding);
        this.placeTorch(world, EnumFacing.SOUTH, 2, 4, 7, bounding);
        this.placeTorch(world, EnumFacing.EAST, 1, 4, 6, bounding);
        this.placeTorch(world, EnumFacing.WEST, 3, 4, 6, bounding);
        this.placeTorch(world, EnumFacing.NORTH, 2, 4, 5, bounding);
        IBlockState ladder = Blocks.LADDER.getDefaultState().withProperty(BlockLadder.FACING, EnumFacing.WEST);

        for(int k = 1; k <= 9; ++k) {
            this.setBlockState(world, ladder, 3, k, 3, bounding);
        }

        this.setBlockState(world, Blocks.AIR.getDefaultState(), 2, 1, 0, bounding);
        this.setBlockState(world, Blocks.AIR.getDefaultState(), 2, 2, 0, bounding);
        this.createVillageDoor(world, bounding, rand, 2, 1, 0, EnumFacing.NORTH);
        if (this.getBlockStateFromPos(world, 2, 0, -1, bounding).getMaterial() == Material.AIR && this.getBlockStateFromPos(world, 2, -1, -1, bounding).getMaterial() != Material.AIR) {
            this.setBlockState(world, nStairs, 2, 0, -1, bounding);
            if (this.getBlockStateFromPos(world, 2, -1, -1, bounding).getBlock() == Blocks.GRASS_PATH) {
                this.setBlockState(world, Blocks.GRASS.getDefaultState(), 2, -1, -1, bounding);
            }
        }

        for(int k = 0; k < 9; ++k) {
            for(int j = 0; j < 5; ++j) {
                this.clearCurrentPositionBlocksUpwards(world, j, 12, k, bounding);
                this.replaceAirAndLiquidDownwards(world, cobble, j, -1, k, bounding);
            }
        }

        this.spawnVillagers(world, bounding, 2, 1, 2, 1);
        return true;
    }
}
