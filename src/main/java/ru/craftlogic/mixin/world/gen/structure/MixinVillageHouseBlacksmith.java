package ru.craftlogic.mixin.world.gen.structure;

import net.minecraft.block.BlockStairs;
import net.minecraft.block.material.Material;
import net.minecraft.block.state.IBlockState;
import net.minecraft.init.Blocks;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;
import net.minecraft.world.gen.structure.StructureBoundingBox;
import net.minecraft.world.gen.structure.StructureVillagePieces;
import net.minecraft.world.storage.loot.LootTableList;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Overwrite;
import org.spongepowered.asm.mixin.Shadow;

import java.util.Random;

@Mixin(StructureVillagePieces.House2.class)
public abstract class MixinVillageHouseBlacksmith extends StructureVillagePieces.Village {
    @Shadow
    private boolean hasMadeChest;

    @Overwrite
    public boolean addComponentParts(World world, Random rand, StructureBoundingBox boundings) {
        if (this.averageGroundLvl < 0) {
            this.averageGroundLvl = this.getAverageGroundLevel(world, boundings);
            if (this.averageGroundLvl < 0) {
                return true;
            }

            this.boundingBox.offset(0, this.averageGroundLvl - this.boundingBox.maxY + 6 - 1, 0);
        }

        IBlockState cobble = Blocks.COBBLESTONE.getDefaultState();
        IBlockState bricks = Blocks.STONEBRICK.getDefaultState();
        IBlockState seat = this.getBiomeSpecificBlockState(Blocks.OAK_STAIRS.getDefaultState());
        IBlockState planks = this.getBiomeSpecificBlockState(Blocks.PLANKS.getDefaultState());
        IBlockState stairs = this.getBiomeSpecificBlockState(Blocks.STONE_BRICK_STAIRS.getDefaultState().withProperty(BlockStairs.FACING, EnumFacing.NORTH));
        IBlockState log = this.getBiomeSpecificBlockState(Blocks.LOG.getDefaultState());
        IBlockState fence = this.getBiomeSpecificBlockState(Blocks.COBBLESTONE_WALL.getDefaultState());
        this.fillWithBlocks(world, boundings, 0, 1, 0, 9, 4, 6, Blocks.AIR.getDefaultState(), Blocks.AIR.getDefaultState(), false);
        this.fillWithBlocks(world, boundings, 0, 0, 0, 9, 0, 6, bricks, bricks, false);
        this.fillWithBlocks(world, boundings, 0, 4, 0, 9, 4, 6, bricks, bricks, false);
        this.fillWithBlocks(world, boundings, 0, 5, 0, 9, 5, 6, Blocks.STONE_SLAB.getDefaultState(), Blocks.STONE_SLAB.getDefaultState(), false);
        this.fillWithBlocks(world, boundings, 1, 5, 1, 8, 5, 5, Blocks.AIR.getDefaultState(), Blocks.AIR.getDefaultState(), false);
        this.fillWithBlocks(world, boundings, 1, 1, 0, 2, 3, 0, planks, planks, false);
        this.fillWithBlocks(world, boundings, 0, 1, 0, 0, 4, 0, log, log, false);
        this.fillWithBlocks(world, boundings, 3, 1, 0, 3, 4, 0, log, log, false);
        this.fillWithBlocks(world, boundings, 0, 1, 6, 0, 4, 6, log, log, false);
        this.setBlockState(world, planks, 3, 3, 1, boundings);
        this.fillWithBlocks(world, boundings, 3, 1, 2, 3, 3, 2, planks, planks, false);
        this.fillWithBlocks(world, boundings, 4, 1, 3, 5, 3, 3, planks, planks, false);
        this.fillWithBlocks(world, boundings, 0, 1, 1, 0, 3, 5, planks, planks, false);
        this.fillWithBlocks(world, boundings, 1, 1, 6, 5, 3, 6, planks, planks, false);
        this.fillWithBlocks(world, boundings, 5, 1, 0, 5, 3, 0, fence, fence, false);
        this.fillWithBlocks(world, boundings, 9, 1, 0, 9, 3, 0, fence, fence, false);
        this.fillWithBlocks(world, boundings, 6, 1, 4, 9, 4, 6, bricks, bricks, false);
        this.setBlockState(world, Blocks.FLOWING_LAVA.getDefaultState(), 7, 1, 5, boundings);
        this.setBlockState(world, Blocks.FLOWING_LAVA.getDefaultState(), 8, 1, 5, boundings);
        this.setBlockState(world, Blocks.FURNACE.getDefaultState(), 7, 1, 4, boundings);
        this.setBlockState(world, Blocks.FURNACE.getDefaultState(), 8, 1, 4, boundings);
        this.setBlockState(world, Blocks.IRON_BARS.getDefaultState(), 9, 2, 5, boundings);
        this.setBlockState(world, Blocks.IRON_BARS.getDefaultState(), 9, 2, 4, boundings);
        this.fillWithBlocks(world, boundings, 7, 2, 4, 8, 2, 5, Blocks.AIR.getDefaultState(), Blocks.AIR.getDefaultState(), false);
        this.setBlockState(world, bricks, 6, 1, 3, boundings);
        this.setBlockState(world, bricks, 6, 2, 3, boundings);
        this.setBlockState(world, bricks, 6, 13, 3, boundings);
        this.setBlockState(world, Blocks.ANVIL.getDefaultState(), 8, 1, 1, boundings);
        this.setBlockState(world, Blocks.GLASS_PANE.getDefaultState(), 0, 2, 2, boundings);
        this.setBlockState(world, Blocks.GLASS_PANE.getDefaultState(), 0, 2, 4, boundings);
        this.setBlockState(world, Blocks.GLASS_PANE.getDefaultState(), 2, 2, 6, boundings);
        this.setBlockState(world, Blocks.GLASS_PANE.getDefaultState(), 4, 2, 6, boundings);
        this.setBlockState(world, Blocks.OAK_FENCE.getDefaultState(), 2, 1, 4, boundings);
        this.setBlockState(world, Blocks.WOODEN_PRESSURE_PLATE.getDefaultState(), 2, 2, 4, boundings);
        this.setBlockState(world, planks, 1, 1, 5, boundings);
        this.setBlockState(world, seat.withProperty(BlockStairs.FACING, EnumFacing.NORTH), 2, 1, 5, boundings);
        this.setBlockState(world, seat.withProperty(BlockStairs.FACING, EnumFacing.WEST), 1, 1, 4, boundings);
        if (!this.hasMadeChest && boundings.isVecInside(new BlockPos(this.getXWithOffset(5, 5), this.getYWithOffset(1), this.getZWithOffset(5, 5)))) {
            this.hasMadeChest = true;
            this.generateChest(world, boundings, rand, 5, 1, 5, LootTableList.CHESTS_VILLAGE_BLACKSMITH);
        }

        for(int k = 6; k <= 8; ++k) {
            if (this.getBlockStateFromPos(world, k, 0, -1, boundings).getMaterial() == Material.AIR && this.getBlockStateFromPos(world, k, -1, -1, boundings).getMaterial() != Material.AIR) {
                this.setBlockState(world, stairs, k, 0, -1, boundings);
                if (this.getBlockStateFromPos(world, k, -1, -1, boundings).getBlock() == Blocks.GRASS_PATH) {
                    this.setBlockState(world, Blocks.GRASS.getDefaultState(), k, -1, -1, boundings);
                }
            }
        }

        for(int k = 0; k < 7; ++k) {
            for(int j = 0; j < 10; ++j) {
                this.clearCurrentPositionBlocksUpwards(world, j, 6, k, boundings);
                this.replaceAirAndLiquidDownwards(world, cobble, j, -1, k, boundings);
            }
        }

        this.spawnVillagers(world, boundings, 7, 1, 1, 1);
        return true;
    }
}
