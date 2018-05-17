package ru.craftlogic.mixin.entity.item;

import net.minecraft.block.material.Material;
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
import ru.craftlogic.CraftLogic;
import ru.craftlogic.api.inventory.manager.InventoryItemManager;
import ru.craftlogic.api.inventory.manager.ListInventoryItemManager;
import ru.craftlogic.api.world.Location;
import ru.craftlogic.common.block.BlockFurnace;
import ru.craftlogic.common.item.ItemCrowbar;
import ru.craftlogic.util.Furnace;

import java.util.Random;

import static ru.craftlogic.CraftLogic.SOUND_FURNACE_VENT_CLOSE;
import static ru.craftlogic.CraftLogic.SOUND_FURNACE_VENT_OPEN;
import static ru.craftlogic.util.Furnace.getItemBurnTemperature;
import static ru.craftlogic.util.Furnace.getItemBurnTime;

@Mixin(EntityMinecartFurnace.class)
public abstract class MixinEntityMinecartFurnace extends EntityMinecart implements Furnace {
    private static final DataParameter<Boolean> OPEN = EntityDataManager.createKey(EntityMinecartFurnace.class, DataSerializers.BOOLEAN);
    private static final DataParameter<Integer> TEMPERATURE = EntityDataManager.createKey(EntityMinecartFurnace.class, DataSerializers.VARINT);
    private static final DataParameter<Integer> HOT_TEMPERATURE = EntityDataManager.createKey(EntityMinecartFurnace.class, DataSerializers.VARINT);
    private static final DataParameter<Integer> MAX_TEMPERATURE = EntityDataManager.createKey(EntityMinecartFurnace.class, DataSerializers.VARINT);

    private int maxFuel;
    @SideOnly(Side.CLIENT) private int prevTemperature;
    @Shadow private int fuel;
    @Shadow public double pushX, pushZ;

    private NonNullList<ItemStack> inventory = NonNullList.withSize(2, ItemStack.EMPTY);

    public MixinEntityMinecartFurnace(World world) {
        super(world);
    }

    @Override
    public InventoryItemManager getItemManager() {
        return new ListInventoryItemManager(this.inventory);
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
        this.dataManager.register(TEMPERATURE, 0);
        this.dataManager.register(HOT_TEMPERATURE, 2000);
        this.dataManager.register(MAX_TEMPERATURE, 2200);
    }

    @Inject(method = "writeEntityToNBT", at = @At("RETURN"))
    protected void writeEntityToNBT(NBTTagCompound compound, CallbackInfo info) {
        compound.setBoolean("Open", this.isFurnaceOpen());
        compound.setInteger("Temperature", this.getTemperature());
        compound.setInteger("HotTemperature", this.getHotTemperature());
        compound.setInteger("MaxTemperature", this.getMaxTemperature());
        ItemStackHelper.saveAllItems(compound, this.inventory);
    }

    @Inject(method = "readEntityFromNBT", at = @At("RETURN"))
    protected void readEntityFromNBT(NBTTagCompound compound, CallbackInfo info) {
        this.setFurnaceOpen(compound.getBoolean("Open"));
        this.setTemperature(compound.getInteger("Temperature"));
        this.setHotTemperature(compound.getInteger("HotTemperature"));
        this.setMaxTemperature(compound.getInteger("MaxTemperature"));
        ItemStackHelper.loadAllItems(compound, this.inventory);
    }

    private boolean isFurnaceOpen() {
        return this.dataManager.get(OPEN);
    }

    private void setFurnaceOpen(boolean open) {
        this.dataManager.set(OPEN, open);
    }

    @Overwrite
    public IBlockState getDefaultDisplayTile() {
        return CraftLogic.BLOCK_FURNACE.getDefaultState()
                .withProperty(BlockFurnace.ACTIVE, this.isMinecartPowered())
                .withProperty(BlockFurnace.OPEN, this.isFurnaceOpen());
    }

    @Overwrite
    public void onUpdate() {
        super.onUpdate();
        if (!this.world.isRemote) {
            Location location = this.getLocation();
            boolean closed = !this.isFurnaceOpen();
            int envTemperature = this.getEnvironmentTemperature();
            if (envTemperature > this.getTemperature()) {
                this.setTemperature(this.getTemperature() + Math.max(1, (envTemperature - this.getTemperature()) / 4));
            }
            if (this.getTemperature() >= this.getMaxTemperature()) {
                location.explode(null, 1F, true, false);
            } else {
                if (this.getTemperature() >= this.getHotTemperature()) {
                    Random rand = this.world.rand;

                    if (rand.nextInt(10) == 0) {
                        for (int i = 0; i < 4; ++i) {
                            Location offsetLocation = location.randomize(rand, 3);
                            if (offsetLocation.isSameBlockMaterial(Material.AIR) && offsetLocation.canBlockBePlaced(Blocks.FIRE)) {
                                offsetLocation.setBlock(Blocks.FIRE);
                            }
                        }
                    }
                }
                if (this.fuel <= 0 && this.ticksExisted % (closed ? 4 : 8) == 0) {
                    this.setTemperature(Math.max(envTemperature, (this.getTemperature() - (closed ? 1 : 2))));
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
                    this.setTemperature(this.getTemperature() + 1);
                    int mod = (int) (10F * getScaledTemperature());
                    this.fuel = Math.max(0, this.fuel - (closed ? 4 : 8) * Math.max(1, mod));
                }
                if (this.fuel == 0 && this.maxFuel > 0 && this.world.rand.nextInt(this.maxFuel) >= 100) {
                    growSlotContents(FurnaceSlot.ASH, CraftLogic.ITEM_ASH, 1);
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
        }
    }

    @Override
    public void notifyDataManagerChange(DataParameter<?> parameter) {
        if (parameter == TEMPERATURE && this.world.isRemote) {
            if (this.getTemperature() >= this.getHotTemperature() / 2 && prevTemperature < this.getHotTemperature() / 2) {
                CraftLogic.playSound(this, CraftLogic.SOUND_FURNACE_HOT_LOOP);
            }
            prevTemperature = this.getTemperature();
        }
    }

    @Override
    public int getTemperature() {
        return this.dataManager.get(TEMPERATURE);
    }

    @Override
    public void setTemperature(int temperature) {
        this.dataManager.set(TEMPERATURE, temperature);
    }

    @Override
    public int getHotTemperature() {
        return this.dataManager.get(HOT_TEMPERATURE);
    }

    private void setHotTemperature(int hotTemperature) {
        this.dataManager.set(HOT_TEMPERATURE, hotTemperature);
    }

    @Override
    public int getMaxTemperature() {
        return this.dataManager.get(MAX_TEMPERATURE);
    }

    public void setMaxTemperature(int maxTemperature) {
        this.dataManager.set(MAX_TEMPERATURE, maxTemperature);
    }

    @Override
    public int getFuel() {
        return fuel;
    }

    @Override
    public int getMaxFuel() {
        return maxFuel;
    }

    @Overwrite
    public boolean processInitialInteract(EntityPlayer player, EnumHand hand) {
        ItemStack heldItem = player.getHeldItem(hand);
        if (!super.processInitialInteract(player, hand) && !this.world.isRemote) {
            Location location = this.getLocation();
            if (player.isSneaking()) {
                boolean opened = this.isFurnaceOpen();
                this.setFurnaceOpen(!opened);
                location.playSound(opened ? SOUND_FURNACE_VENT_CLOSE : SOUND_FURNACE_VENT_OPEN, SoundCategory.BLOCKS, 1F, 1F);
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
                this.dropTemperature(100);
            } else if (itemType == Items.POTIONITEM && PotionUtils.getPotionFromItem(heldItem) == PotionTypes.WATER) {
                if (!player.capabilities.isCreativeMode) {
                    heldItem.shrink(1);
                    if (!player.inventory.addItemStackToInventory(new ItemStack(Items.GLASS_BOTTLE))) {
                        player.dropItem(new ItemStack(Items.GLASS_BOTTLE), true);
                    }
                    player.openContainer.detectAndSendChanges();
                }
                this.dropTemperature(10);
            } else if (itemType instanceof ItemCrowbar) {
                this.pushX = (this.posX - player.posX) / 500.0;
                this.pushZ = (this.posZ - player.posZ) / 500.0;
            } else {
                CraftLogic.showScreen(this, player);
            }
        }
        return true;
    }

    @Overwrite
    public void killMinecart(DamageSource damageSource) {
        super.killMinecart(damageSource);
        if (!damageSource.isExplosion() && this.world.getGameRules().getBoolean("doEntityDrops")) {
            this.entityDropItem(new ItemStack(CraftLogic.BLOCK_FURNACE), 0F);
        }
    }
}
