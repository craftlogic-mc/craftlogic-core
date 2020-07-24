package ru.craftlogic.api.item;

import net.minecraft.block.Block;
import net.minecraft.block.BlockGlass;
import net.minecraft.block.BlockPane;
import net.minecraft.block.material.Material;
import net.minecraft.block.state.IBlockState;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.init.SoundEvents;
import net.minecraft.item.ItemStack;
import net.minecraft.stats.StatList;
import net.minecraft.util.ActionResult;
import net.minecraft.util.EnumActionResult;
import net.minecraft.util.SoundCategory;
import net.minecraft.util.math.RayTraceResult;
import net.minecraft.world.World;
import ru.craftlogic.api.world.Location;
import ru.craftlogic.common.entity.projectile.EntityThrownItem;

public interface ThrowableItem {
    default boolean onProjectileHit(EntityThrownItem entity, RayTraceResult target) {
        entity.playSound(SoundEvents.BLOCK_STONE_BREAK, 1F, entity.world.rand.nextFloat() * 0.5F + 0.5F);
        if (target.typeOfHit == RayTraceResult.Type.BLOCK) {
            Location loc = new Location(entity.world, target.getBlockPos());
            IBlockState state = loc.getBlockState();
            Block block = state.getBlock();
            if (block instanceof BlockGlass || block instanceof BlockPane && state.getMaterial() == Material.GLASS) {
                loc.setBlockToAir();
                loc.playEvent(2001, Block.getStateId(state));
            }
        }
        int dropChance = this.getProjectileDropChance(entity, target);
        return dropChance <= 0 || entity.world.rand.nextInt(dropChance) != 0;
    }

    default int getProjectileDropChance(EntityThrownItem entity, RayTraceResult target) {
        return 0;
    }

    default float getProjectileDamage(EntityThrownItem entity, RayTraceResult target) {
        return 0F;
    }

    default float getProjectileGravityVelocity(EntityThrownItem item) {
        return 0.06F;
    }

    default int getProjectileCooldown(EntityPlayer player, ItemStack item) {
        return 20;
    }

    default ActionResult<ItemStack> shoot(World world, EntityPlayer player, ItemStack item) {
        Location playerLocation = new Location(player);
        int cooldown = this.getProjectileCooldown(player, item);
        if (cooldown > 0) {
            player.getCooldownTracker().setCooldown(item.getItem(), cooldown);
        }
        if (!world.isRemote) {
            playerLocation.playSound(SoundEvents.ENTITY_EGG_THROW, SoundCategory.PLAYERS, 0.5F, 0.4F / (world.rand.nextFloat() * 0.4F + 0.8F));
            EntityThrownItem entity = new EntityThrownItem(world, player, item, !player.capabilities.isCreativeMode);
            entity.shoot(player, player.rotationPitch, player.rotationYaw, 0F, 0.5F, 1F);
            world.spawnEntity(entity);
            if (!player.capabilities.isCreativeMode) {
                item.shrink(1);
            }
            player.addStat(StatList.getObjectUseStats(item.getItem()));
        }
        return new ActionResult<>(EnumActionResult.SUCCESS, item);
    }
}
