package ru.craftlogic.common.block;

import net.minecraft.block.state.IBlockState;
import net.minecraft.item.Item;
import net.minecraft.util.math.AxisAlignedBB;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.IBlockAccess;
import ru.craftlogic.api.CraftItems;

public class BlockBlackberryBush extends BlockBerryBush {
    private static final AxisAlignedBB BOUNDING = new AxisAlignedBB(0, 0, 0, 1, 10 / 16.0, 1);

    public BlockBlackberryBush() {
        super("blackberry");
    }

    @Override
    public AxisAlignedBB getBoundingBox(IBlockState state, IBlockAccess source, BlockPos pos) {
        return BOUNDING;
    }

    @Override
    public Item getBerry() {
        return CraftItems.BLACKBERRY;
    }
}
