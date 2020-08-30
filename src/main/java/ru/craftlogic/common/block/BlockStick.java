package ru.craftlogic.common.block;

import net.minecraft.block.SoundType;
import net.minecraft.creativetab.CreativeTabs;
import net.minecraft.init.Items;
import net.minecraft.item.ItemStack;
import ru.craftlogic.api.block.BlockLyingItem;
import ru.craftlogic.api.world.Location;

public class BlockStick extends BlockLyingItem {
    public BlockStick() {
        super("stick", 0F, CreativeTabs.DECORATIONS);
        setSoundType(SoundType.WOOD);
    }

    @Override
    public ItemStack getItem(Location location) {
        return new ItemStack(Items.STICK);
    }

    @Override
    protected double getMaxOffset() {
        return 0.2;
    }
}
