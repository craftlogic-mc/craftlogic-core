package ru.craftlogic.api;

import com.ferreusveritas.dynamictrees.api.treedata.ILeavesProperties;
import com.ferreusveritas.dynamictrees.blocks.BlockDynamicLeaves;
import com.ferreusveritas.dynamictrees.blocks.LeavesPaging;
import com.ferreusveritas.dynamictrees.trees.Species;
import net.minecraft.block.Block;
import net.minecraft.item.Item;
import net.minecraft.util.ResourceLocation;
import net.minecraftforge.fml.common.registry.GameRegistry;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.registries.IForgeRegistry;
import ru.craftlogic.api.trees.TreeBase;
import ru.craftlogic.common.trees.TreeDead;

import javax.annotation.Nonnull;
import java.util.ArrayList;
import java.util.Map;

import static ru.craftlogic.api.CraftAPI.MOD_ID;

public class CraftTrees {
    public static Map<String, ILeavesProperties> LEAVES;

    public static TreeBase DEAD;

    static void init(Side side) {
        LEAVES = LeavesPaging.build(MOD_ID, new ResourceLocation(MOD_ID, "leaves.json"));
        DEAD = registerTree(new TreeDead());

        IForgeRegistry<Block> blockRegistry = GameRegistry.findRegistry(Block.class);

        for (BlockDynamicLeaves leaf : LeavesPaging.getLeavesMapForModId(MOD_ID).values()) {
            blockRegistry.register(leaf);
        }
    }

    public static <T extends TreeBase> T registerTree(@Nonnull T tree) {
        tree.registerSpecies(Species.REGISTRY);
        IForgeRegistry<Block> blockRegistry = GameRegistry.findRegistry(Block.class);
        IForgeRegistry<Item> itemRegistry = GameRegistry.findRegistry(Item.class);
        for (Block block : tree.getRegisterableBlocks(new ArrayList<>())) {
            blockRegistry.register(block);
        }
        for (Item item : tree.getRegisterableItems(new ArrayList<>())) {
            itemRegistry.register(item);
        }
        if (tree.getName().getNamespace().equals(MOD_ID)) {
            CraftTrees.LEAVES.get(tree.getName().getPath()).setTree(tree);
        }
        return tree;
    }
}
