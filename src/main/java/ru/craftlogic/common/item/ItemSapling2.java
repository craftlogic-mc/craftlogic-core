package ru.craftlogic.common.item;

import net.minecraft.item.ItemBlock;
import net.minecraft.item.ItemStack;
import ru.craftlogic.common.block.BlockSapling2;

public class ItemSapling2 extends ItemBlock {
    private final BlockSapling2 sapling;

    public ItemSapling2(BlockSapling2 leaves) {
        super(leaves);
        this.sapling = leaves;
        setMaxDamage(0);
        setHasSubtypes(true);
    }

    @Override
    public int getMetadata(int damage) {
        return damage;
    }

    @Override
    public String getTranslationKey(ItemStack stack) {
        return "tile.sapling." +  sapling.getTranslationKey(stack);
    }
}
