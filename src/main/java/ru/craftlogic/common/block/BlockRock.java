package ru.craftlogic.common.block;

import net.minecraft.block.SoundType;
import net.minecraft.creativetab.CreativeTabs;
import net.minecraft.util.math.AxisAlignedBB;
import ru.craftlogic.api.block.BlockLyingItem;
import ru.craftlogic.api.world.Location;

public class BlockRock extends BlockLyingItem {
    private static final AxisAlignedBB BOUNDING = new AxisAlignedBB(4.0 / 16.0, 0.0, 4.0 / 16.0, 12.0 / 16.0, 1.0 / 16.0, 12.0 / 16.0);
    private final double maxOffset;

    public BlockRock(String name, double maxOffset) {
        super(name, 0F, CreativeTabs.MATERIALS);
        this.maxOffset = maxOffset;
        setSoundType(SoundType.STONE);
    }

    @Override
    protected AxisAlignedBB getBoundingBox(Location location) {
        return BOUNDING;
    }

    @Override
    protected double getMaxOffset() {
        return maxOffset;
    }
}
