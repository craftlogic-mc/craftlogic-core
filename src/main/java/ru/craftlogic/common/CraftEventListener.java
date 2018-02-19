package ru.craftlogic.common;

import net.minecraft.entity.Entity;
import net.minecraft.entity.effect.EntityLightningBolt;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.init.SoundEvents;
import net.minecraft.item.ItemStack;
import net.minecraft.stats.StatList;
import net.minecraft.util.EnumActionResult;
import net.minecraft.util.SoundCategory;
import net.minecraft.world.World;
import net.minecraftforge.event.entity.living.LivingSpawnEvent;
import net.minecraftforge.event.entity.player.PlayerInteractEvent;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;
import ru.craftlogic.api.block.LightningStruckable;
import ru.craftlogic.api.entity.EntityThrownItem;
import ru.craftlogic.api.world.Location;

public class CraftEventListener {
    @SubscribeEvent
    public void onEntitySpawn(LivingSpawnEvent event) {
        World world = event.getWorld();
        Entity entity = event.getEntity();
        Location location = new Location(entity);
        if (entity instanceof EntityLightningBolt) {
            System.out.println("Bolt: " + location);
            EntityLightningBolt lightningBolt = (EntityLightningBolt)entity;
            if (location.isBlockLoaded()) {
                LightningStruckable struckable = location.getBlock(LightningStruckable.class);
                if (struckable != null) {
                    struckable.onStruckByLightning(world, location, location.getBlockState(), lightningBolt);
                }
            }
        }
    }

    @SubscribeEvent
    public void onItemRightClick(PlayerInteractEvent.RightClickItem event) {
        ItemStack item = event.getItemStack();
        if (EntityThrownItem.isThrowable(item.getItem())) {
            event.setCanceled(true);
            EntityPlayer player = event.getEntityPlayer();
            World world = event.getWorld();

            if (!player.capabilities.isCreativeMode) {
                item.shrink(1);
            }

            world.playSound(null, player.posX, player.posY, player.posZ, SoundEvents.ENTITY_EGG_THROW, SoundCategory.PLAYERS, 0.5F, 0.4F / (world.rand.nextFloat() * 0.4F + 0.8F));
            if (!world.isRemote) {
                EntityThrownItem entity = new EntityThrownItem(world, player, item);
                entity.shoot(player, player.rotationPitch, player.rotationYaw, 0.0F, 1.5F, 1.0F);
                world.spawnEntity(entity);
            }

            player.addStat(StatList.getObjectUseStats(item.getItem()));
            event.setCancellationResult(EnumActionResult.SUCCESS);
        }
    }
}
