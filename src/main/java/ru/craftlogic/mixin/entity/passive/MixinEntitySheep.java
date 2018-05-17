package ru.craftlogic.mixin.entity.passive;

import net.minecraft.entity.passive.EntityAnimal;
import net.minecraft.entity.passive.EntitySheep;
import net.minecraft.init.Blocks;
import net.minecraft.init.SoundEvents;
import net.minecraft.item.EnumDyeColor;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.IBlockAccess;
import net.minecraft.world.World;
import net.minecraftforge.common.IShearable;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Overwrite;
import org.spongepowered.asm.mixin.Shadow;

import java.util.ArrayList;
import java.util.List;

@Mixin(EntitySheep.class)
public abstract class MixinEntitySheep extends EntityAnimal implements IShearable {
    public MixinEntitySheep(World world) {
        super(world);
    }

    @Overwrite(remap = false)
    public List<ItemStack> onSheared(ItemStack tool, IBlockAccess blockAccessor, BlockPos pos, int meta) {
        this.setSheared(true);
        int i = 1 + this.rand.nextInt(3);
        List<ItemStack> drops = new ArrayList<>();

        for(int j = 0; j < i; ++j) {
            drops.add(new ItemStack(Item.getItemFromBlock(Blocks.CARPET), 1, this.getFleeceColor().getMetadata()));
        }

        this.playSound(SoundEvents.ENTITY_SHEEP_SHEAR, 1F, 1F);
        return drops;
    }

    @Shadow
    public void setSheared(boolean sheared) {}

    @Shadow
    public EnumDyeColor getFleeceColor() { return EnumDyeColor.WHITE; }
}
