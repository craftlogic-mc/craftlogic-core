package ru.craftlogic.common.tileentity;

import net.minecraft.block.state.IBlockState;
import net.minecraft.client.gui.GuiScreen;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.inventory.ItemStackHelper;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.EnumHand;
import net.minecraft.util.NonNullList;
import net.minecraft.util.math.RayTraceResult;
import net.minecraft.world.World;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;
import ru.craftlogic.CraftLogic;
import ru.craftlogic.api.block.HeatAcceptor;
import ru.craftlogic.api.block.Updatable;
import ru.craftlogic.api.block.holders.ScreenHolder;
import ru.craftlogic.api.inventory.InventoryFieldHolder;
import ru.craftlogic.api.inventory.InventoryHolder;
import ru.craftlogic.api.inventory.manager.InventoryManager;
import ru.craftlogic.api.inventory.manager.ListInventoryManager;
import ru.craftlogic.api.recipe.ProcessingRecipe;
import ru.craftlogic.api.CraftRecipes;
import ru.craftlogic.api.tile.TileEntityBase;
import ru.craftlogic.api.util.ExperienceBuffer;
import ru.craftlogic.api.util.TemperatureBuffer;
import ru.craftlogic.client.screen.ScreenSmeltingVat;
import ru.craftlogic.common.inventory.ContainerSmeltingVat;
import ru.craftlogic.common.recipe.RecipeAlloying;
import ru.craftlogic.common.recipe.RecipeGridAlloying;

import java.util.List;

public class TileEntitySmeltingVat extends TileEntityBase implements HeatAcceptor, Updatable,
        InventoryHolder, ScreenHolder, RecipeGridAlloying {

    private final NonNullList<ItemStack> items = NonNullList.withSize(3, ItemStack.EMPTY);
    private final ExperienceBuffer experience = new ExperienceBuffer(Float.MAX_VALUE);
    private final TemperatureBuffer temperature = new TemperatureBuffer(2000);
    private ProcessingRecipe<RecipeGridAlloying, RecipeAlloying> currentRecipe;
    public boolean locked;

    public TileEntitySmeltingVat(World world, IBlockState state) {
        super(world, state);
    }

    @Override
    public boolean onActivated(EntityPlayer player, EnumHand hand, RayTraceResult target) {
        if (!this.world.isRemote) {
            CraftLogic.showScreen(this, player);
        }
        return true;
    }

    @Override
    public void update() {
        boolean dirty = false;
        this.ticksExisted++;

        if (!this.world.isRemote) {
            if (this.temperature.getStored() > 0 && this.ticksExisted % 4 == 0) {
                this.temperature.drain(1, false);
            }

            if (this.temperature.getStored() > 0 && this.canOperate()) {
                if (this.currentRecipe != null) {
                    int speed = (int) Math.max(1F, 2F * (float) this.temperature.getStored() / (float)this.currentRecipe.getRecipe().getTemperature());
                    for (int i = 0; i < speed; i++) {
                        if (!this.currentRecipe.incrTimer()) {
                            RecipeAlloying recipe = this.currentRecipe.getRecipe();
                            ItemStack ist = recipe.getResult();
                            this.experience.accept(getLocation(), recipe.getExp(), false);
                            this.growSlotContents(SmelterSlot.OUTPUT, ist, ist.getCount());
                            this.currentRecipe = null;
                            this.markDirty();
                            break;
                        }
                    }
                } else {
                    RecipeAlloying recipe = CraftRecipes.getMatchingRecipe(this);
                    if (recipe != null) {
                        this.currentRecipe = new ProcessingRecipe<>(this, recipe);
                        this.currentRecipe.consume(this);
                    }
                }
            } else {
                if (this.currentRecipe != null && !this.currentRecipe.decrTimer()) {
                    if (!this.locked) {
                        this.locked = true;
                        this.markForUpdate();
                        dirty = true;
                    }
                }
            }

            if (dirty) {
                this.markDirty();
            }
        }
    }

    private boolean canOperate() {
        if (this.currentRecipe != null) {
            RecipeAlloying recipe = this.currentRecipe.getRecipe();
            ItemStack output = this.getStackInSlot(SmelterSlot.OUTPUT);
            if (output.isEmpty()) {
                return true;
            } else if (!output.isItemEqual(recipe.getResult())) {
                return false;
            } else {
                int st = output.getCount() + recipe.getResult().getCount();
                return st <= this.getInventoryStackLimit() && st <= recipe.getResult().getMaxStackSize();
            }
        } else {
            return true;
        }
    }

    @Override
    public boolean isItemValidForSlot(int slot, ItemStack stack) {
        return slot < 2;
    }

    @Override
    public int getTemperature() {
        return temperature.getStored();
    }

    @Override
    public List<ItemStack> getIngredients() {
        return this.items.subList(0, 2);
    }

    @Override
    public int getMaxTemperature() {
        return this.temperature.getCapacity();
    }

    @Override
    public int acceptHeat(EnumFacing side, int amount) {
        if (this.ticksExisted % 2 == 0) {
            this.temperature.accept(amount, false);
        }
        return amount;
    }

    @Override
    public InventoryManager getInventoryManager() {
        return new ListInventoryManager(this.items);
    }

    @Override
    public void addSyncFields(InventoryFieldHolder fieldHolder) {
        fieldHolder.addReadOnlyField(SmelterField.PROGRESS, this::getProgressTime);
        fieldHolder.addReadOnlyField(SmelterField.REQUIRED_TIME, this::getRequiredTime);
        fieldHolder.addReadOnlyField(SmelterField.TEMPERATURE, this::getTemperature);
        fieldHolder.addReadOnlyField(SmelterField.MAX_TEMPERATURE, this::getMaxTemperature);
    }

    public int getProgressTime() {
        return this.currentRecipe != null ? (int)(this.currentRecipe.getProgress() * this.getRequiredTime()) : 0;
    }

    public int getRequiredTime() {
        return this.currentRecipe != null ? this.currentRecipe.getRecipe().getTimeRequired() : 0;
    }

    @Override
    public void readFromNBT(NBTTagCompound compound) {
        super.readFromNBT(compound);
        if (compound.hasKey("ProcessingRecipe")) {
            NBTTagCompound pr = compound.getCompoundTag("ProcessingRecipe");
            this.currentRecipe = ProcessingRecipe.readFromNBT(RecipeGridAlloying.class, pr);
        }
        this.temperature.readFromNBT(compound, "Temperature");
        ItemStackHelper.loadAllItems(compound, this.items);
    }

    @Override
    public NBTTagCompound writeToNBT(NBTTagCompound compound) {
        compound = super.writeToNBT(compound);
        if (this.currentRecipe != null) {
            NBTTagCompound pr = new NBTTagCompound();
            this.currentRecipe.writeToNBT(pr);
            compound.setTag("ProcessingRecipe", pr);
        }
        this.temperature.writeToNBT(compound, "Temperature");
        ItemStackHelper.saveAllItems(compound, this.items);
        return compound;
    }

    @Override
    public ContainerSmeltingVat createContainer(EntityPlayer player, int subId) {
        return new ContainerSmeltingVat(player.inventory, this);
    }

    @Override
    @SideOnly(Side.CLIENT)
    public GuiScreen createScreen(EntityPlayer player, int subId) {
        return new ScreenSmeltingVat(player.inventory, this, this.createContainer(player, subId));
    }

    @Override
    public int getGridSize() {
        return 2;
    }

    @Override
    public float takeExp(float amount, boolean simulate) {
        return this.experience.drain(amount, simulate);
    }

    @Override
    protected NBTTagCompound writeToPacket(NBTTagCompound compound) {
        compound.setBoolean("locked", this.locked);
        return compound;
    }

    @Override
    protected void readFromPacket(NBTTagCompound compound) {
        this.locked = compound.getBoolean("locked");
    }

    public enum SmelterSlot implements SlotIdentifier {
        INPUT_A,
        INPUT_B,
        OUTPUT
    }

    public enum SmelterField implements FieldIdentifier {
        PROGRESS, REQUIRED_TIME, TEMPERATURE, MAX_TEMPERATURE
    }
}
