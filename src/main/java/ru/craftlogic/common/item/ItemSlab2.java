package ru.craftlogic.common.item;

import net.minecraft.item.ItemSlab;
import ru.craftlogic.api.CraftBlocks;
import ru.craftlogic.common.block.BlockWoodSlab2;

public class ItemSlab2 extends ItemSlab {
    public ItemSlab2(BlockWoodSlab2 slab) {
        super(slab, slab, CraftBlocks.DOUBLE_WOODEN_SLAB2);
    }
}
