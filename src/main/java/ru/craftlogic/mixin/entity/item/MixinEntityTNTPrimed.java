package ru.craftlogic.mixin.entity.item;

import net.minecraft.entity.Entity;
import net.minecraft.entity.item.EntityTNTPrimed;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.init.Blocks;
import net.minecraft.init.Items;
import net.minecraft.init.SoundEvents;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.world.World;
import org.spongepowered.asm.mixin.Mixin;

@Mixin(EntityTNTPrimed.class)
public abstract class MixinEntityTNTPrimed extends Entity {
    public MixinEntityTNTPrimed(World world) {
        super(world);
    }

    @Override
    public boolean hitByEntity(Entity entity) {
        if (entity instanceof EntityPlayer && !this.world.isRemote) {
            EntityPlayer player = (EntityPlayer) entity;
            ItemStack heldItem = player.getHeldItem(player.getActiveHand());
            if (heldItem.getItem() == Items.SHEARS) {
                heldItem.damageItem(4, player);
                this.setDead();
                this.playSound(SoundEvents.BLOCK_FIRE_EXTINGUISH, 1F, this.world.rand.nextFloat() * 0.5F + 0.5F);
                this.dropItem(Item.getItemFromBlock(Blocks.TNT), 1);
            }
        }
        return super.hitByEntity(entity);
    }
}
