package ru.craftlogic.common.item;

import net.minecraft.creativetab.CreativeTabs;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.util.ActionResult;
import net.minecraft.util.EnumHand;
import net.minecraft.util.math.RayTraceResult;
import net.minecraft.world.World;
import ru.craftlogic.api.item.ThrowableItem;
import ru.craftlogic.common.entity.EntityThrownItem;

public class ItemBrick extends Item implements ThrowableItem {
    private final boolean isNetherBrick;

    public ItemBrick(boolean isNetherBrick) {
        this.isNetherBrick = isNetherBrick;
        this.setUnlocalizedName(isNetherBrick ? "netherbrick" : "brick");
        this.setCreativeTab(CreativeTabs.MATERIALS);
    }

    @Override
    public float getProjectileDamage(EntityThrownItem entity, RayTraceResult target) {
        return this.isNetherBrick ? 0.6F : 0.5F;
    }

    @Override
    public float getProjectileGravityVelocity(EntityThrownItem item) {
        return this.isNetherBrick ? 0.09F : 0.08F;
    }

    @Override
    public ActionResult<ItemStack> onItemRightClick(World world, EntityPlayer player, EnumHand hand) {
        return !player.isSneaking()
                ? this.shoot(world, player, player.getHeldItem(hand))
                : super.onItemRightClick(world, player, hand);
    }
}
