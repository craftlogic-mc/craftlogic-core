package ru.craftlogic.common.item;

import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.ItemStack;
import net.minecraft.util.ActionResult;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.EnumHand;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.RayTraceResult;
import net.minecraft.world.World;
import ru.craftlogic.api.item.ItemBlockBase;
import ru.craftlogic.api.item.ThrowableItem;
import ru.craftlogic.common.block.BlockRock;
import ru.craftlogic.common.entity.projectile.EntityThrownItem;

public class ItemRock extends ItemBlockBase implements ThrowableItem {
    public ItemRock(BlockRock block) {
        super(block);
    }

    @Override
    public boolean canPlaceBlockOnSide(World world, BlockPos pos, EnumFacing side, EntityPlayer player, ItemStack item) {
        return false;
    }

    @Override
    public float getProjectileGravityVelocity(EntityThrownItem item) {
        return 0.04F;
    }

    @Override
    public float getProjectileDamage(EntityThrownItem entity, RayTraceResult target) {
        return 0.4F;
    }

    @Override
    public ActionResult<ItemStack> onItemRightClick(World world, EntityPlayer player, EnumHand hand) {
        return !player.isSneaking()
                ? shoot(world, player, player.getHeldItem(hand))
                : super.onItemRightClick(world, player, hand);
    }
}
