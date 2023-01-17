package ru.craftlogic.mixin.world.gen.structure;

import net.minecraft.block.*;
import net.minecraft.block.state.IBlockState;
import net.minecraft.init.Blocks;
import net.minecraft.util.EnumFacing;
import net.minecraft.world.World;
import net.minecraft.world.gen.structure.StructureBoundingBox;
import net.minecraft.world.gen.structure.StructureComponent;
import net.minecraft.world.gen.structure.StructureVillagePieces;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.event.terraingen.BiomeEvent.GetVillageBlockID;
import net.minecraftforge.fml.common.eventhandler.Event;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Overwrite;
import org.spongepowered.asm.mixin.Shadow;
import ru.craftlogic.CraftConfig;
import ru.craftlogic.common.block.BlockBurningTorch;

@Mixin(StructureVillagePieces.Village.class)
public abstract class MixinVillage extends StructureComponent {
    @Shadow protected boolean isZombieInfested;
    @Shadow protected int structureType;
    @Shadow(remap = false) protected StructureVillagePieces.Start startPiece;

    /**
     * @author Radviger
     * @reason Burning torches
     */
    @Overwrite
    protected void placeTorch(World world, EnumFacing side, int x, int y, int z, StructureBoundingBox bounds) {
        this.setBlockState(world,
            Blocks.TORCH.getDefaultState()
                .withProperty(BlockTorch.FACING, side)
                .withProperty(BlockBurningTorch.LIT, !this.isZombieInfested)
        , x, y, z, bounds);
    }

    /**
     * @author Radviger
     * @reason Falling cobblestone
     */
//    @Overwrite
//    protected IBlockState getBiomeSpecificBlockState(IBlockState state) {
//        GetVillageBlockID event = new GetVillageBlockID(this.startPiece == null ? null : this.startPiece.biome, state);
//        MinecraftForge.TERRAIN_GEN_BUS.post(event);
//        if (event.getResult() == Event.Result.DENY) {
//            return event.getReplacement();
//        } else {
//            if (this.structureType == 1) {
//                if (state.getBlock() == Blocks.LOG || state.getBlock() == Blocks.LOG2) {
//                    return Blocks.SANDSTONE.getDefaultState();
//                }
//
//                if (state.getBlock() == Blocks.COBBLESTONE) {
//                    return Blocks.SANDSTONE.getStateFromMeta(BlockSandStone.EnumType.DEFAULT.getMetadata());
//                }
//
//                if (state.getBlock() == Blocks.PLANKS) {
//                    return Blocks.SANDSTONE.getStateFromMeta(BlockSandStone.EnumType.SMOOTH.getMetadata());
//                }
//
//                if (state.getBlock() == Blocks.OAK_STAIRS) {
//                    return Blocks.SANDSTONE_STAIRS.getDefaultState().withProperty(BlockStairs.FACING, state.getValue(BlockStairs.FACING));
//                }
//
//                if (state.getBlock() == Blocks.STONE_STAIRS) {
//                    return Blocks.SANDSTONE_STAIRS.getDefaultState().withProperty(BlockStairs.FACING, state.getValue(BlockStairs.FACING));
//                }
//
//                if (state.getBlock() == Blocks.GRAVEL) {
//                    return Blocks.SANDSTONE.getDefaultState();
//                }
//            } else if (this.structureType == 3) {
//                if (state.getBlock() == Blocks.LOG || state.getBlock() == Blocks.LOG2) {
//                    return Blocks.LOG.getDefaultState().withProperty(BlockOldLog.VARIANT, net.minecraft.block.BlockPlanks.EnumType.SPRUCE).withProperty(BlockLog.LOG_AXIS, state.getValue(BlockLog.LOG_AXIS));
//                }
//
//                if (state.getBlock() == Blocks.PLANKS) {
//                    return Blocks.PLANKS.getDefaultState().withProperty(BlockPlanks.VARIANT, net.minecraft.block.BlockPlanks.EnumType.SPRUCE);
//                }
//
//                if (state.getBlock() == Blocks.OAK_STAIRS) {
//                    return Blocks.SPRUCE_STAIRS.getDefaultState().withProperty(BlockStairs.FACING, state.getValue(BlockStairs.FACING));
//                }
//
//                if (state.getBlock() == Blocks.OAK_FENCE) {
//                    return Blocks.SPRUCE_FENCE.getDefaultState();
//                }
//            } else if (this.structureType == 2) {
//                if (state.getBlock() == Blocks.LOG || state.getBlock() == Blocks.LOG2) {
//                    return Blocks.LOG2.getDefaultState().withProperty(BlockNewLog.VARIANT, net.minecraft.block.BlockPlanks.EnumType.ACACIA).withProperty(BlockLog.LOG_AXIS, state.getValue(BlockLog.LOG_AXIS));
//                }
//
//                if (state.getBlock() == Blocks.PLANKS) {
//                    return Blocks.PLANKS.getDefaultState().withProperty(BlockPlanks.VARIANT, net.minecraft.block.BlockPlanks.EnumType.ACACIA);
//                }
//
//                if (state.getBlock() == Blocks.OAK_STAIRS) {
//                    return Blocks.ACACIA_STAIRS.getDefaultState().withProperty(BlockStairs.FACING, state.getValue(BlockStairs.FACING));
//                }
//
//                if (state.getBlock() == Blocks.COBBLESTONE) {
//                    return Blocks.LOG2.getDefaultState().withProperty(BlockNewLog.VARIANT, net.minecraft.block.BlockPlanks.EnumType.ACACIA).withProperty(BlockLog.LOG_AXIS, BlockLog.EnumAxis.Y);
//                }
//
//                if (state.getBlock() == Blocks.OAK_FENCE) {
//                    return Blocks.ACACIA_FENCE.getDefaultState();
//                }
//            }
//
//            if (CraftConfig.tweaks.enableCobblestoneGravity) {
//                if (state.getBlock() == Blocks.COBBLESTONE) {
//                    return Blocks.STONEBRICK.getDefaultState();
//                } else if (state.getBlock() == Blocks.MOSSY_COBBLESTONE) {
//                    return Blocks.STONEBRICK.getDefaultState().withProperty(BlockStoneBrick.VARIANT, BlockStoneBrick.EnumType.MOSSY);
//                } else if (state.getBlock() == Blocks.STONE_STAIRS) {
//                    return Blocks.STONE_BRICK_STAIRS.getDefaultState().withProperty(BlockStairs.FACING, state.getValue(BlockStairs.FACING));
//                }
//            }
//
//            return state;
//        }
//    }
}
