package ru.craftlogic.api.tile;

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
import net.minecraft.util.text.ITextComponent;
import net.minecraft.util.text.TextComponentTranslation;
import net.minecraft.world.World;
import ru.craftlogic.api.block.BlockBase;
import ru.craftlogic.api.inventory.InventoryFieldHolder;
import ru.craftlogic.api.world.Locateable;
import ru.craftlogic.api.world.Location;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;

public class TileEntityBase extends TileEntity implements Locateable {
    private boolean loaded;
    private InventoryFieldHolder fieldHolder = new InventoryFieldHolder(this::addInvSyncFields);

    protected TileEntityBase(World world) {
        this.setWorldCreate(world);
    }

    public void addInvSyncFields(InventoryFieldHolder fieldHolder) {}

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

    public void markForUpdate() {
        if (this.world != null && !this.world.isRemote) {
            IBlockState state = this.world.getBlockState(this.getPos());
            this.world.notifyBlockUpdate(this.getPos(), state, state, 3);
        }
    }

    public IBlockState getState() {
        return this.getBlockType().getStateFromMeta(this.getBlockMetadata());
    }

    public ItemStack getItemStack() {
        return new ItemStack(this.getBlockType(), 1, this.getBlockMetadata());
    }

    @Nonnull
    @Override
    public ITextComponent getDisplayName() {
        if (!(this.getBlockType() instanceof BlockBase)) {
            return new TextComponentTranslation(this.getBlockType().getUnlocalizedName() + ".name");
        } else {
            BlockBase block = (BlockBase) this.getBlockType();
            ItemStack stack = this.getItemStack();
            return new TextComponentTranslation(block.getUnlocalizedName(stack) + ".name");
        }
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

    @Deprecated
    @Override
    public final void onLoad() {}

    protected boolean isLoaded() {
        return this.loaded;
    }

    protected void onLoaded() {}

    protected void onUnloaded() {}

    public boolean onActivated(EntityPlayer player, EnumHand hand, EnumFacing side, float hitX, float hitY, float hitZ) {
        return false;
    }

    public void onPlacedBy(EntityLivingBase placer, ItemStack stack) {
    }

    public IBlockState getActualState(IBlockState metaState) {
        return metaState;
    }

    @Override
    public final NBTTagCompound getUpdateTag() {
        NBTTagCompound compound = new NBTTagCompound();
        this.writeToPacket(compound);
        return compound;
    }

    @Nullable
    @Override
    public final SPacketUpdateTileEntity getUpdatePacket() {
        NBTTagCompound compound = this.getUpdateTag();
        return compound.getSize() > 0 ? new SPacketUpdateTileEntity(this.pos, 0, compound) : super.getUpdatePacket();
    }

    @Override
    public void onDataPacket(NetworkManager networkManager, SPacketUpdateTileEntity packet) {
        this.readFromPacket(packet.getNbtCompound());
    }

    protected void writeToPacket(NBTTagCompound compound) {}

    protected void readFromPacket(NBTTagCompound compound) {}
}