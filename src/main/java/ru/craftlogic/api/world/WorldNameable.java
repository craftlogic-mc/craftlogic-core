package ru.craftlogic.api.world;

import net.minecraft.util.text.ITextComponent;
import net.minecraft.util.text.TextComponentString;
import net.minecraft.world.IWorldNameable;
import ru.craftlogic.api.tile.TileEntityBase;

public interface WorldNameable extends IWorldNameable {
    @Override
    default String getName() {
        if (this instanceof TileEntityBase) {
            return ((TileEntityBase)this).getItemStack().getUnlocalizedName() + ".name";
        } else if (this instanceof Locateable) {
            return ((Locateable)this).getLocation().getBlock().getUnlocalizedName() + ".name";
        } else {
            return this.getClass().getTypeName().toLowerCase();
        }
    }

    @Override
    default boolean hasCustomName() {
        return false;
    }

    @Override
    default ITextComponent getDisplayName() {
        return new TextComponentString(this.getName());
    }
}
