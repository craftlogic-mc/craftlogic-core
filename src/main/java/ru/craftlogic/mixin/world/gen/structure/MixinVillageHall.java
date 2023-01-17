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

@Mixin(StructureVillagePieces.House3.class)
public abstract class MixinVillageHall extends StructureVillagePieces.Village {
    /**
     * @author Radviger
     * @reason Fallable cobblestone
     */
//    @Overwrite
//    public boolean addComponentParts(World world, Random rand, StructureBoundingBox bounding) {
//        if (this.averageGroundLvl < 0) {
//            this.averageGroundLvl = this.getAverageGroundLevel(world, bounding);
//            if (this.averageGroundLvl < 0) {
//                return true;
//            }
//
//            this.boundingBox.offset(0, this.averageGroundLvl - this.boundingBox.maxY + 7 - 1, 0);
//        }
//
//        IBlockState cobble = this.getBiomeSpecificBlockState(Blocks.COBBLESTONE.getDefaultState());
//        IBlockState bricks = this.getBiomeSpecificBlockState(Blocks.STONEBRICK.getDefaultState());
//        IBlockState nStairs = this.getBiomeSpecificBlockState(Blocks.OAK_STAIRS.getDefaultState().withProperty(BlockStairs.FACING, EnumFacing.NORTH));
//        IBlockState sStairs = this.getBiomeSpecificBlockState(Blocks.OAK_STAIRS.getDefaultState().withProperty(BlockStairs.FACING, EnumFacing.SOUTH));
//        IBlockState eStairs = this.getBiomeSpecificBlockState(Blocks.OAK_STAIRS.getDefaultState().withProperty(BlockStairs.FACING, EnumFacing.EAST));
//        IBlockState wStairs = this.getBiomeSpecificBlockState(Blocks.OAK_STAIRS.getDefaultState().withProperty(BlockStairs.FACING, EnumFacing.WEST));
//        IBlockState planks = this.getBiomeSpecificBlockState(Blocks.PLANKS.getDefaultState());
//        IBlockState log = this.getBiomeSpecificBlockState(Blocks.LOG.getDefaultState());
//        this.fillWithBlocks(world, bounding, 1, 1, 1, 7, 4, 4, Blocks.AIR.getDefaultState(), Blocks.AIR.getDefaultState(), false);
//        this.fillWithBlocks(world, bounding, 2, 1, 6, 8, 4, 10, Blocks.AIR.getDefaultState(), Blocks.AIR.getDefaultState(), false);
//        this.fillWithBlocks(world, bounding, 2, 0, 5, 8, 0, 10, planks, planks, false);
//        this.fillWithBlocks(world, bounding, 1, 0, 1, 7, 0, 4, planks, planks, false);
//        this.fillWithBlocks(world, bounding, 0, 0, 0, 0, 3, 5, bricks, bricks, false);
//        this.fillWithBlocks(world, bounding, 8, 0, 0, 8, 3, 10, bricks, bricks, false);
//        this.fillWithBlocks(world, bounding, 1, 0, 0, 7, 2, 0, bricks, bricks, false);
//        this.fillWithBlocks(world, bounding, 1, 0, 5, 2, 1, 5, bricks, bricks, false);
//        this.fillWithBlocks(world, bounding, 2, 0, 6, 2, 3, 10, bricks, bricks, false);
//        this.fillWithBlocks(world, bounding, 3, 0, 10, 7, 3, 10, bricks, bricks, false);
//        this.fillWithBlocks(world, bounding, 1, 2, 0, 7, 3, 0, planks, planks, false);
//        this.fillWithBlocks(world, bounding, 1, 2, 5, 2, 3, 5, planks, planks, false);
//        this.fillWithBlocks(world, bounding, 0, 4, 1, 8, 4, 1, planks, planks, false);
//        this.fillWithBlocks(world, bounding, 0, 4, 4, 3, 4, 4, planks, planks, false);
//        this.fillWithBlocks(world, bounding, 0, 5, 2, 8, 5, 3, planks, planks, false);
//        this.setBlockState(world, planks, 0, 4, 2, bounding);
//        this.setBlockState(world, planks, 0, 4, 3, bounding);
//        this.setBlockState(world, planks, 8, 4, 2, bounding);
//        this.setBlockState(world, planks, 8, 4, 3, bounding);
//        this.setBlockState(world, planks, 8, 4, 4, bounding);
//
//        for(int j1 = -1; j1 <= 2; ++j1) {
//            for(int j2 = 0; j2 <= 8; ++j2) {
//                this.setBlockState(world, nStairs, j2, 4 + j1, j1, bounding);
//                if ((j1 > -1 || j2 <= 1) && (j1 > 0 || j2 <= 3) && (j1 > 1 || j2 <= 4 || j2 >= 6)) {
//                    this.setBlockState(world, sStairs, j2, 4 + j1, 5 - j1, bounding);
//                }
//            }
//        }
//
//        this.fillWithBlocks(world, bounding, 3, 4, 5, 3, 4, 10, planks, planks, false);
//        this.fillWithBlocks(world, bounding, 7, 4, 2, 7, 4, 10, planks, planks, false);
//        this.fillWithBlocks(world, bounding, 4, 5, 4, 4, 5, 10, planks, planks, false);
//        this.fillWithBlocks(world, bounding, 6, 5, 4, 6, 5, 10, planks, planks, false);
//        this.fillWithBlocks(world, bounding, 5, 6, 3, 5, 6, 10, planks, planks, false);
//
//        for(int j1 = 4; j1 >= 1; --j1) {
//            this.setBlockState(world, planks, j1, 2 + j1, 7 - j1, bounding);
//
//            for(int j2 = 8 - j1; j2 <= 10; ++j2) {
//                this.setBlockState(world, eStairs, j1, 2 + j1, j2, bounding);
//            }
//        }
//
//        this.setBlockState(world, planks, 6, 6, 3, bounding);
//        this.setBlockState(world, planks, 7, 5, 4, bounding);
//        this.setBlockState(world, wStairs, 6, 6, 4, bounding);
//
//        for(int x = 6; x <= 8; ++x) {
//            for(int z = 5; z <= 10; ++z) {
//                this.setBlockState(world, wStairs, x, 12 - x, z, bounding);
//            }
//        }
//
//        this.setBlockState(world, log, 0, 2, 1, bounding);
//        this.setBlockState(world, log, 0, 2, 4, bounding);
//        this.setBlockState(world, Blocks.GLASS_PANE.getDefaultState(), 0, 2, 2, bounding);
//        this.setBlockState(world, Blocks.GLASS_PANE.getDefaultState(), 0, 2, 3, bounding);
//        this.setBlockState(world, log, 4, 2, 0, bounding);
//        this.setBlockState(world, Blocks.GLASS_PANE.getDefaultState(), 5, 2, 0, bounding);
//        this.setBlockState(world, log, 6, 2, 0, bounding);
//        this.setBlockState(world, log, 8, 2, 1, bounding);
//        this.setBlockState(world, Blocks.GLASS_PANE.getDefaultState(), 8, 2, 2, bounding);
//        this.setBlockState(world, Blocks.GLASS_PANE.getDefaultState(), 8, 2, 3, bounding);
//        this.setBlockState(world, log, 8, 2, 4, bounding);
//        this.setBlockState(world, planks, 8, 2, 5, bounding);
//        this.setBlockState(world, log, 8, 2, 6, bounding);
//        this.setBlockState(world, Blocks.GLASS_PANE.getDefaultState(), 8, 2, 7, bounding);
//        this.setBlockState(world, Blocks.GLASS_PANE.getDefaultState(), 8, 2, 8, bounding);
//        this.setBlockState(world, log, 8, 2, 9, bounding);
//        this.setBlockState(world, log, 2, 2, 6, bounding);
//        this.setBlockState(world, Blocks.GLASS_PANE.getDefaultState(), 2, 2, 7, bounding);
//        this.setBlockState(world, Blocks.GLASS_PANE.getDefaultState(), 2, 2, 8, bounding);
//        this.setBlockState(world, log, 2, 2, 9, bounding);
//        this.setBlockState(world, log, 4, 4, 10, bounding);
//        this.setBlockState(world, Blocks.GLASS_PANE.getDefaultState(), 5, 4, 10, bounding);
//        this.setBlockState(world, log, 6, 4, 10, bounding);
//        this.setBlockState(world, planks, 5, 5, 10, bounding);
//        this.setBlockState(world, Blocks.AIR.getDefaultState(), 2, 1, 0, bounding);
//        this.setBlockState(world, Blocks.AIR.getDefaultState(), 2, 2, 0, bounding);
//        this.placeTorch(world, EnumFacing.NORTH, 2, 3, 1, bounding);
//        this.createVillageDoor(world, bounding, rand, 2, 1, 0, EnumFacing.NORTH);
//        this.fillWithBlocks(world, bounding, 1, 0, -1, 3, 2, -1, Blocks.AIR.getDefaultState(), Blocks.AIR.getDefaultState(), false);
//        if (this.getBlockStateFromPos(world, 2, 0, -1, bounding).getMaterial() == Material.AIR && this.getBlockStateFromPos(world, 2, -1, -1, bounding).getMaterial() != Material.AIR) {
//            this.setBlockState(world, nStairs, 2, 0, -1, bounding);
//            if (this.getBlockStateFromPos(world, 2, -1, -1, bounding).getBlock() == Blocks.GRASS_PATH) {
//                this.setBlockState(world, Blocks.GRASS.getDefaultState(), 2, -1, -1, bounding);
//            }
//        }
//
//        for(int z = 0; z < 5; ++z) {
//            for(int x = 0; x < 9; ++x) {
//                this.clearCurrentPositionBlocksUpwards(world, x, 7, z, bounding);
//                this.replaceAirAndLiquidDownwards(world, cobble, x, -1, z, bounding);
//            }
//        }
//
//        for(int z = 5; z < 11; ++z) {
//            for(int x = 2; x < 9; ++x) {
//                this.clearCurrentPositionBlocksUpwards(world, x, 7, z, bounding);
//                this.replaceAirAndLiquidDownwards(world, cobble, x, -1, z, bounding);
//            }
//        }
//
//        this.spawnVillagers(world, bounding, 4, 1, 2, 2);
//        return true;
//    }
}
