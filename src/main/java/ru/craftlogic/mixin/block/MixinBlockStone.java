package ru.craftlogic.mixin.block;

import net.minecraft.block.Block;
import net.minecraft.block.BlockStone;
import net.minecraft.block.material.Material;
import net.minecraft.block.properties.PropertyEnum;
import net.minecraft.block.state.IBlockState;
import net.minecraft.init.Blocks;
import net.minecraft.item.Item;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Overwrite;
import org.spongepowered.asm.mixin.Shadow;
import ru.craftlogic.CraftConfig;
import ru.craftlogic.api.CraftItems;

import java.util.Random;

@Mixin(BlockStone.class)
public abstract class MixinBlockStone extends Block {
    @Shadow @Final public static PropertyEnum<BlockStone.EnumType> VARIANT;

    public MixinBlockStone() {
        super(Material.ROCK);
    }

    /**
     * @author Radviger
     * @reason Rocks
     */
    @Overwrite
    public Item getItemDropped(IBlockState state, Random rand, int fortune) {
        if (CraftConfig.items.enableRocks) {
            return CraftItems.ROCK;
        } else {
            return state.getValue(VARIANT) == BlockStone.EnumType.STONE ? Item.getItemFromBlock(Blocks.COBBLESTONE) : Item.getItemFromBlock(Blocks.STONE);
        }
    }

    @Override
    public int quantityDropped(Random random) {
        if (CraftConfig.items.enableRocks) {
            return 1 + random.nextInt(4);
        } else {
            return super.quantityDropped(random);
        }
    }

    @Override
    public void neighborChanged(IBlockState state, World world, BlockPos pos, Block neighborBlock, BlockPos neighborPos) {
        if (CraftConfig.tweaks.enableStoneCracking) {
            world.scheduleBlockUpdate(pos, this, 1, 0);
        }
    }

    @Override
    public void updateTick(World world, BlockPos pos, IBlockState state, Random random) {
        if (!world.isRemote && random.nextInt(5) == 0 && state.getBlock() == this && CraftConfig.tweaks.enableStoneCracking) {
            if (state.getValue(BlockStone.VARIANT) == BlockStone.EnumType.STONE
                    && !world.isSideSolid(pos.down(), EnumFacing.UP)) {
                world.setBlockState(pos, Blocks.COBBLESTONE.getDefaultState());
            }
        }
    }
}
