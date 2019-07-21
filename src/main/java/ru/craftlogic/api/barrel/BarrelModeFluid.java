package ru.craftlogic.api.barrel;

import net.minecraft.block.Block;
import net.minecraft.block.material.Material;
import net.minecraft.block.state.IBlockState;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.texture.TextureAtlasSprite;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.init.Blocks;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.EnumHand;
import net.minecraft.util.EnumParticleTypes;
import net.minecraftforge.common.capabilities.Capability;
import net.minecraftforge.fluids.*;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;
import ru.craftlogic.api.block.Mossable;
import ru.craftlogic.api.world.Location;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import java.util.Random;

import static net.minecraftforge.fluids.capability.CapabilityFluidHandler.FLUID_HANDLER_CAPABILITY;
import static net.minecraftforge.fluids.capability.CapabilityFluidHandler.FLUID_HANDLER_ITEM_CAPABILITY;

public class BarrelModeFluid extends BarrelMode {
    private final FluidTank tank = new FluidTank(10_000);

    public BarrelModeFluid(Barrel barrel) {}

    @Override
    public void onCreated(Object... input) {
        FluidStack fs = ((FluidStack) input[0]);
        this.tank.fill(fs, true);
    }

    public FluidStack getFluid() {
        return this.tank.getFluid();
    }

    @Override
    public NBTTagCompound writeToNBT(NBTTagCompound compound) {
        NBTTagCompound tank = new NBTTagCompound();
        this.tank.writeToNBT(tank);
        compound.setTag("Tank", tank);
        return compound;
    }

    @Override
    public void readFromNBT(NBTTagCompound compound) {
        this.tank.readFromNBT(compound.getCompoundTag("Tank"));
    }

    @Override
    public void randomTick(Barrel barrel, Random random) {
        FluidStack fluid = this.getFluid();
        Location location = barrel.getLocation();
        if (fluid != null) {
            int temperature = fluid.getFluid().getTemperature(fluid);
            if (temperature >= 573 /*K*/) {
                if (random.nextFloat() <= this.getFill(barrel)) {
                    if (barrel.getMaterial() == Material.WOOD) {
                        location.playEvent(2001, Block.getStateId(location.getBlockState()));
                        location.setBlockToAir();
                    } else if (temperature >= 1273 /*K*/) {
                        //melt things or smth.
                    }
                }
            } else if (fluid.getFluid() == FluidRegistry.WATER) {
                if (!barrel.isClosed()) {
                    for (EnumFacing side : EnumFacing.values()) {
                        Location l = location.offset(side);
                        Mossable mossable = l.getBlock(Mossable.class);
                        if (fluid.amount >= 100) {
                            if (mossable != null) {
                                if (mossable.growMoss(l)) {
                                    this.tank.drain(100, true);
                                    barrel.markForUpdate();
                                    barrel.markForRenderUpdate();
                                    break;
                                }
                            }
                        } else {
                            break;
                        }
                    }
                    if (location.offset(EnumFacing.UP).canBlockSeeSky() && location.getWorld().isDaytime()) {
                        this.tank.drain(100, true);
                        barrel.markForUpdate();
                        barrel.markForRenderUpdate();
                    }
                }
            }
        }
    }

    @Override
    @SideOnly(Side.CLIENT)
    public void randomDisplayTick(Barrel barrel, Random random) {
        FluidStack fluid = this.getFluid();
        Location location = barrel.getLocation();
        if (fluid != null) {
            int temperature = fluid.getFluid().getTemperature(fluid);
            if (temperature >= 573 /*K*/) {
                float fill = this.getFill(barrel);
                while (random.nextFloat() <= fill / 1.3F) {
                    location.spawnParticle(EnumParticleTypes.SMOKE_NORMAL,
                        random.nextDouble() * 0.875 + 0.0625 - 0.5,
                        random.nextDouble() * 0.2 + 0.5,
                        random.nextDouble() * 0.875 + 0.0625 - 0.5,
                        0, 0, 0
                    );
                    location.spawnParticle(EnumParticleTypes.FLAME,
                        random.nextDouble() * 0.875 + 0.0625 - 0.5,
                        random.nextDouble() * 0.1 - 0.5 + fill,
                        random.nextDouble() * 0.875 + 0.0625 - 0.5,
                        0, 0, 0
                    );
                }
            }
        }
    }

    @Override
    public void fillWithRain(Barrel barrel, Fluid fluid) {
        boolean thundering = barrel.getLocation().getWorld().isThundering();
        FluidStack lastFluid = this.tank.getFluid();
        if (lastFluid != null && lastFluid.getFluid() == FluidRegistry.WATER && fluid.getName().equals("acid_water")) {
            this.tank.setFluid(new FluidStack(fluid, this.tank.getFluidAmount()));
        } else {
            this.tank.fill(new FluidStack(fluid, thundering ? 250 : 100), true);
        }
        barrel.markForUpdate();
        barrel.markForRenderUpdate();
    }

    @Override
    public IBlockState getBlock(Barrel barrel) {
        FluidStack fluid = tank.getFluid();
        if (fluid != null) {
            return fluid.getFluid().getBlock().getDefaultState();
        } else {
            return Blocks.AIR.getDefaultState();
        }
    }

    @Override
    public int getColor(Barrel barrel) {
        FluidStack fluid = tank.getFluid();
        if (fluid != null) {
            return fluid.getFluid().getColor(fluid);
        } else {
            return 0xFFFFFF;
        }
    }

    @Override
    @SideOnly(Side.CLIENT)
    public TextureAtlasSprite getTexture(Minecraft mc, Barrel barrel) {
        FluidStack fluid = tank.getFluid();
        String icon = fluid.getFluid().getStill().toString();
        return mc.getTextureMapBlocks().getTextureExtry(icon);
    }

    @Override
    public float getFill(Barrel barrel) {
        return (float)this.tank.getFluidAmount()/(float)this.tank.getCapacity();
    }

    @Override
    public boolean isEmpty(Barrel barrel) {
        return this.tank.getFluidAmount() <= 0;
    }

    @Override
    public boolean interact(Barrel barrel, EntityPlayer player, EnumHand hand) {
        ItemStack heldItem = player.getHeldItem(hand);
        if (!heldItem.isEmpty()) {
            FluidStack fluid = tank.getFluid();
            if (fluid != null && fluid.getFluid() == FluidRegistry.WATER && fluid.amount >= 1000
                    && heldItem.getItem() == Item.getItemFromBlock(Blocks.SAND)) {

                if (!player.world.isRemote) {
                    heldItem.shrink(1);
                    this.tank.drain(1000, true);
                    ItemStack drop = new ItemStack(Blocks.CLAY);
                    if (!player.addItemStackToInventory(drop)) {
                        player.dropItem(drop, false);
                    }
                    barrel.markForUpdate();
                    barrel.markForRenderUpdate();
                }
            } else if (heldItem.hasCapability(FLUID_HANDLER_ITEM_CAPABILITY, null)) {
                if (!player.world.isRemote) {
                    if (FluidUtil.interactWithFluidHandler(player, hand, this.tank)) {
                        barrel.markForUpdate();
                        barrel.markForRenderUpdate();
                    }
                }
            }
        }
        return true;
    }

    @Override
    public boolean hasCapability(@Nonnull Capability<?> capability, @Nullable EnumFacing side) {
        return capability == FLUID_HANDLER_CAPABILITY && (side == null || side.getAxis().isVertical());
    }

    @Nullable
    @Override
    public <T> T getCapability(@Nonnull Capability<T> capability, @Nullable EnumFacing side) {
        return capability == FLUID_HANDLER_CAPABILITY  && (side == null || side.getAxis().isVertical()) ? (T) this.tank : null;
    }
}
