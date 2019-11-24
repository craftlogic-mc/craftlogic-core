package ru.craftlogic.common.item;

import net.minecraft.block.Block;
import net.minecraft.item.ItemBlock;
import net.minecraft.item.ItemStack;
import net.minecraft.world.ColorizerFoliage;
import ru.craftlogic.api.block.Colored;

public class ItemBerryBush extends ItemBlock implements Colored {
    public ItemBerryBush(Block block) {
        super(block);
    }

    @Override
    public int getItemColor(ItemStack stack, int tint) {
        return tint == 1 ? 0xFF0000 : ColorizerFoliage.getFoliageColorBasic();
    }
}
