package ru.craftlogic.common.item;

import net.minecraft.item.ItemBlock;
import net.minecraft.item.ItemStack;
import ru.craftlogic.common.block.BlockLog3;

public class ItemLog3 extends ItemBlock {
    private final BlockLog3 log;

    public ItemLog3(BlockLog3 log) {
        super(log);
        this.log = log;
        setMaxDamage(0);
        setHasSubtypes(true);
    }

    @Override
    public int getMetadata(int damage) {
        return damage;
    }

    @Override
    public String getTranslationKey(ItemStack stack) {
        return "tile.log." + log.getTranslationKey(stack);
    }
}
