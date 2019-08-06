package ru.craftlogic.mixin.inventory;

import net.minecraft.entity.player.InventoryPlayer;
import net.minecraft.inventory.Container;
import net.minecraft.inventory.ContainerPlayer;
import net.minecraft.inventory.EntityEquipmentSlot;
import net.minecraft.inventory.Slot;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import ru.craftlogic.common.inventory.SlotArmor;

@Mixin(ContainerPlayer.class)
public abstract class MixinContainerPlayer extends Container {
    @Shadow @Final private static EntityEquipmentSlot[] VALID_EQUIPMENT_SLOTS;

    @Override
    protected Slot addSlotToContainer(Slot slot) {
        if (slot.xPos == 8 && slot.slotNumber >= 36 && slot.slotNumber < 36 + 4) {
            int i = 3 - (slot.slotNumber - 36);
            final EntityEquipmentSlot s = VALID_EQUIPMENT_SLOTS[i];
            InventoryPlayer inv = (InventoryPlayer) slot.inventory;
            slot = new SlotArmor(inv, inv.player, s, slot.slotNumber, 8, 8 + i * 18);
        }
        return super.addSlotToContainer(slot);
    }
}
