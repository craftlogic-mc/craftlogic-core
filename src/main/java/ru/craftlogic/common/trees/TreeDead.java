package ru.craftlogic.common.trees;

import com.ferreusveritas.dynamictrees.api.treedata.ITreePart;
import com.ferreusveritas.dynamictrees.blocks.BlockRooty;
import com.ferreusveritas.dynamictrees.trees.Species;
import net.minecraft.init.Blocks;
import net.minecraft.item.ItemStack;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;
import net.minecraftforge.common.BiomeDictionary;
import ru.craftlogic.api.CraftTrees;
import ru.craftlogic.api.trees.TreeBase;

import java.util.Random;

public class TreeDead extends TreeBase {
    public TreeDead() {
        super("dead");
        setPrimitiveLog(Blocks.LOG.getDefaultState(), new ItemStack(Blocks.LOG, 1, 0));
    }

    @Override
    public void createSpecies() {
        setCommonSpecies(new SpeciesDead(this));
    }

    public static class SpeciesDead extends Species {
        SpeciesDead(TreeBase tree) {
            super(tree.getName(), tree, CraftTrees.LEAVES.get("dead"));
            setBasicGrowingParameters(0.3F, 12F, upProbability, lowestBranchHeight, 0.5F);

            envFactor(BiomeDictionary.Type.LUSH, 0.75F);
            envFactor(BiomeDictionary.Type.SPOOKY, 1.05F);
            envFactor(BiomeDictionary.Type.DEAD, 1.05F);

            addAcceptableSoil(Blocks.SAND);
        }

        @Override
        public boolean grow(World world, BlockRooty rootyDirt, BlockPos rootPos, int soilLife, ITreePart treeBase, BlockPos treePos, Random random, boolean natural) {
            return true;
        }

        @Override
        public boolean canGrowWithBoneMeal(World world, BlockPos pos) {
            return false;
        }

        @Override
        public boolean canUseBoneMealNow(World world, Random rand, BlockPos pos) {
            return false;
        }

        @Override
        public boolean rot(World world, BlockPos pos, int neighborCount, int radius, Random random, boolean rapid) {
            return false;
        }
    }
}
