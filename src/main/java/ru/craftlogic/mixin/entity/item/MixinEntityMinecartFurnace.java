package ru.craftlogic.mixin.entity.item;

import net.minecraft.block.state.IBlockState;
import net.minecraft.entity.item.EntityMinecart;
import net.minecraft.entity.item.EntityMinecartFurnace;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.init.Blocks;
import net.minecraft.init.Items;
import net.minecraft.init.PotionTypes;
import net.minecraft.init.SoundEvents;
import net.minecraft.inventory.ItemStackHelper;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.network.datasync.DataParameter;
import net.minecraft.network.datasync.DataSerializers;
import net.minecraft.network.datasync.EntityDataManager;
import net.minecraft.potion.PotionUtils;
import net.minecraft.util.*;
import net.minecraft.world.World;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Overwrite;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import ru.craftlogic.api.CraftAPI;
import ru.craftlogic.api.CraftBlocks;
import ru.craftlogic.api.CraftItems;
import ru.craftlogic.api.CraftSounds;
import ru.craftlogic.api.inventory.InventoryFieldHolder;
import ru.craftlogic.api.inventory.manager.InventoryManager;
import ru.craftlogic.api.inventory.manager.ListInventoryManager;
import ru.craftlogic.api.sound.EntitySoundSource;
import ru.craftlogic.api.util.TemperatureBuffer;
import ru.craftlogic.api.world.Location;
import ru.craftlogic.common.block.BlockFurnace;
import ru.craftlogic.common.item.ItemCrowbar;
import ru.craftlogic.util.Furnace;

import java.util.Random;

import static ru.craftlogic.api.CraftSounds.FURNACE_VENT_CLOSE;
import static ru.craftlogic.api.CraftSounds.FURNACE_VENT_OPEN;
import static ru.craftlogic.util.Furnace.getItemBurnTemperature;
import static ru.craftlogic.util.Furnace.getItemBurnTime;

@Mixin(EntityMinecartFurnace.class)
public abstract class MixinEntityMinecartFurnace extends EntityMinecart implements Furnace {
    private static final DataParameter<Boolean> OPEN = EntityDataManager.createKey(EntityMinecartFurnace.class, DataSerializers.BOOLEAN);

    private int maxFuel;
    @SideOnly(Side.CLIENT) private int prevTemperature;
    @Shadow private int fuel;
    @Shadow public double pushX, pushZ;
    private TemperatureBuffer temperature = new TemperatureBuffer(2000);

    private NonNullList<ItemStack> inventory = NonNullList.withSize(2, ItemStack.EMPTY);
    @SideOnly(Side.CLIENT)
    private boolean soundPlaying;

    public MixinEntityMinecartFurnace(World world) {
        super(world);
    }

    @Override
    public InventoryManager getInventoryManager() {
        return new ListInventoryManager(this.inventory);
    }

    @Override
    public void addSyncFields(InventoryFieldHolder fieldHolder) {
        fieldHolder.addReadOnlyField(FurnaceField.FUEL, this::getFuel);
        fieldHolder.addReadOnlyField(FurnaceField.MAX_FUEL, this::getMaxFuel);
        fieldHolder.addReadOnlyField(FurnaceField.TEMPERATURE, this::getTemperature);
        fieldHolder.addReadOnlyField(FurnaceField.MAX_TEMPERATURE, this::getMaxTemperature);
    }

    @Override
    public Location getLocation() {
        return new Location(this);
    }

    @Shadow
    protected boolean isMinecartPowered() {
        return false;
    }

    @Shadow
    protected void setMinecartPowered(boolean powered) {}

    @Override
    public boolean isSoundActive(SoundEvent sound) {
        return !this.isDead && Furnace.super.isSoundActive(sound);
    }

    @Inject(method = "entityInit", at = @At("RETURN"))
    protected void entityInit(CallbackInfo info) {
        this.dataManager.register(OPEN, true);
    }

    @Inject(method = "writeEntityToNBT", at = @At("RETURN"))
    protected void writeEntityToNBT(NBTTagCompound compound, CallbackInfo info) {
        compound.setBoolean("Open", this.isFurnaceOpen());
        this.temperature.writeToNBT(compound, "Temperature");
        ItemStackHelper.saveAllItems(compound, this.inventory);
    }

    @Inject(method = "readEntityFromNBT", at = @At("RETURN"))
    protected void readEntityFromNBT(NBTTagCompound compound, CallbackInfo info) {
        this.setFurnaceOpen(compound.getBoolean("Open"));
        this.temperature.readFromNBT(compound, "Temperature");
        ItemStackHelper.loadAllItems(compound, this.inventory);
    }

    private boolean isFurnaceOpen() {
        return this.dataManager.get(OPEN);
    }

    private void setFurnaceOpen(boolean open) {
        this.dataManager.set(OPEN, open);
    }

    /**
     * @author Radviger
     * @reason Better furnace minecarts
     */
    @Overwrite
    public IBlockState getDefaultDisplayTile() {
        return CraftBlocks.FURNACE.getDefaultState()
                .withProperty(BlockFurnace.ACTIVE, this.isMinecartPowered())
                .withProperty(BlockFurnace.OPEN, this.isFurnaceOpen());
    }

    /**
     * @author Radviger
     * @reason Better furnace minecarts
     */
    @Overwrite
    public void onUpdate() {
        super.onUpdate();
        if (!this.world.isRemote) {
            Location location = this.getLocation();
            boolean closed = !this.isFurnaceOpen();
            if (this.getTemperature() >= this.getMaxTemperature()) {
                location.explode(null, 1F, true, false);
            } else {
                if (this.getTemperature() >= this.getMaxTemperature()) {
                    Random rand = this.world.rand;

                    if (rand.nextInt(10) == 0) {
                        for (int i = 0; i < 4; ++i) {
                            Location offsetLocation = location.randomize(rand, 3);
                            offsetLocation.setBlockIfPossible(Blocks.FIRE);
                        }
                    }
                }
                if (this.fuel <= 0 && this.ticksExisted % (closed ? 4 : 8) == 0) {
                    this.temperature.drain(closed ? 1 : 2, false);
                }
                boolean active = this.fuel > 0;
                if (this.isMinecartPowered() != active) {
                    this.setMinecartPowered(active);
                }
            }

            if (this.getTemperature() <= 0) {
                this.pushX = 0.0;
                this.pushZ = 0.0;
            }

            if (this.fuel > 0) {
                if (this.ticksExisted % 4 == 0) {
                    this.temperature.accept(1, false);
                    int mod = (int) (10F * getScaledTemperature());
                    this.fuel = Math.max(0, this.fuel - (closed ? 4 : 8) * Math.max(1, mod));
                }
                if (this.fuel == 0 && this.maxFuel > 0 && this.world.rand.nextInt(this.maxFuel) >= 100) {
                    growSlotContents(FurnaceSlot.ASH, CraftItems.ASH, 1);
                }
            } else {
                ItemStack fuelItem = this.getStackInSlot(FurnaceSlot.FUEL);
                if (!fuelItem.isEmpty()) {
                    int burnTime = getItemBurnTime(fuelItem);
                    int burnTemperature = getItemBurnTemperature(fuelItem);
                    if (this.getTemperature() >= burnTemperature
                            || this.getTemperature() >= burnTemperature / 2 && this.world.rand.nextInt(2) == 0) {
                        fuelItem.shrink(1);
                        this.fuel = this.maxFuel = burnTime;
                    }
                }
            }
        } else {
            if (this.isMinecartPowered() && this.rand.nextInt(4) == 0) {
                this.world.spawnParticle(EnumParticleTypes.SMOKE_LARGE, this.posX, this.posY + 0.8, this.posZ, 0.0, 0.0, 0.0);
            }
            if (this.getTemperature() >= this.getMaxTemperature() / 3 && prevTemperature < this.getMaxTemperature() / 3) {
                this.world.setEntityState(this, (byte) 1);
            } else if (this.getTemperature() < this.getMaxTemperature() / 3 && prevTemperature >= this.getMaxTemperature() / 3) {
                this.world.setEntityState(this, (byte) 0);
            }
            prevTemperature = this.getTemperature();
        }
    }

    @Override
    public void notifyDataManagerChange(DataParameter<?> parameter) {

    }


    @Override
    public void dropTemperature(int amount, boolean simulate) {
        this.temperature.drain(amount, simulate);
    }

    @Override
    public int getTemperature() {
        return this.temperature.getStored();
    }

    @Override
    public int getMaxTemperature() {
        return this.temperature.getCapacity();
    }

    @Override
    public int getFuel() {
        return fuel;
    }

    @Override
    public int getMaxFuel() {
        return maxFuel;
    }

    /**
     * @author Radviger
     * @reason Better furnace minecarts
     */
    @Overwrite
    public boolean processInitialInteract(EntityPlayer player, EnumHand hand) {
        ItemStack heldItem = player.getHeldItem(hand);
        if (!super.processInitialInteract(player, hand) && !this.world.isRemote) {
            Location location = this.getLocation();
            if (player.isSneaking()) {
                boolean opened = this.isFurnaceOpen();
                this.setFurnaceOpen(!opened);
                location.playSound(opened ? FURNACE_VENT_CLOSE : FURNACE_VENT_OPEN, SoundCategory.BLOCKS, 1F, 1F);
                return true;
            }
            Item itemType = heldItem.getItem();
            if (itemType == Items.FLINT_AND_STEEL) {
                ItemStack fuelItem = this.getStackInSlot(FurnaceSlot.FUEL);
                if (!fuelItem.isEmpty() && !this.isMinecartPowered()) {
                    int burnTime = getItemBurnTime(fuelItem);
                    int burnTemperature = getItemBurnTemperature(fuelItem);
                    if (this.world.rand.nextInt(burnTemperature / 50) == 0) {
                        fuelItem.shrink(1);
                        this.fuel = this.maxFuel = burnTime;
                    }
                }
                heldItem.damageItem(1, player);
                location.playSound(SoundEvents.ITEM_FLINTANDSTEEL_USE, SoundCategory.PLAYERS, 1F, 1F);
            } else if (itemType == Items.WATER_BUCKET) {
                if (!player.capabilities.isCreativeMode) {
                    player.setHeldItem(hand, itemType.getContainerItem(heldItem));
                    player.openContainer.detectAndSendChanges();
                }
                this.dropTemperatureWithEffect(100);
            } else if (itemType == Items.POTIONITEM && PotionUtils.getPotionFromItem(heldItem) == PotionTypes.WATER) {
                if (!player.capabilities.isCreativeMode) {
                    heldItem.shrink(1);
                    if (!player.inventory.addItemStackToInventory(new ItemStack(Items.GLASS_BOTTLE))) {
                        player.dropItem(new ItemStack(Items.GLASS_BOTTLE), true);
                    }
                    player.openContainer.detectAndSendChanges();
                }
                this.dropTemperatureWithEffect(10);
            } else if (itemType instanceof ItemCrowbar) {
                this.pushX = (this.posX - player.posX) / 500.0;
                this.pushZ = (this.posZ - player.posZ) / 500.0;
            } else {
                CraftAPI.showScreen(this, player);
            }
        }
        return true;
    }

    /**
     * @author Radviger
     * @reason Better furnace minecarts
     */
    @Overwrite
    public void killMinecart(DamageSource damageSource) {
        super.killMinecart(damageSource);
        if (!damageSource.isExplosion() && this.world.getGameRules().getBoolean("doEntityDrops")) {
            this.entityDropItem(new ItemStack(CraftBlocks.FURNACE), 0F);
        }
    }

    @Override
    @SideOnly(Side.CLIENT)
    public void handleStatusUpdate(byte status) {
        if (status == 1 && !this.soundPlaying) {
            CraftSounds.playSound(new EntitySoundSource(this, e -> {
                if (!this.soundPlaying || this.isDead) {
                    this.soundPlaying = false;
                    return false;
                } else {
                    return true;
                }
            }, null), CraftSounds.FURNACE_HOT_LOOP);
            this.soundPlaying = true;
        } else if (status == 0 && this.soundPlaying) {
            this.soundPlaying = false;
        }
    }
}
