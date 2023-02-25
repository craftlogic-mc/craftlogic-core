package ru.craftlogic.mixin.network;

import io.netty.buffer.ByteBuf;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.network.PacketBuffer;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Overwrite;
import org.spongepowered.asm.mixin.Shadow;

import javax.annotation.Nullable;
import java.io.IOException;

@Mixin(PacketBuffer.class)
public abstract class MixinPacketBuffer {

    @Shadow
    public abstract ByteBuf writeShort(int p_writeShort_1_);

    @Shadow
    public abstract ByteBuf writeByte(int p_writeByte_1_);

    @Shadow
    public abstract PacketBuffer writeCompoundTag(@Nullable NBTTagCompound nbt);

    @Shadow public abstract ByteBuf writeInt(int p_writeInt_1_);

    @Shadow public abstract short readShort();

    @Shadow public abstract byte readByte();

    @Shadow @Nullable public abstract NBTTagCompound readCompoundTag() throws IOException;

    @Shadow public abstract int readInt();

    /**
     * @author Pudo
     * @reason Extended item damage
     */

    @Overwrite
    public PacketBuffer writeItemStack(ItemStack stack) {
        if (stack.isEmpty()) {
            this.writeShort(-1);
        } else {
            this.writeShort(Item.getIdFromItem(stack.getItem()));
            this.writeByte(stack.getCount());
            this.writeInt(stack.getMetadata());
            NBTTagCompound nbttagcompound = null;
            if (stack.getItem().isDamageable() || stack.getItem().getShareTag()) {
                nbttagcompound = stack.getItem().getNBTShareTag(stack);
            }

            this.writeCompoundTag(nbttagcompound);
        }

        return (PacketBuffer) (Object) this;
    }


    /**
     * @author Pudo
     * @reason Extended item damage
     */

    @Overwrite
    public ItemStack readItemStack() throws IOException {
        int i = this.readShort();
        if (i < 0) {
            return ItemStack.EMPTY;
        } else {
            int j = this.readByte();
            int k = this.readInt();
            ItemStack itemstack = new ItemStack(Item.getItemById(i), j, k);
            itemstack.getItem().readNBTShareTag(itemstack, this.readCompoundTag());
            return itemstack;
        }
    }



}
