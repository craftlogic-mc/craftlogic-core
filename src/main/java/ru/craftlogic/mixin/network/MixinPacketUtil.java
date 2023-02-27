package ru.craftlogic.mixin.network;

import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.network.PacketBuffer;
import net.minecraftforge.common.util.PacketUtil;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Overwrite;

@Mixin(PacketUtil.class)
public class MixinPacketUtil {

    /**
     * @author
     * @reason
     */

    @Overwrite
    public static void writeItemStackFromClientToServer(PacketBuffer buffer, ItemStack stack) {
        if (stack.isEmpty()) {
            buffer.writeShort(-1);
        } else {
            buffer.writeShort(Item.getIdFromItem(stack.getItem()));
            buffer.writeByte(stack.getCount());
            buffer.writeInt(stack.getMetadata());
            NBTTagCompound nbttagcompound = null;
            if (stack.getItem().isDamageable() || stack.getItem().getShareTag()) {
                nbttagcompound = stack.getTagCompound();
            }

            buffer.writeCompoundTag(nbttagcompound);
        }

    }
}
