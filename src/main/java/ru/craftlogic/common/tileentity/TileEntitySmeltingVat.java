package ru.craftlogic.common.tileentity;

import net.minecraft.block.state.IBlockState;
import net.minecraft.client.gui.GuiScreen;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.ItemStack;
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
import ru.craftlogic.api.inventory.InventoryHolder;
import ru.craftlogic.api.inventory.manager.InventoryItemManager;
import ru.craftlogic.api.inventory.manager.ListInventoryItemManager;
import ru.craftlogic.api.tile.TileEntityBase;
import ru.craftlogic.client.screen.ScreenSmeltingVat;
import ru.craftlogic.common.inventory.ContainerSmeltingVat;

public class TileEntitySmeltingVat extends TileEntityBase implements HeatAcceptor, Updatable,
        InventoryHolder, ScreenHolder {

    private final NonNullList<ItemStack> items = NonNullList.withSize(11, ItemStack.EMPTY);
    private int temperature, maxTemperature = 1200;

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

    }

    @Override
    public int getTemperature() {
        return temperature;
    }

    @Override
    public int getMaxTemperature() {
        return maxTemperature;
    }

    @Override
    public int acceptHeat(EnumFacing side, int amount) {
        if (this.world.getWorldTime() % 4 == 0) {
            this.temperature += amount;
        }
        return amount;
    }

    @Override
    public InventoryItemManager getItemManager() {
        return new ListInventoryItemManager(this.items);
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
}
