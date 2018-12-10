package ru.craftlogic.mixin.entity;

import net.minecraft.entity.EntityLiving;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.init.Blocks;
import net.minecraft.init.Items;
import net.minecraft.inventory.EntityEquipmentSlot;
import net.minecraft.item.Item;
import net.minecraft.item.ItemArmor;
import net.minecraft.item.ItemStack;
import net.minecraft.world.World;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Overwrite;

@Mixin(EntityLiving.class)
public abstract class MixinEntityLiving extends EntityLivingBase {
    public MixinEntityLiving(World world) {
        super(world);
    }

    /**
     * @author Radviger
     * @reason
     */
    @Overwrite
    public static EntityEquipmentSlot getSlotForItemStack(ItemStack item) {
        EntityEquipmentSlot slot = item.getItem().getEquipmentSlot(item);
        if (slot != null) {
            return slot;
        } else if (item.getItem() != Item.getItemFromBlock(Blocks.LIT_PUMPKIN) && item.getItem() != Items.SKULL) {
            if (item.getItem() instanceof ItemArmor) {
                return ((ItemArmor)item.getItem()).armorType;
            } else if (item.getItem() == Items.ELYTRA) {
                return EntityEquipmentSlot.CHEST;
            } else {
                return item.getItem().isShield(item, null) ? EntityEquipmentSlot.OFFHAND : EntityEquipmentSlot.MAINHAND;
            }
        } else {
            return EntityEquipmentSlot.HEAD;
        }
    }
}
