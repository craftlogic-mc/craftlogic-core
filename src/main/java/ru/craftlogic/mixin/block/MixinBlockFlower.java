package ru.craftlogic.mixin.block;

import net.minecraft.block.BlockBush;
import net.minecraft.block.BlockFlower;
import net.minecraft.block.properties.IProperty;
import net.minecraft.block.state.IBlockState;
import net.minecraft.init.Items;
import net.minecraft.item.EnumDyeColor;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.util.NonNullList;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Overwrite;
import org.spongepowered.asm.mixin.Shadow;
import ru.craftlogic.api.block.Shearable;
import ru.craftlogic.api.world.Location;

import javax.annotation.Nonnull;
import java.util.List;
import java.util.Random;

@Mixin(BlockFlower.class)
public abstract class MixinBlockFlower extends BlockBush implements Shearable {
    @Shadow public abstract IProperty<BlockFlower.EnumFlowerType> getTypeProperty();

    private EnumDyeColor getColor(BlockFlower.EnumFlowerType type) {
        switch (type) {
            case DANDELION:
            default:
                return EnumDyeColor.YELLOW;
            case RED_TULIP:
            case POPPY:
                return EnumDyeColor.RED;
            case BLUE_ORCHID:
                return EnumDyeColor.LIGHT_BLUE;
            case ALLIUM:
                return EnumDyeColor.MAGENTA;
            case HOUSTONIA:
            case WHITE_TULIP:
            case OXEYE_DAISY:
                return EnumDyeColor.SILVER;
            case ORANGE_TULIP:
                return EnumDyeColor.ORANGE;
            case PINK_TULIP:
                return EnumDyeColor.PINK;
        }
    }

    /**
     * @author Radviger
     * @reason Drop dyes from flowers instead of flowers themselves
     */
    @Overwrite
    public int damageDropped(IBlockState state) {
        EnumDyeColor color = getColor(state.getValue(getTypeProperty()));
        return color.getDyeDamage();
    }

    @Override
    public Item getItemDropped(IBlockState state, Random rand, int fortune) {
        return Items.DYE;
    }

    @Override
    public boolean isShearable(Location location, @Nonnull ItemStack tool) {
        return true;
    }

    @Override
    public List<ItemStack> onSheared(Location location, int fortune, @Nonnull ItemStack tool) {
        BlockFlower.EnumFlowerType type = location.getBlockProperty(getTypeProperty());
        return NonNullList.withSize(1, new ItemStack(this, 1, type.getMeta()));
    }
}
