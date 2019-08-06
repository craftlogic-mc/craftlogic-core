package ru.craftlogic.common.inventory;

import net.minecraft.block.Block;
import net.minecraft.enchantment.EnchantmentHelper;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.init.Blocks;
import net.minecraft.init.Items;
import net.minecraft.init.SoundEvents;
import net.minecraft.inventory.EntityEquipmentSlot;
import net.minecraft.inventory.IInventory;
import net.minecraft.inventory.Slot;
import net.minecraft.item.Item;
import net.minecraft.item.ItemArmor;
import net.minecraft.item.ItemElytra;
import net.minecraft.item.ItemStack;
import net.minecraft.util.SoundEvent;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;

import javax.annotation.Nullable;

public class SlotArmor extends Slot {
    private final EntityPlayer player;
    private final EntityEquipmentSlot slot;

    public SlotArmor(IInventory inventory, EntityPlayer player, EntityEquipmentSlot slot, int id, int x, int y) {
        super(inventory, id, x, y);
        this.player = player;
        this.slot = slot;
    }

    @Override
    public int getSlotStackLimit() {
        return 1;
    }

    @Override
    public void onSlotChange(ItemStack oldItem, ItemStack newItem) {
        super.onSlotChange(oldItem, newItem);
        if (oldItem.getItem() != newItem.getItem()) {
            if (oldItem.isEmpty() && !newItem.isEmpty()) {
                playEquipSound(newItem, player);
            } else if (!oldItem.isEmpty() && newItem.isEmpty()) {
                playEquipSound(oldItem, player);
            }
        }
    }

    @Override
    public boolean isItemValid(ItemStack item) {
        return item.getItem().isValidArmor(item, slot, player);
    }

    @Override
    public boolean canTakeStack(EntityPlayer player) {
        ItemStack item = this.getStack();
        return (item.isEmpty() || player.isCreative() || !EnchantmentHelper.hasBindingCurse(item)) && super.canTakeStack(player);
    }

    @Nullable
    @SideOnly(Side.CLIENT)
    public String getSlotTexture() {
        return ItemArmor.EMPTY_SLOT_NAMES[slot.getIndex()];
    }

    private static void playEquipSound(ItemStack stack, EntityPlayer player) {
        Item item = stack.getItem();
        SoundEvent sound = null;

        if (item == Items.SKULL) {
            sound = SoundEvents.ITEM_ARMOR_EQUIP_GENERIC;
        } else if (item instanceof ItemArmor) {
            sound = ((ItemArmor) item).getArmorMaterial().getSoundEvent();
        } else if (item instanceof ItemElytra) {
            sound = SoundEvents.ITEM_ARMOR_EQUIP_GENERIC;
        } else if (Block.getBlockFromItem(item) == Blocks.PUMPKIN) {
            sound = SoundEvents.ITEM_ARMOR_EQUIP_GENERIC;
        }

        if (sound != null) {
            player.playSound(sound, 1F, 1F);
        }
    }
}
