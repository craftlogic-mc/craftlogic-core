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

@Mixin(StructureVillagePieces.House1.class)
public abstract class MixinVillageHouseLibrary extends StructureVillagePieces.Village {
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

            this.boundingBox.offset(0, this.averageGroundLvl - this.boundingBox.maxY + 9 - 1, 0);
        }

        IBlockState cobble = this.getBiomeSpecificBlockState(Blocks.COBBLESTONE.getDefaultState());
        IBlockState bricks = this.getBiomeSpecificBlockState(Blocks.STONEBRICK.getDefaultState());
        IBlockState nStairs = this.getBiomeSpecificBlockState(Blocks.OAK_STAIRS.getDefaultState().withProperty(BlockStairs.FACING, EnumFacing.NORTH));
        IBlockState sStairs = this.getBiomeSpecificBlockState(Blocks.OAK_STAIRS.getDefaultState().withProperty(BlockStairs.FACING, EnumFacing.SOUTH));
        IBlockState eStairs = this.getBiomeSpecificBlockState(Blocks.OAK_STAIRS.getDefaultState().withProperty(BlockStairs.FACING, EnumFacing.EAST));
        IBlockState planks = this.getBiomeSpecificBlockState(Blocks.PLANKS.getDefaultState());
        IBlockState stairs = this.getBiomeSpecificBlockState(Blocks.STONE_BRICK_STAIRS.getDefaultState().withProperty(BlockStairs.FACING, EnumFacing.NORTH));
        IBlockState fence = this.getBiomeSpecificBlockState(Blocks.OAK_FENCE.getDefaultState());
        this.fillWithBlocks(world, bounding, 1, 1, 1, 7, 5, 4, Blocks.AIR.getDefaultState(), Blocks.AIR.getDefaultState(), false);
        this.fillWithBlocks(world, bounding, 0, 0, 0, 8, 0, 5, bricks, bricks, false);
        this.fillWithBlocks(world, bounding, 0, 5, 0, 8, 5, 5, bricks, bricks, false);
        this.fillWithBlocks(world, bounding, 0, 6, 1, 8, 6, 4, bricks, bricks, false);
        this.fillWithBlocks(world, bounding, 0, 7, 2, 8, 7, 3, bricks, bricks, false);

        for(int l = -1; l <= 2; ++l) {
            for(int k = 0; k <= 8; ++k) {
                this.setBlockState(world, nStairs, k, 6 + l, l, bounding);
                this.setBlockState(world, sStairs, k, 6 + l, 5 - l, bounding);
            }
        }

        this.fillWithBlocks(world, bounding, 0, 1, 0, 0, 1, 5, bricks, bricks, false);
        this.fillWithBlocks(world, bounding, 1, 1, 5, 8, 1, 5, bricks, bricks, false);
        this.fillWithBlocks(world, bounding, 8, 1, 0, 8, 1, 4, bricks, bricks, false);
        this.fillWithBlocks(world, bounding, 2, 1, 0, 7, 1, 0, bricks, bricks, false);
        this.fillWithBlocks(world, bounding, 0, 2, 0, 0, 4, 0, bricks, bricks, false);
        this.fillWithBlocks(world, bounding, 0, 2, 5, 0, 4, 5, bricks, bricks, false);
        this.fillWithBlocks(world, bounding, 8, 2, 5, 8, 4, 5, bricks, bricks, false);
        this.fillWithBlocks(world, bounding, 8, 2, 0, 8, 4, 0, bricks, bricks, false);
        this.fillWithBlocks(world, bounding, 0, 2, 1, 0, 4, 4, planks, planks, false);
        this.fillWithBlocks(world, bounding, 1, 2, 5, 7, 4, 5, planks, planks, false);
        this.fillWithBlocks(world, bounding, 8, 2, 1, 8, 4, 4, planks, planks, false);
        this.fillWithBlocks(world, bounding, 1, 2, 0, 7, 4, 0, planks, planks, false);
        this.setBlockState(world, Blocks.GLASS_PANE.getDefaultState(), 4, 2, 0, bounding);
        this.setBlockState(world, Blocks.GLASS_PANE.getDefaultState(), 5, 2, 0, bounding);
        this.setBlockState(world, Blocks.GLASS_PANE.getDefaultState(), 6, 2, 0, bounding);
        this.setBlockState(world, Blocks.GLASS_PANE.getDefaultState(), 4, 3, 0, bounding);
        this.setBlockState(world, Blocks.GLASS_PANE.getDefaultState(), 5, 3, 0, bounding);
        this.setBlockState(world, Blocks.GLASS_PANE.getDefaultState(), 6, 3, 0, bounding);
        this.setBlockState(world, Blocks.GLASS_PANE.getDefaultState(), 0, 2, 2, bounding);
        this.setBlockState(world, Blocks.GLASS_PANE.getDefaultState(), 0, 2, 3, bounding);
        this.setBlockState(world, Blocks.GLASS_PANE.getDefaultState(), 0, 3, 2, bounding);
        this.setBlockState(world, Blocks.GLASS_PANE.getDefaultState(), 0, 3, 3, bounding);
        this.setBlockState(world, Blocks.GLASS_PANE.getDefaultState(), 8, 2, 2, bounding);
        this.setBlockState(world, Blocks.GLASS_PANE.getDefaultState(), 8, 2, 3, bounding);
        this.setBlockState(world, Blocks.GLASS_PANE.getDefaultState(), 8, 3, 2, bounding);
        this.setBlockState(world, Blocks.GLASS_PANE.getDefaultState(), 8, 3, 3, bounding);
        this.setBlockState(world, Blocks.GLASS_PANE.getDefaultState(), 2, 2, 5, bounding);
        this.setBlockState(world, Blocks.GLASS_PANE.getDefaultState(), 3, 2, 5, bounding);
        this.setBlockState(world, Blocks.GLASS_PANE.getDefaultState(), 5, 2, 5, bounding);
        this.setBlockState(world, Blocks.GLASS_PANE.getDefaultState(), 6, 2, 5, bounding);
        this.fillWithBlocks(world, bounding, 1, 4, 1, 7, 4, 1, planks, planks, false);
        this.fillWithBlocks(world, bounding, 1, 4, 4, 7, 4, 4, planks, planks, false);
        this.fillWithBlocks(world, bounding, 1, 3, 4, 7, 3, 4, Blocks.BOOKSHELF.getDefaultState(), Blocks.BOOKSHELF.getDefaultState(), false);
        this.setBlockState(world, planks, 7, 1, 4, bounding);
        this.setBlockState(world, eStairs, 7, 1, 3, bounding);
        this.setBlockState(world, nStairs, 6, 1, 4, bounding);
        this.setBlockState(world, nStairs, 5, 1, 4, bounding);
        this.setBlockState(world, nStairs, 4, 1, 4, bounding);
        this.setBlockState(world, nStairs, 3, 1, 4, bounding);
        this.setBlockState(world, fence, 6, 1, 3, bounding);
        this.setBlockState(world, Blocks.WOODEN_PRESSURE_PLATE.getDefaultState(), 6, 2, 3, bounding);
        this.setBlockState(world, fence, 4, 1, 3, bounding);
        this.setBlockState(world, Blocks.WOODEN_PRESSURE_PLATE.getDefaultState(), 4, 2, 3, bounding);
        this.setBlockState(world, Blocks.CRAFTING_TABLE.getDefaultState(), 7, 1, 1, bounding);
        this.setBlockState(world, Blocks.AIR.getDefaultState(), 1, 1, 0, bounding);
        this.setBlockState(world, Blocks.AIR.getDefaultState(), 1, 2, 0, bounding);
        this.createVillageDoor(world, bounding, rand, 1, 1, 0, EnumFacing.NORTH);
        if (this.getBlockStateFromPos(world, 1, 0, -1, bounding).getMaterial() == Material.AIR && this.getBlockStateFromPos(world, 1, -1, -1, bounding).getMaterial() != Material.AIR) {
            this.setBlockState(world, stairs, 1, 0, -1, bounding);
            if (this.getBlockStateFromPos(world, 1, -1, -1, bounding).getBlock() == Blocks.GRASS_PATH) {
                this.setBlockState(world, Blocks.GRASS.getDefaultState(), 1, -1, -1, bounding);
            }
        }

        for(int l = 0; l < 6; ++l) {
            for(int k = 0; k < 9; ++k) {
                this.clearCurrentPositionBlocksUpwards(world, k, 9, l, bounding);
                this.replaceAirAndLiquidDownwards(world, cobble, k, -1, l, bounding);
            }
        }

        this.spawnVillagers(world, bounding, 2, 1, 2, 1);
        return true;
    }
}
