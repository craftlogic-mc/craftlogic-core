package ru.craftlogic.common.block;

import net.minecraft.block.SoundType;
import net.minecraft.creativetab.CreativeTabs;
import ru.craftlogic.api.block.BlockLyingItem;

public class BlockRock extends BlockLyingItem {
    private final double maxOffset;

    public BlockRock(String name, double maxOffset) {
        super(name, 0F, CreativeTabs.MATERIALS);
        this.maxOffset = maxOffset;
        setSoundType(SoundType.STONE);
    }

    @Override
    protected double getMaxOffset() {
        return maxOffset;
    }
}
