package ru.craftlogic.mixin.world.gen.structure;

import net.minecraft.block.Block;
import net.minecraft.block.BlockStairs;
import net.minecraft.block.BlockStoneBrick;
import net.minecraft.block.state.IBlockState;
import net.minecraft.init.Blocks;
import net.minecraft.world.World;
import net.minecraft.world.gen.structure.ComponentScatteredFeaturePieces;
import net.minecraft.world.gen.structure.StructureBoundingBox;
import net.minecraft.world.gen.structure.StructureComponent;
import org.spongepowered.asm.mixin.Mixin;

@Mixin(ComponentScatteredFeaturePieces.JunglePyramid.class)
public abstract class MixinJungleTemple extends StructureComponent {
    @Override
    protected void setBlockState(World world, IBlockState state, int x, int y, int z, StructureBoundingBox bounding) {
        Block block = state.getBlock();
        if (block == Blocks.COBBLESTONE) {
            state = Blocks.STONEBRICK.getDefaultState();
        } else if (block == Blocks.MOSSY_COBBLESTONE) {
            state = Blocks.STONEBRICK.getDefaultState().withProperty(BlockStoneBrick.VARIANT, BlockStoneBrick.EnumType.MOSSY);
        } else if (block == Blocks.STONE_STAIRS) {
            state = Blocks.STONE_BRICK_STAIRS.getDefaultState()
                    .withProperty(BlockStairs.FACING, state.getValue(BlockStairs.FACING))
                    .withProperty(BlockStairs.HALF, state.getValue(BlockStairs.HALF));
        }
        super.setBlockState(world, state, x, y, z, bounding);
    }
}
