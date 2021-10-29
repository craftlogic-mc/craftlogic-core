package ru.craftlogic.mixin.item;

import net.minecraft.entity.EntityAreaEffectCloud;
import net.minecraft.entity.boss.EntityDragon;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.init.Items;
import net.minecraft.init.SoundEvents;
import net.minecraft.item.Item;
import net.minecraft.item.ItemGlassBottle;
import net.minecraft.item.ItemStack;
import net.minecraft.util.ActionResult;
import net.minecraft.util.EnumActionResult;
import net.minecraft.util.EnumHand;
import net.minecraft.util.SoundCategory;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.RayTraceResult;
import net.minecraft.world.World;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Overwrite;
import org.spongepowered.asm.mixin.Shadow;

import java.util.List;

@Mixin(ItemGlassBottle.class)
public abstract class MixinItemGlassBottle extends Item {
    @Shadow protected abstract ItemStack turnBottleIntoItem(ItemStack bottle, EntityPlayer player, ItemStack result);

    /**
     * @author Radviger
     * @reason Disable ability to fill water bottles from water source
     */
    @Overwrite
    public ActionResult<ItemStack> onItemRightClick(World world, EntityPlayer player, EnumHand hand) {
        List<EntityAreaEffectCloud> effectClouds = world.getEntitiesWithinAABB(EntityAreaEffectCloud.class, player.getEntityBoundingBox().grow(2.0D), e -> e != null && e.isEntityAlive() && e.getOwner() instanceof EntityDragon);
        ItemStack heldItem = player.getHeldItem(hand);
        if (!effectClouds.isEmpty()) {
            EntityAreaEffectCloud cloud = effectClouds.get(0);
            cloud.setRadius(cloud.getRadius() - 0.5F);
            world.playSound(null, player.posX, player.posY, player.posZ, SoundEvents.ITEM_BOTTLE_FILL_DRAGONBREATH, SoundCategory.NEUTRAL, 1.0F, 1.0F);
            return new ActionResult<>(EnumActionResult.SUCCESS, this.turnBottleIntoItem(heldItem, player, new ItemStack(Items.DRAGON_BREATH)));
        } else {
            RayTraceResult rayTraceResult = this.rayTrace(world, player, true);
            if (rayTraceResult != null) {
                if (rayTraceResult.typeOfHit == RayTraceResult.Type.BLOCK) {
                    BlockPos pos = rayTraceResult.getBlockPos();
                    if (!world.isBlockModifiable(player, pos) || !player.canPlayerEdit(pos.offset(rayTraceResult.sideHit), rayTraceResult.sideHit, heldItem)) {
                        return new ActionResult<>(EnumActionResult.PASS, heldItem);
                    }

                    /*if (world.getBlockState(pos).getMaterial() == Material.WATER) {
                        world.playSound(player, player.posX, player.posY, player.posZ, SoundEvents.ITEM_BOTTLE_FILL, SoundCategory.NEUTRAL, 1.0F, 1.0F);
                        return new ActionResult<>(EnumActionResult.SUCCESS, this.turnBottleIntoItem(heldItem, player, PotionUtils.addPotionToItemStack(new ItemStack(Items.POTIONITEM), PotionTypes.WATER)));
                    }*/
                }

            }
            return new ActionResult<>(EnumActionResult.PASS, heldItem);
        }
    }
}
