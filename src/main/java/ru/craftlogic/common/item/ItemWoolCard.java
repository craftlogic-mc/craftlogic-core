package ru.craftlogic.common.item;

import net.minecraft.creativetab.CreativeTabs;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.entity.passive.EntitySheep;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.init.Items;
import net.minecraft.item.ItemStack;
import net.minecraft.util.EnumHand;
import ru.craftlogic.api.item.ItemBase;

public class ItemWoolCard extends ItemBase {
    public ItemWoolCard() {
        super("wool_card", CreativeTabs.TOOLS);
        this.setMaxStackSize(1);
        this.setMaxDamage(32);
    }

    @Override
    public ItemStack getContainerItem(ItemStack item) {
        return damage(item, 1);
    }

    @Override
    public boolean hasContainerItem() {
        return true;
    }

    @Override
    public boolean itemInteractionForEntity(ItemStack item, EntityPlayer player, EntityLivingBase target, EnumHand hand) {
        if (!player.world.isRemote && target instanceof EntitySheep) {
            EntitySheep sheep = (EntitySheep) target;
            sheep.setSheared(true);
            item.damageItem(1 + target.world.rand.nextInt(2), player);
            int amount = target.world.rand.nextInt(3) + 4;
            target.entityDropItem(new ItemStack(Items.STRING, amount, sheep.getFleeceColor().getMetadata()), 0F);
            return true;
        } else {
            return super.itemInteractionForEntity(item, player, target, hand);
        }
    }
}
