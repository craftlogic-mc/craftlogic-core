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
import ru.craftlogic.CraftLogic;
import ru.craftlogic.api.block.HeatAcceptor;
import ru.craftlogic.api.block.Updatable;
import ru.craftlogic.api.block.holders.ScreenHolder;
import ru.craftlogic.api.inventory.manager.InventoryItemManager;
import ru.craftlogic.api.inventory.manager.ListInventoryItemManager;
import ru.craftlogic.api.sound.LoopingSoundSource;
import ru.craftlogic.api.tile.TileEntityBase;
import ru.craftlogic.api.world.Location;
import ru.craftlogic.common.CraftItems;
import ru.craftlogic.common.CraftSounds;
import ru.craftlogic.util.Furnace;

import java.util.Random;

import static ru.craftlogic.common.block.BlockFurnace.*;
import static ru.craftlogic.util.Furnace.getItemBurnTemperature;
import static ru.craftlogic.util.Furnace.getItemBurnTime;

public class TileEntityFurnace extends TileEntityBase implements Updatable, Furnace, LoopingSoundSource, ScreenHolder,
        HeatAcceptor {
    private int hotTemperature = 2000;
    private int maxTemperature = hotTemperature + 200;
    private int temperature = 0;
    private int fuel, maxFuel;

    private NonNullList<ItemStack> inventory = NonNullList.withSize(2, ItemStack.EMPTY);

    public TileEntityFurnace(World world, IBlockState state) {
        super(world, state);
    }

    @Override
    public void update() {
        this.ticksExisted++;
        if (!this.world.isRemote) {
            Location location = this.getLocation();
            boolean closed = !location.getBlockProperty(OPEN);
            int envTemperature = this.getEnvironmentTemperature();
            if (envTemperature > this.temperature) {
                this.temperature += Math.max(1, (envTemperature - this.temperature) / 4);
            }
            if (this.temperature >= this.maxTemperature) {
                location.explode(null, 1F, true, false);
                location.setBlock(Blocks.LAVA);
            } else {
                if (this.temperature >= this.hotTemperature) {
                    Random rand = this.world.rand;

                    if (rand.nextInt(10) == 0) {
                        for (int i = 0; i < 4; ++i) {
                            Location offsetLocation = location.randomize(rand, 3);
                            offsetLocation.setBlockIfPossible(Blocks.FIRE);
                        }
                    }
                }
                if (this.ticksExisted % 4 == 0) {
                    HeatAcceptor heatAcceptor = location.offset(EnumFacing.UP).getTileEntity(HeatAcceptor.class);
                    if (heatAcceptor != null) {
                        float targetTemperature = heatAcceptor.getTemperature();
                        if (this.temperature > envTemperature && this.temperature > targetTemperature) {
                            int output = Math.min(this.temperature, 2);
                            this.temperature -= heatAcceptor.acceptHeat(EnumFacing.DOWN, output) / 2;
                        }
                    } else if (this.fuel <= 0) {
                        this.temperature = Math.max(envTemperature, (this.temperature - (closed ? 4 : 8)));
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
                        this.temperature++;
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
                        if (this.temperature >= burnTemperature
                                || this.temperature >= burnTemperature / 2 && this.world.rand.nextInt(2) == 0) {
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
                this.dropTemperature(100);
            } else if (heldItem.getItem() == Items.POTIONITEM && PotionUtils.getPotionFromItem(heldItem) == PotionTypes.WATER) {
                if (!player.capabilities.isCreativeMode) {
                    heldItem.shrink(1);
                    if (!player.inventory.addItemStackToInventory(new ItemStack(Items.GLASS_BOTTLE))) {
                        player.dropItem(new ItemStack(Items.GLASS_BOTTLE), true);
                    }
                    player.openContainer.detectAndSendChanges();
                }
                this.dropTemperature(10);
            } else {
                CraftLogic.showScreen(this, player);
            }
        }
        return true;
    }

    @Override
    public InventoryItemManager getItemManager() {
        return new ListInventoryItemManager(this.inventory);
    }

    @Override
    protected void writeToPacket(NBTTagCompound compound) {
        compound.setInteger("fuel", this.fuel);
        compound.setFloat("temperature", this.temperature);
        compound.setFloat("hotTemperature", this.hotTemperature);
        compound.setFloat("maxTemperature", this.maxTemperature);
    }

    @Override
    public void readFromNBT(NBTTagCompound compound) {
        super.readFromNBT(compound);
        this.fuel = compound.getInteger("fuel");
        this.temperature = compound.getInteger("temperature");
        this.hotTemperature = compound.getInteger("hotTemperature");
        this.maxTemperature = compound.getInteger("maxTemperature");
        ItemStackHelper.loadAllItems(compound, this.inventory);
    }

    @Override
    public NBTTagCompound writeToNBT(NBTTagCompound compound) {
        compound = super.writeToNBT(compound);
        compound.setInteger("fuel", this.fuel);
        compound.setInteger("temperature", this.temperature);
        compound.setInteger("hotTemperature", this.hotTemperature);
        compound.setInteger("maxTemperature", this.maxTemperature);
        ItemStackHelper.saveAllItems(compound, this.inventory);
        return compound;
    }

    @Override
    protected void readFromPacket(NBTTagCompound compound) {
        float oldTemperature = this.temperature;
        this.fuel = compound.getInteger("fuel");
        this.temperature = compound.getInteger("temperature");
        this.hotTemperature = compound.getInteger("hotTemperature");
        this.maxTemperature = compound.getInteger("maxTemperature");
        if (this.world != null && this.pos != null
                && this.temperature >= this.hotTemperature / 2 && oldTemperature < this.hotTemperature / 2) {
            CraftLogic.playSound(this, CraftSounds.FURNACE_HOT_LOOP);
        }
    }

    @Override
    public int getTemperature() {
        return temperature;
    }

    @Override
    public void setTemperature(int temperature) {
        this.temperature = temperature;
    }

    @Override
    public int getHotTemperature() {
        return hotTemperature;
    }

    @Override
    public int getMaxTemperature() {
        return maxTemperature;
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
        this.temperature += amount;
        return amount;
    }
}
