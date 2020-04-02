package ru.craftlogic.api.tile;

import net.minecraft.block.Block;
import net.minecraft.block.state.IBlockState;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.network.NetworkManager;
import net.minecraft.network.play.server.SPacketUpdateTileEntity;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.EnumHand;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.RayTraceResult;
import net.minecraft.util.text.ITextComponent;
import net.minecraft.util.text.TextComponentTranslation;
import net.minecraft.world.World;
import net.minecraftforge.common.capabilities.Capability;
import net.minecraftforge.fluids.Fluid;
import net.minecraftforge.fluids.capability.CapabilityFluidHandler;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;
import net.minecraftforge.items.CapabilityItemHandler;
import net.minecraftforge.items.wrapper.InvWrapper;
import net.minecraftforge.items.wrapper.SidedInvWrapper;
import ru.craftlogic.api.block.BlockBase;
import ru.craftlogic.api.fluid.FluidHolder;
import ru.craftlogic.api.inventory.InventoryFieldHolder;
import ru.craftlogic.api.inventory.InventoryFieldHolder.InventoryFieldAdder;
import ru.craftlogic.api.inventory.InventoryHolder;
import ru.craftlogic.api.inventory.SidedInventoryHolder;
import ru.craftlogic.api.util.Identifiable;
import ru.craftlogic.api.world.Locatable;
import ru.craftlogic.api.world.Location;
import ru.craftlogic.api.world.WorldNameable;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import java.util.Random;
import java.util.UUID;

public class TileEntityBase extends TileEntity implements Locatable, WorldNameable, InventoryFieldAdder, Identifiable {
    protected int ticksExisted;
    private boolean loaded;
    private InventoryFieldHolder fieldHolder = new InventoryFieldHolder(this);
    private int blockMeta;
    private UUID uuid = UUID.randomUUID();

    protected TileEntityBase(World world, IBlockState state) {
        this.setWorldCreate(world);
        this.blockType = state.getBlock();
        this.blockMeta = -1;
        this.pos = null;
    }

    @Override
    public Block getBlockType() {
        return this.blockType;
    }

    @Override
    public void markDirty() {
        if (this.world != null) {
            IBlockState state = this.world.getBlockState(this.pos);
            this.blockMeta = state.getBlock().getMetaFromState(state);
            this.world.markChunkDirty(this.pos, this);
            if (this.getLocation().getBlock() == this.blockType) {
                this.world.updateComparatorOutputLevel(this.pos, this.getBlockType());
            }
        }
    }

    @Override
    public int getBlockMetadata() {
        if (this.blockMeta == -1) {
            IBlockState state = this.world.getBlockState(this.pos);
            this.blockMeta = state.getBlock().getMetaFromState(state);
        }

        return this.blockMeta;
    }

    @Override
    public void updateContainingBlockInfo() {
        this.blockMeta = -1;
    }

    @Override
    public void addSyncFields(InventoryFieldHolder fieldHolder) {}

    public InventoryFieldHolder getFieldHolder() {
        return this.fieldHolder;
    }

    @Override
    protected void setWorldCreate(World world) {
        this.setWorld(world);
    }

    @Override
    public Location getLocation() {
        return new Location(this.getWorld(), this.getPos());
    }

    public void randomTick(Random random) {}

    @SideOnly(Side.CLIENT)
    public void randomDisplayTick(Random random) {}

    public void markForUpdate() {
        if (world != null && !world.isRemote) {
            IBlockState state = world.getBlockState(getPos());
            world.notifyBlockUpdate(getPos(), state, state, 3);
        }
    }

    public void markForRenderUpdate() {
        if (world != null) {
            world.markBlockRangeForRenderUpdate(getPos(), getPos());
        }
    }

    public IBlockState getState() {
        return this.getBlockType().getStateFromMeta(getBlockMetadata());
    }

    public ItemStack getDroppedItem() {
        return new ItemStack(getBlockType(), 1, getBlockMetadata());
    }

    @Override
    public String getName() {
        if (this.getBlockType() instanceof BlockBase) {
            BlockBase block = (BlockBase) this.getBlockType();
            ItemStack stack = getDroppedItem();
            return block.getTranslationKey(stack) + ".name";
        } else {
            return getBlockType().getTranslationKey() + ".name";
        }
    }

    @Nonnull
    @Override
    public ITextComponent getDisplayName() {
        return new TextComponentTranslation(getName());
    }

    @Override
    public boolean shouldRefresh(World world, BlockPos pos, IBlockState oldState, IBlockState newState) {
        return oldState.getBlock() != newState.getBlock();
    }

    @Override
    public void invalidate() {
        super.invalidate();
        if (this.loaded) {
            this.loaded = false;
            this.onUnloaded();
        }
    }

    @Override
    public void validate() {
        super.validate();
        if (!this.loaded) {
            this.loaded = true;
            this.onLoaded();
        }
    }

    @Override
    public boolean isInvalid() {
        if (world != null && pos != null && !getLocation().isSameBlock(blockType)) {
            return true;
        }
        return super.isInvalid();
    }

    @Deprecated
    @Override
    public final void onLoad() {}

    protected boolean isLoaded() {
        return loaded;
    }

    protected void onLoaded() {}

    protected void onUnloaded() {}

    public boolean onActivated(EntityPlayer player, EnumHand hand, RayTraceResult target) {
        return false;
    }

    public void onPlacedBy(EntityLivingBase placer, ItemStack stack) {
    }

    public IBlockState getActualState(IBlockState metaState) {
        return metaState;
    }

    public void fillWithRain(Fluid fluid) {}

    @Override
    public final NBTTagCompound getUpdateTag() {
        NBTTagCompound data = super.getUpdateTag();
        writeToPacket(data);
        return data;
    }

    @Nullable
    @Override
    public final SPacketUpdateTileEntity getUpdatePacket() {
        NBTTagCompound compound = getUpdateTag();
        return new SPacketUpdateTileEntity(this.pos, 0, compound);
    }

    @Override
    public void readFromNBT(NBTTagCompound compound) {
        super.readFromNBT(compound);
        uuid = compound.getUniqueId("uuid");
    }

    @Override
    public NBTTagCompound writeToNBT(NBTTagCompound compound) {
        compound = super.writeToNBT(compound);
        compound.setUniqueId("uuid", uuid);
        return compound;
    }

    @Override
    public void onDataPacket(NetworkManager networkManager, SPacketUpdateTileEntity packet) {
        try {
            readFromPacket(packet.getNbtCompound());
        } catch (Throwable t) {
            System.out.println("Error reading " + getClass() + " from packet: ");
            t.printStackTrace();
        }
    }

    @Override
    public void handleUpdateTag(NBTTagCompound data) {
        try {
            super.handleUpdateTag(data);
        } catch (Throwable t) {
            System.out.println("Error handling update tag for " + getClass() + ": ");
            t.printStackTrace();
        }
    }

    @Nonnull
    protected NBTTagCompound writeToPacket(NBTTagCompound compound) {
        return compound;
    }

    protected void readFromPacket(NBTTagCompound compound) {}

    @Override
    public boolean hasCapability(Capability<?> capability, @Nullable EnumFacing side) {
        if (capability == CapabilityFluidHandler.FLUID_HANDLER_CAPABILITY) {
            return this instanceof FluidHolder;
        }
        if (capability == CapabilityItemHandler.ITEM_HANDLER_CAPABILITY) {
            return this instanceof InventoryHolder;
        }
        return super.hasCapability(capability, side);
    }

    @Nullable
    @Override
    public <T> T getCapability(Capability<T> capability, @Nullable EnumFacing side) {
        if (capability == CapabilityFluidHandler.FLUID_HANDLER_CAPABILITY && this instanceof FluidHolder) {
            return (T) this;
        }
        if (capability == CapabilityItemHandler.ITEM_HANDLER_CAPABILITY && this instanceof InventoryHolder) {
            return this instanceof SidedInventoryHolder
                    ? (T) new SidedInvWrapper((SidedInventoryHolder) this, side)
                    : (T) new InvWrapper((InventoryHolder) this);
        }
        return super.getCapability(capability, side);
    }

    @Nonnull
    @Override
    public UUID getUUID() {
        return uuid;
    }
}