package ru.craftlogic.common.item;

import net.minecraft.item.ItemBlock;
import net.minecraft.item.ItemStack;
import net.minecraft.world.ColorizerFoliage;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;
import ru.craftlogic.api.block.Colored;
import ru.craftlogic.common.block.BlockLeaves3;

public class ItemLeaves3 extends ItemBlock implements Colored {
    private final BlockLeaves3 leaves;

    public ItemLeaves3(BlockLeaves3 leaves) {
        super(leaves);
        this.leaves = leaves;
        setMaxDamage(0);
        setHasSubtypes(true);
    }

    @Override
    @SideOnly(Side.CLIENT)
    public int getItemColor(ItemStack stack, int tint) {
        return ColorizerFoliage.getFoliageColorBasic();
    }

    @Override
    public int getMetadata(int damage) {
        return damage;
    }

    @Override
    public String getTranslationKey(ItemStack stack) {
        return "tile.leaves." + leaves.getTranslationKey(stack);
    }
}
