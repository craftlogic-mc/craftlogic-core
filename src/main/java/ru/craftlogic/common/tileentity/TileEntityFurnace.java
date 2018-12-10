package ru.craftlogic.common.tileentity;

import net.minecraft.block.state.IBlockState;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.init.Blocks;
import net.minecraft.init.Items;
import net.minecraft.init.PotionTypes;
import net.minecraft.init.SoundEvents;
import net.minecraft.inventory.ItemStackHelper;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.potion.PotionUtils;
import net.minecraft.util.*;
import net.minecraft.util.math.RayTraceResult;
import net.minecraft.world.World;
import ru.craftlogic.api.CraftAPI;
import ru.craftlogic.api.CraftItems;
import ru.craftlogic.api.CraftSounds;
import ru.craftlogic.api.block.HeatAcceptor;
import ru.craftlogic.api.block.Updatable;
import ru.craftlogic.api.block.holders.ScreenHolder;
import ru.craftlogic.api.inventory.InventoryFieldHolder;
import ru.craftlogic.api.inventory.manager.InventoryManager;
import ru.craftlogic.api.inventory.manager.ListInventoryManager;
import ru.craftlogic.api.sound.LoopingSoundSource;
import ru.craftlogic.api.tile.TileEntityBase;
import ru.craftlogic.api.util.TemperatureBuffer;
import ru.craftlogic.api.world.Location;
import ru.craftlogic.util.Furnace;

import java.util.Random;

import static ru.craftlogic.common.block.BlockFurnace.*;
import static ru.craftlogic.util.Furnace.getItemBurnTemperature;
import static ru.craftlogic.util.Furnace.getItemBurnTime;

public class TileEntityFurnace extends TileEntityBase implements Updatable, Furnace, LoopingSoundSource, ScreenHolder,
        HeatAcceptor {
    private final TemperatureBuffer temperature = new TemperatureBuffer(2000);
    private int fuel, maxFuel;

    private NonNullList<ItemStack> inventory = NonNullList.withSize(2, ItemStack.EMPTY);

    public TileEntityFurnace(World world, IBlockState state) {
        super(world, state);
    }

    @Override
    public void addSyncFields(InventoryFieldHolder fieldHolder) {
        fieldHolder.addReadOnlyField(FurnaceField.FUEL, this::getFuel);
        fieldHolder.addReadOnlyField(FurnaceField.MAX_FUEL, this::getMaxFuel);
        fieldHolder.addReadOnlyField(FurnaceField.TEMPERATURE, this.temperature::getStored);
        fieldHolder.addReadOnlyField(FurnaceField.MAX_TEMPERATURE, this.temperature::getCapacity);
    }

    @Override
    public void update() {
        this.ticksExisted++;
        if (!this.world.isRemote) {
            Location location = this.getLocation();
            boolean closed = !location.getBlockProperty(OPEN);
            if (this.temperature.getStored() >= this.temperature.getCapacity()) {
                Random rand = this.world.rand;

                if (rand.nextInt(10) == 0) {
                    for (int i = 0; i < 4; ++i) {
                        Location offsetLocation = location.randomize(rand, 3);
                        offsetLocation.setBlockIfPossible(Blocks.FIRE);
                    }
                }
                location.explode(null, 1F, true, false);
                location.setBlock(Blocks.LAVA);
            } else {
                if (this.ticksExisted % 4 == 0) {
                    HeatAcceptor heatAcceptor = location.offset(EnumFacing.UP).getTileEntity(HeatAcceptor.class);
                    if (heatAcceptor != null) {
                        float targetTemperature = heatAcceptor.getTemperature();
                        if (this.temperature.getStored() > targetTemperature) {
                            int output = Math.min(this.temperature.getStored(), 2);
                            this.temperature.drain(heatAcceptor.acceptHeat(EnumFacing.DOWN, output) / 2, false);
                        }
                    } else if (this.fuel <= 0) {
                        this.temperature.drain(closed ? 4 : 8, false);
                    }
                    boolean active = this.fuel > 0;
                    if (location.getBlockProperty(ACTIVE) != active) {
                        location.setBlockProperty(ACTIVE, active);
                    } else {
                        this.markForUpdate();
                    }
                }
                if (this.fuel > 0) {
                    if (this.ticksExisted % (closed ? 4 : 8) == 0) {
                        this.temperature.accept(1, false);
                        this.fuel = Math.max(0, this.fuel - (closed ? 4 : 8));
                    }
                    if (this.fuel == 0 && this.maxFuel > 0 && this.world.rand.nextInt(this.maxFuel) >= 100) {
                        growSlotContents(FurnaceSlot.ASH, CraftItems.ASH, 1);
                    }
                } else {
                    ItemStack fuelItem = this.getStackInSlot(FurnaceSlot.FUEL);
                    if (!fuelItem.isEmpty()) {
                        int burnTime = getItemBurnTime(fuelItem);
                        int burnTemperature = getItemBurnTemperature(fuelItem);
                        if (this.temperature.getStored() >= burnTemperature
                                || this.temperature.getStored() >= burnTemperature / 2 && this.world.rand.nextInt(2) == 0) {
                            fuelItem.shrink(1);
                            this.fuel = this.maxFuel = burnTime;
                        }
                    }
                }
            }
        }
    }

    @Override
    public boolean isSoundActive(SoundEvent sound) {
        return !this.isInvalid() && Furnace.super.isSoundActive(sound);
    }

    @Override
    public void dropTemperature(int amount, boolean simulate) {
        this.temperature.drain(amount, simulate);
    }

    @Override
    public boolean onActivated(EntityPlayer player, EnumHand hand, RayTraceResult target) {
        ItemStack heldItem = player.getHeldItem(hand);
        Location location = this.getLocation();
        EnumFacing facing = location.getBlockProperty(FACING);
        Boolean open = location.getBlockProperty(OPEN);
        if (!this.world.isRemote) {
            if (target.sideHit == facing && open && heldItem.getItem() == Items.FLINT_AND_STEEL) {
                ItemStack fuelItem = this.getStackInSlot(FurnaceSlot.FUEL);
                if (!fuelItem.isEmpty() && !location.getBlockProperty(ACTIVE)) {
                    int burnTime = getItemBurnTime(fuelItem);
                    int burnTemperature = getItemBurnTemperature(fuelItem);
                    if (this.world.rand.nextInt(burnTemperature / 50) == 0) {
                        fuelItem.shrink(1);
                        this.fuel = this.maxFuel = burnTime;
                    }
                }
                heldItem.damageItem(1, player);
                location.playSound(SoundEvents.ITEM_FLINTANDSTEEL_USE, SoundCategory.PLAYERS, 1F, 1F);
            } else if (heldItem.getItem() == Items.WATER_BUCKET) {
                if (!player.capabilities.isCreativeMode) {
                    player.setHeldItem(hand, heldItem.getItem().getContainerItem(heldItem));
                    player.openContainer.detectAndSendChanges();
                }
                this.dropTemperatureWithEffect(100);
            } else if (heldItem.getItem() == Items.POTIONITEM && PotionUtils.getPotionFromItem(heldItem) == PotionTypes.WATER) {
                if (!player.capabilities.isCreativeMode) {
                    heldItem.shrink(1);
                    if (!player.inventory.addItemStackToInventory(new ItemStack(Items.GLASS_BOTTLE))) {
                        player.dropItem(new ItemStack(Items.GLASS_BOTTLE), true);
                    }
                    player.openContainer.detectAndSendChanges();
                }
                this.dropTemperatureWithEffect(10);
            } else {
                CraftAPI.showScreen(this, player);
            }
        }
        return true;
    }

    @Override
    public InventoryManager getInventoryManager() {
        return new ListInventoryManager(this.inventory);
    }

    @Override
    protected NBTTagCompound writeToPacket(NBTTagCompound compound) {
        compound.setInteger("fuel", this.fuel);
        this.temperature.writeToNBT(compound, "temperature");
        return compound;
    }

    @Override
    public void readFromNBT(NBTTagCompound compound) {
        super.readFromNBT(compound);
        this.fuel = compound.getInteger("fuel");
        this.temperature.readFromNBT(compound, "temperature");
        ItemStackHelper.loadAllItems(compound, this.inventory);
    }

    @Override
    public NBTTagCompound writeToNBT(NBTTagCompound compound) {
        compound = super.writeToNBT(compound);
        compound.setInteger("fuel", this.fuel);
        this.temperature.writeToNBT(compound, "temperature");
        ItemStackHelper.saveAllItems(compound, this.inventory);
        return compound;
    }

    @Override
    protected void readFromPacket(NBTTagCompound compound) {
        float oldTemperature = this.temperature.getStored();
        this.fuel = compound.getInteger("fuel");
        this.temperature.readFromNBT(compound, "temperature");
        if (this.world != null && this.pos != null
                && this.temperature.getStored() >= this.temperature.getCapacity() / 3 && oldTemperature < this.temperature.getCapacity() / 3) {
            CraftSounds.playSound(this, CraftSounds.FURNACE_HOT_LOOP);
        }
    }

    @Override
    public int getTemperature() {
        return temperature.getStored();
    }

    @Override
    public int getMaxTemperature() {
        return temperature.getCapacity();
    }

    @Override
    public int getFuel() {
        return fuel;
    }

    @Override
    public int getMaxFuel() {
        return maxFuel;
    }

    @Override
    public int acceptHeat(EnumFacing side, int amount) {
        return this.temperature.accept(amount, false);
    }
}
