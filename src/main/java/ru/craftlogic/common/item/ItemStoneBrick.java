package ru.craftlogic.common.item;

import net.minecraft.creativetab.CreativeTabs;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.ItemStack;
import net.minecraft.util.ActionResult;
import net.minecraft.util.EnumHand;
import net.minecraft.util.math.RayTraceResult;
import net.minecraft.world.World;
import ru.craftlogic.api.item.ItemBase;
import ru.craftlogic.api.item.ThrowableItem;
import ru.craftlogic.common.entity.EntityThrownItem;

public class ItemStoneBrick extends ItemBase implements ThrowableItem {

    public ItemStoneBrick() {
        super("stone_brick", CreativeTabs.MATERIALS);
    }

    @Override
    public float getProjectileDamage(EntityThrownItem entity, RayTraceResult target) {
        return 0.5F;
    }

    @Override
    public float getProjectileGravityVelocity(EntityThrownItem item) {
        return 0.1F;
    }

    @Override
    public ActionResult<ItemStack> onItemRightClick(World world, EntityPlayer player, EnumHand hand) {
        return !player.isSneaking()
                ? this.shoot(world, player, player.getHeldItem(hand))
                : super.onItemRightClick(world, player, hand);
    }
}
