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
import org.spongepowered.asm.mixin.Shadow;

import java.util.Random;

@Mixin(StructureVillagePieces.House4Garden.class)
public abstract class MixinVillageHouseGardener extends StructureVillagePieces.Village {
    @Shadow
    private boolean isRoofAccessible;

    @Overwrite
    public boolean addComponentParts(World world, Random rand, StructureBoundingBox boundings) {
        if (this.averageGroundLvl < 0) {
            this.averageGroundLvl = this.getAverageGroundLevel(world, boundings);
            if (this.averageGroundLvl < 0) {
                return true;
            }

            this.boundingBox.offset(0, this.averageGroundLvl - this.boundingBox.maxY + 6 - 1, 0);
        }

        IBlockState cobble = this.getBiomeSpecificBlockState(Blocks.COBBLESTONE.getDefaultState());
        IBlockState planks = this.getBiomeSpecificBlockState(Blocks.PLANKS.getDefaultState());
        IBlockState stairs = this.getBiomeSpecificBlockState(Blocks.STONE_STAIRS.getDefaultState().withProperty(BlockStairs.FACING, EnumFacing.NORTH));
        IBlockState log = this.getBiomeSpecificBlockState(Blocks.LOG.getDefaultState());
        IBlockState fence = this.getBiomeSpecificBlockState(Blocks.OAK_FENCE.getDefaultState());
        this.fillWithBlocks(world, boundings, 0, 0, 0, 4, 0, 4, cobble, cobble, false);
        this.fillWithBlocks(world, boundings, 0, 4, 0, 4, 4, 4, log, log, false);
        this.fillWithBlocks(world, boundings, 1, 4, 1, 3, 4, 3, planks, planks, false);
        this.setBlockState(world, cobble, 0, 1, 0, boundings);
        this.setBlockState(world, cobble, 0, 2, 0, boundings);
        this.setBlockState(world, cobble, 0, 3, 0, boundings);
        this.setBlockState(world, cobble, 4, 1, 0, boundings);
        this.setBlockState(world, cobble, 4, 2, 0, boundings);
        this.setBlockState(world, cobble, 4, 3, 0, boundings);
        this.setBlockState(world, cobble, 0, 1, 4, boundings);
        this.setBlockState(world, cobble, 0, 2, 4, boundings);
        this.setBlockState(world, cobble, 0, 3, 4, boundings);
        this.setBlockState(world, cobble, 4, 1, 4, boundings);
        this.setBlockState(world, cobble, 4, 2, 4, boundings);
        this.setBlockState(world, cobble, 4, 3, 4, boundings);
        this.fillWithBlocks(world, boundings, 0, 1, 1, 0, 3, 3, planks, planks, false);
        this.fillWithBlocks(world, boundings, 4, 1, 1, 4, 3, 3, planks, planks, false);
        this.fillWithBlocks(world, boundings, 1, 1, 4, 3, 3, 4, planks, planks, false);
        this.setBlockState(world, Blocks.GLASS_PANE.getDefaultState(), 0, 2, 2, boundings);
        this.setBlockState(world, Blocks.GLASS_PANE.getDefaultState(), 2, 2, 4, boundings);
        this.setBlockState(world, Blocks.GLASS_PANE.getDefaultState(), 4, 2, 2, boundings);
        this.setBlockState(world, planks, 1, 1, 0, boundings);
        this.setBlockState(world, planks, 1, 2, 0, boundings);
        this.setBlockState(world, planks, 1, 3, 0, boundings);
        this.setBlockState(world, planks, 2, 3, 0, boundings);
        this.setBlockState(world, planks, 3, 3, 0, boundings);
        this.setBlockState(world, planks, 3, 2, 0, boundings);
        this.setBlockState(world, planks, 3, 1, 0, boundings);
        if (this.getBlockStateFromPos(world, 2, 0, -1, boundings).getMaterial() == Material.AIR && this.getBlockStateFromPos(world, 2, -1, -1, boundings).getMaterial() != Material.AIR) {
            this.setBlockState(world, stairs, 2, 0, -1, boundings);
            if (this.getBlockStateFromPos(world, 2, -1, -1, boundings).getBlock() == Blocks.GRASS_PATH) {
                this.setBlockState(world, Blocks.GRASS.getDefaultState(), 2, -1, -1, boundings);
            }
        }

        this.fillWithBlocks(world, boundings, 1, 1, 1, 3, 3, 3, Blocks.AIR.getDefaultState(), Blocks.AIR.getDefaultState(), false);
        if (this.isRoofAccessible) {
            this.setBlockState(world, fence, 0, 5, 0, boundings);
            this.setBlockState(world, fence, 1, 5, 0, boundings);
            this.setBlockState(world, fence, 2, 5, 0, boundings);
            this.setBlockState(world, fence, 3, 5, 0, boundings);
            this.setBlockState(world, fence, 4, 5, 0, boundings);
            this.setBlockState(world, fence, 0, 5, 4, boundings);
            this.setBlockState(world, fence, 1, 5, 4, boundings);
            this.setBlockState(world, fence, 2, 5, 4, boundings);
            this.setBlockState(world, fence, 3, 5, 4, boundings);
            this.setBlockState(world, fence, 4, 5, 4, boundings);
            this.setBlockState(world, fence, 4, 5, 1, boundings);
            this.setBlockState(world, fence, 4, 5, 2, boundings);
            this.setBlockState(world, fence, 4, 5, 3, boundings);
            this.setBlockState(world, fence, 0, 5, 1, boundings);
            this.setBlockState(world, fence, 0, 5, 2, boundings);
            this.setBlockState(world, fence, 0, 5, 3, boundings);
        }

        if (this.isRoofAccessible) {
            IBlockState ladder = Blocks.LADDER.getDefaultState().withProperty(BlockLadder.FACING, EnumFacing.SOUTH);
            this.setBlockState(world, ladder, 3, 1, 3, boundings);
            this.setBlockState(world, ladder, 3, 2, 3, boundings);
            this.setBlockState(world, ladder, 3, 3, 3, boundings);
            this.setBlockState(world, ladder, 3, 4, 3, boundings);
        }

        this.placeTorch(world, EnumFacing.NORTH, 2, 3, 1, boundings);

        for(int j = 0; j < 5; ++j) {
            for(int i = 0; i < 5; ++i) {
                this.clearCurrentPositionBlocksUpwards(world, i, 6, j, boundings);
                this.replaceAirAndLiquidDownwards(world, cobble, i, -1, j, boundings);
            }
        }

        this.spawnVillagers(world, boundings, 1, 1, 2, 1);
        return true;
    }
}
