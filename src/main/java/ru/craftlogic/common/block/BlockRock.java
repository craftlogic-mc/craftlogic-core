package ru.craftlogic.common.block;

import net.minecraft.block.SoundType;
import net.minecraft.creativetab.CreativeTabs;
import ru.craftlogic.api.block.BlockLyingItem;

public class BlockRock extends BlockLyingItem {
    public BlockRock() {
        super("rock", 0F, CreativeTabs.DECORATIONS);
        setSoundType(SoundType.STONE);
    }

    @Override
    protected double getMaxOffset() {
        return 0.2;
    }
}
