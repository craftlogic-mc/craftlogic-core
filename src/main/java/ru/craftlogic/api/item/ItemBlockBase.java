package ru.craftlogic.api.item;

import net.minecraft.item.ItemBlock;
import net.minecraft.item.ItemStack;
import ru.craftlogic.api.block.BlockBase;

public class ItemBlockBase extends ItemBlock {
    public ItemBlockBase(BlockBase block) {
        super(block);
    }

    @Override
    public boolean getHasSubtypes() {
        return ((BlockBase)this.block).getHasSubtypes();
    }

    @Override
    public String getTranslationKey(ItemStack stack) {
        return ((BlockBase)this.block).getTranslationKey(stack);
    }

    @Override
    public int getMetadata(int meta) {
        return this.getHasSubtypes() ? meta : 0;
    }
}
