package ru.craftlogic.mixin.block;

import net.minecraft.block.Block;
import net.minecraft.block.BlockStone;
import net.minecraft.block.material.Material;
import net.minecraft.block.properties.PropertyEnum;
import net.minecraft.block.state.IBlockState;
import net.minecraft.creativetab.CreativeTabs;
import net.minecraft.init.Blocks;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.NonNullList;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Overwrite;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import ru.craftlogic.CraftConfig;
import ru.craftlogic.api.CraftBlocks;

import java.util.Random;

@Mixin(BlockStone.class)
public abstract class MixinBlockStone extends Block {
    @Shadow @Final public static PropertyEnum<BlockStone.EnumType> VARIANT;

    public MixinBlockStone() {
        super(Material.ROCK);
    }

    @Inject(method = "<init>", at = @At("RETURN"))
    public void constructor(CallbackInfo info) {
        this.setHardness(8F);
        this.setResistance(50F);
    }

    /**
     * @author Radviger
     * @reason Rocks
     */
    @Overwrite
    public Item getItemDropped(IBlockState state, Random rand, int fortune) {
        if (CraftConfig.items.enableRocksDrop) {
            return Item.getItemFromBlock(CraftBlocks.ROCK);
        } else if (CraftConfig.tweaks.enableStoneUnification || state.getValue(VARIANT) == BlockStone.EnumType.STONE) {
            return Item.getItemFromBlock(Blocks.COBBLESTONE);
        } else {
            return Item.getItemFromBlock(this);
        }
    }

    @Override
    public int quantityDropped(Random random) {
        if (CraftConfig.items.enableRocksDrop) {
            return 1 + random.nextInt(4);
        } else {
            return super.quantityDropped(random);
        }
    }

    /**
     * @author Radviger
     * @reason Rocks
     */
    @Overwrite
    public int damageDropped(IBlockState state) {
        if (CraftConfig.items.enableRocksDrop) {
            return 0;
        } else {
            return super.damageDropped(state);
        }
    }

    @Override
    public void neighborChanged(IBlockState state, World world, BlockPos pos, Block neighborBlock, BlockPos neighborPos) {
        if (CraftConfig.tweaks.enableStoneCracking) {
            world.scheduleBlockUpdate(pos, this, 1, 0);
        }
    }

    private int getSupportLevel(World world, BlockPos pos, EnumFacing omit, boolean deep) {
        int support = 0;
        for (EnumFacing side : EnumFacing.values()) {
            if (side != omit) {
                if (world.isSideSolid(pos, side)) {
                    if (world.isSideSolid(pos, EnumFacing.DOWN) && world.isSideSolid(pos.offset(EnumFacing.DOWN), EnumFacing.UP)) {
                        support += 2;
                    } else if (deep) {
                        support += Math.max(0, getSupportLevel(world, pos.offset(side), side.getOpposite(), false) - 1);
                    }
                }
            }
        }
        return support;
    }

    @Override
    public void updateTick(World world, BlockPos pos, IBlockState state, Random random) {
        if (!world.isRemote && state.getBlock() == this && CraftConfig.tweaks.enableStoneCracking) {
            if (state.getValue(BlockStone.VARIANT) == BlockStone.EnumType.STONE
                    && !world.isSideSolid(pos.down(), EnumFacing.UP)) {
                int support = getSupportLevel(world, pos, null, true);
                if (support < 2 && random.nextInt(support + 1) == 0) {
                    world.setBlockState(pos, Blocks.COBBLESTONE.getDefaultState());
                }
            }
        }
    }

    /**
     * @author Radviger
     * @reason Stone types unification
     */
    @Overwrite
    public void getSubBlocks(CreativeTabs tab, NonNullList<ItemStack> blocks) {
        if (CraftConfig.tweaks.enableStoneUnification) {
            blocks.add(new ItemStack(this, 1, 0));
        } else {
            super.getSubBlocks(tab, blocks);
        }
    }
}
