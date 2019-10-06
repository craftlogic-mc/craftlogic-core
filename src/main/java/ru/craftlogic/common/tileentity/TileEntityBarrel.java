package ru.craftlogic.common.tileentity;

import net.minecraft.block.material.Material;
import net.minecraft.block.state.IBlockState;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.EnumHand;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.math.RayTraceResult;
import net.minecraft.world.World;
import net.minecraftforge.common.capabilities.Capability;
import net.minecraftforge.fluids.Fluid;
import net.minecraftforge.fluids.FluidStack;
import net.minecraftforge.fluids.capability.IFluidHandlerItem;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;
import ru.craftlogic.api.barrel.Barrel;
import ru.craftlogic.api.barrel.BarrelMode;
import ru.craftlogic.api.barrel.BarrelModeType;
import ru.craftlogic.api.block.Updatable;
import ru.craftlogic.api.CraftRecipes;
import ru.craftlogic.api.tile.TileEntityBase;
import ru.craftlogic.api.CraftBarrelModes;
import ru.craftlogic.common.block.BlockBarrel;
import ru.craftlogic.common.recipe.RecipeBarrel;
import ru.craftlogic.common.recipe.RecipeGridBarrel;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import java.util.Random;

import static net.minecraftforge.fluids.capability.CapabilityFluidHandler.FLUID_HANDLER_ITEM_CAPABILITY;

public class TileEntityBarrel extends TileEntityBase implements Barrel, Updatable {
    private BarrelMode mode;

    public TileEntityBarrel(World world, IBlockState state) {
        super(world, state);
    }

    @Override
    public Material getMaterial() {
        return getState().getMaterial();
    }

    @Override
    public BarrelMode getMode() {
        return mode;
    }

    public void setMode(BarrelMode mode) {
        this.mode = mode;
        if (!world.isRemote) {
            markForUpdate();
        }
    }

    @Override
    public void clear() {
        if (mode != null) {
            mode = null;
            if (!world.isRemote) {
                markForUpdate();
            }
        }
    }

    @Override
    public boolean isEmpty() {
        return getMode() == null || getMode().isEmpty(this);
    }

    @Override
    public boolean isClosed() {
        return getState().getValue(BlockBarrel.CLOSED);
    }

    @Override
    public boolean hasCapability(@Nonnull Capability<?> capability, @Nullable EnumFacing side) {
        BarrelMode mode = getMode();
        return mode != null && mode.hasCapability(capability, side) || super.hasCapability(capability, side);
    }

    @Nullable
    @Override
    public <T> T getCapability(@Nonnull Capability<T> capability, @Nullable EnumFacing side) {
        BarrelMode mode = getMode();
        if (mode != null) {
            T result = mode.getCapability(capability, side);
            if (result != null) {
                return result;
            }
        }
        return super.getCapability(capability, side);
    }

    @Override
    public boolean onActivated(EntityPlayer player, EnumHand hand, RayTraceResult target) {
        ItemStack heldItem = player.getHeldItem(hand);
        BarrelMode mode = getMode();
        if (mode != null) {
            boolean result = mode.interact(this, player, hand);
            if (!world.isRemote && mode.isEmpty(this)) {
                clear();
            }
            return result;
        } else {
            RecipeGridBarrel grid = new RecipeGridBarrel(heldItem, this);
            RecipeBarrel recipe = CraftRecipes.getMatchingRecipe(grid.getClass(), grid);
            if (recipe != null) {
                if (!world.isRemote) {
                    recipe.consume(grid);
                    this.mode = recipe.getMode().createMode(this, recipe);
                    markForUpdate();
                }
                return true;
            } else if (heldItem.hasCapability(FLUID_HANDLER_ITEM_CAPABILITY, null)) {
                IFluidHandlerItem fluidHandlerItem = heldItem.getCapability(FLUID_HANDLER_ITEM_CAPABILITY, null);
                if (fluidHandlerItem != null) {
                    FluidStack fs = fluidHandlerItem.drain(Integer.MAX_VALUE, false);
                    if (fs != null && fs.amount > 0 && !fs.getFluid().isGaseous()) {
                        this.mode = CraftBarrelModes.FLUID.createMode(this);
                        this.mode.interact(this, player, hand);
                        if (!world.isRemote && this.mode.isEmpty(this)) {
                            clear();
                        }
                        return true;
                    }
                }
            }
        }
        return false;
    }

    @Override
    public void randomTick(Random random) {
        BarrelMode mode = getMode();
        if (mode != null) {
            mode.randomTick(this, random);
        }
    }

    @Override
    @SideOnly(Side.CLIENT)
    public void randomDisplayTick(Random random) {
        BarrelMode mode = getMode();
        if (mode != null) {
            mode.randomDisplayTick(this, random);
        }
    }

    @Override
    public void fillWithRain(Fluid fluid) {
        if (!isClosed()) {
            BarrelMode mode = getMode();
            if (mode == null) {
                this.mode = mode = CraftBarrelModes.FLUID.createMode(this);
            }
            mode.fillWithRain(this, fluid);
        }
    }

    @Override
    public void update() {
        BarrelMode mode = getMode();
        if (mode != null) {
            if (mode.isEmpty(this)) {
                if (!world.isRemote) {
                    clear();
                }
            } else {
                mode.update(this);
            }
        }
    }

    @Override
    public NBTTagCompound writeToNBT(NBTTagCompound compound) {
        compound = super.writeToNBT(compound);
        writeMode(mode, compound, "mode");
        return compound;
    }

    @Override
    public void readFromNBT(NBTTagCompound compound) {
        super.readFromNBT(compound);
        mode = readMode(compound, this, "mode");
    }

    @Override
    protected NBTTagCompound writeToPacket(NBTTagCompound compound) {
        writeMode(mode, compound, "mode");
        return compound;
    }

    @Override
    protected void readFromPacket(NBTTagCompound compound) {
        mode = readMode(compound, this, "mode");
        markForRenderUpdate();
    }

    public static BarrelMode readMode(NBTTagCompound compound, Barrel barrel, String key) {
        if (compound.hasKey(key)) {
            NBTTagCompound tag = compound.getCompoundTag(key);
            BarrelModeType type = BarrelModeType.REGISTRY.getValue(new ResourceLocation(tag.getString("name")));
            if (type != null) {
                BarrelMode mode = type.createMode(barrel);
                mode.readFromNBT(tag.getCompoundTag("data"));
                return mode;
            }
        }
        return null;
    }

    public static void writeMode(BarrelMode mode, NBTTagCompound compound, String key) {
        if (mode != null && mode.getRegistryName() != null) {
            NBTTagCompound tag = new NBTTagCompound();

            tag.setString("name", mode.getRegistryName().toString());
            NBTTagCompound data = new NBTTagCompound();
            mode.writeToNBT(data);
            tag.setTag("data", data);

            compound.setTag(key, tag);
        }
    }
}
