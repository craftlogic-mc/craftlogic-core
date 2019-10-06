package ru.craftlogic.mixin.block;

import net.minecraft.block.*;
import net.minecraft.block.properties.PropertyEnum;
import net.minecraft.block.state.IBlockState;
import net.minecraft.creativetab.CreativeTabs;
import net.minecraft.init.Blocks;
import net.minecraft.item.ItemStack;
import net.minecraft.util.NonNullList;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;
import net.minecraft.world.gen.feature.*;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Overwrite;
import org.spongepowered.asm.mixin.Shadow;
import ru.craftlogic.api.CraftBlocks;
import ru.craftlogic.common.block.BlockPlanks2;

import java.util.Random;

@Mixin(BlockSapling.class)
public abstract class MixinBlockSapling extends BlockBush {
    @Shadow
    @Final
    public static PropertyEnum<BlockPlanks.EnumType> TYPE;

    @Shadow
    protected abstract boolean isTwoByTwoOfType(World worldIn, BlockPos pos, int p_181624_3_, int p_181624_4_, BlockPlanks.EnumType type);

    /**
     * @author Radviger
     * @reason Custom pine tree
     */
    @Overwrite
    public void generateTree(World world, BlockPos pos, IBlockState state, Random rand) {
        if (!net.minecraftforge.event.terraingen.TerrainGen.saplingGrowTree(world, rand, pos)) return;
        WorldGenerator gen = rand.nextInt(10) == 0 ? new WorldGenBigTree(true) : new WorldGenTrees(true);
        int i = 0;
        int j = 0;
        boolean flag = false;

        switch (state.getValue(TYPE)) {
            case SPRUCE:
                loop:

                for (i = 0; i >= -1; --i) {
                    for (j = 0; j >= -1; --j) {
                        if (isTwoByTwoOfType(world, pos, i, j, BlockPlanks.EnumType.SPRUCE)) {
                            gen = new WorldGenMegaPineTree(false, true);
                            flag = true;
                            break loop;
                        }
                    }
                }

                if (!flag) {
                    i = 0;
                    j = 0;
                    gen = new WorldGenTaiga2(true);
                }
                break;
            case BIRCH:
                gen = new WorldGenBirchTree(true, false);
                break;
            case JUNGLE:
                IBlockState log = Blocks.LOG.getDefaultState().withProperty(BlockOldLog.VARIANT, BlockPlanks.EnumType.JUNGLE);
                IBlockState leaf = Blocks.LEAVES.getDefaultState().withProperty(BlockOldLeaf.VARIANT, BlockPlanks.EnumType.JUNGLE).withProperty(BlockLeaves.CHECK_DECAY, Boolean.valueOf(false));
                loop:

                for (i = 0; i >= -1; --i) {
                    for (j = 0; j >= -1; --j) {
                        if (this.isTwoByTwoOfType(world, pos, i, j, BlockPlanks.EnumType.JUNGLE)) {
                            gen = new WorldGenMegaJungle(true, 10, 20, log, leaf);
                            flag = true;
                            break loop;
                        }
                    }
                }

                if (!flag) {
                    i = 0;
                    j = 0;
                    gen = new WorldGenTrees(true, 4 + rand.nextInt(7), log, leaf, false);
                }

                break;
            case ACACIA:
                gen = new WorldGenSavannaTree(true);
                break;
            case DARK_OAK:
                loop:

                for (i = 0; i >= -1; --i) {
                    for (j = 0; j >= -1; --j) {
                        if (isTwoByTwoOfType(world, pos, i, j, BlockPlanks.EnumType.DARK_OAK)) {
                            gen = new WorldGenCanopyTree(true);
                            flag = true;
                            break loop;
                        }
                    }
                }

                if (!flag) {
                    return;
                }

            case OAK:
        }

        IBlockState air = Blocks.AIR.getDefaultState();

        if (flag) {
            world.setBlockState(pos.add(i, 0, j), air, 4);
            world.setBlockState(pos.add(i + 1, 0, j), air, 4);
            world.setBlockState(pos.add(i, 0, j + 1), air, 4);
            world.setBlockState(pos.add(i + 1, 0, j + 1), air, 4);
        } else {
            world.setBlockState(pos, air, 4);
        }

        if (!gen.generate(world, rand, pos.add(i, 0, j))) {
            if (flag) {
                world.setBlockState(pos.add(i, 0, j), state, 4);
                world.setBlockState(pos.add(i + 1, 0, j), state, 4);
                world.setBlockState(pos.add(i, 0, j + 1), state, 4);
                world.setBlockState(pos.add(i + 1, 0, j + 1), state, 4);
            } else {
                world.setBlockState(pos, state, 4);
            }
        }
    }

    /**
     * @author Radviger
     * @reason Sorted CreativeTab items
     */
    @Overwrite
    public void getSubBlocks(CreativeTabs itemIn, NonNullList<ItemStack> items) {
        for (BlockPlanks.EnumType type : BlockPlanks.EnumType.values()) {
            items.add(new ItemStack(this, 1, type.getMetadata()));
        }
        for (BlockPlanks2.PlanksType2 type : BlockPlanks2.PlanksType2.values()) {
            items.add(new ItemStack(CraftBlocks.SAPLING2, 1, type.getMetadata()));
        }
    }
}
