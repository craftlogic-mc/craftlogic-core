package ru.craftlogic.mixin.item;

import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.util.text.ITextComponent;
import net.minecraft.util.text.TextComponentString;
import net.minecraft.util.text.event.HoverEvent;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Overwrite;
import org.spongepowered.asm.mixin.Shadow;

@Mixin(ItemStack.class)
public abstract class MixinItemStack {
    @Shadow public abstract boolean hasDisplayName();

    @Shadow public abstract String getDisplayName();

    @Shadow private boolean isEmpty;

    @Shadow public abstract NBTTagCompound writeToNBT(NBTTagCompound nbt);

    @Shadow public abstract Item getItem();

    /**
     * @author Radviger
     * @reason Item stack display without square brackets
     */
    @Overwrite
    public ITextComponent getTextComponent() {
        TextComponentString display = new TextComponentString(getDisplayName());
        if (hasDisplayName()) {
            display.getStyle().setItalic(true);
        }
        if (!isEmpty) {
            NBTTagCompound nbttagcompound = writeToNBT(new NBTTagCompound());
            display.getStyle().setHoverEvent(new HoverEvent(HoverEvent.Action.SHOW_ITEM, new TextComponentString(nbttagcompound.toString())));
            display.getStyle().setColor(getItem().getForgeRarity((ItemStack) (Object) this).getColor());
        }

        return display;
    }
}
