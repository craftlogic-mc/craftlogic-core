package ru.craftlogic.common.tileentity;

import net.minecraft.block.state.IBlockState;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.EnumHand;
import net.minecraft.util.math.RayTraceResult;
import net.minecraft.world.World;
import ru.craftlogic.api.inventory.InventoryHolder;
import ru.craftlogic.api.inventory.manager.InventoryManager;
import ru.craftlogic.api.inventory.manager.OneSlotInventoryManager;
import ru.craftlogic.api.tile.TileEntityBase;
import ru.craftlogic.common.recipe.RecipeDrying;
import ru.craftlogic.common.recipe.RecipeGridDrying;

public class TileEntityDryingRack extends TileEntityBase implements InventoryHolder, RecipeGridDrying {
    private ItemStack ingredient = ItemStack.EMPTY;
    private RecipeDrying recipe;
    private int time, maxTime;

    public TileEntityDryingRack(World world, IBlockState state) {
        super(world, state);
    }

    @Override
    public boolean onActivated(EntityPlayer player, EnumHand hand, RayTraceResult target) {
        if (target.sideHit == EnumFacing.UP) {
            return true;
        }
        return false;
    }

    @Override
    public ItemStack getIngredient() {
        return this.ingredient;
    }

    @Override
    public InventoryManager getInventoryManager() {
        return new OneSlotInventoryManager(this.ingredient);
    }

    @Override
    protected void readFromPacket(NBTTagCompound compound) {
        this.ingredient = new ItemStack(compound.getCompoundTag("item"));
        super.readFromPacket(compound);
    }

    @Override
    protected NBTTagCompound writeToPacket(NBTTagCompound compound) {
        compound.setTag("item", this.ingredient.writeToNBT(new NBTTagCompound()));
        return compound;
    }

    @Override
    public void readFromNBT(NBTTagCompound compound) {
        this.ingredient = new ItemStack(compound.getCompoundTag("item"));
        super.readFromNBT(compound);
    }

    @Override
    public NBTTagCompound writeToNBT(NBTTagCompound compound) {
        compound = super.writeToNBT(compound);
        compound.setTag("item", this.ingredient.writeToNBT(new NBTTagCompound()));
        return compound;
    }
}
