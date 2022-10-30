package ru.craftlogic.common.item;

import net.minecraft.block.BlockFlower;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.ItemMultiTexture;
import net.minecraft.item.ItemStack;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;
import ru.craftlogic.CraftConfig;

public class ItemFlower extends ItemMultiTexture {
    public ItemFlower(BlockFlower.EnumFlowerColor color) {
        super(color.getBlock(), color.getBlock(), item -> BlockFlower.EnumFlowerType.getType(color, item.getMetadata()).getTranslationKey());
        setTranslationKey(color == BlockFlower.EnumFlowerColor.YELLOW ? "flower" : "rose");
    }

    @Override
    public boolean canPlaceBlockOnSide(World world, BlockPos pos, EnumFacing side, EntityPlayer player, ItemStack stack) {
        return CraftConfig.items.enableFlowerPlacing;
    }

}
