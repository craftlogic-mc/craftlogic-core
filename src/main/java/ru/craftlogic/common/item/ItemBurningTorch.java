package ru.craftlogic.common.item;

import net.minecraft.block.Block;
import net.minecraft.item.ItemBlock;

public class ItemBurningTorch extends ItemBlock {
    public ItemBurningTorch(Block block) {
        super(block);
        this.setHasSubtypes(true);
    }
}
