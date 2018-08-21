package ru.craftlogic.common.tileentity;

import net.minecraft.block.state.IBlockState;
import net.minecraft.util.EnumFacing;
import net.minecraft.world.World;
import ru.craftlogic.api.block.HeatAcceptor;
import ru.craftlogic.api.block.Updatable;
import ru.craftlogic.api.tile.TileEntityBase;
import ru.craftlogic.api.util.TemperatureBuffer;

public class TileEntityCauldron extends TileEntityBase implements HeatAcceptor, Updatable {
    private final TemperatureBuffer temperature = new TemperatureBuffer(200);
    private int fluidColor;

    public TileEntityCauldron(World world, IBlockState state) {
        super(world, state);
    }

    @Override
    public void update() {

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
    public int acceptHeat(EnumFacing side, int amount) {
        if (this.world.getWorldTime() % 4 == 0) {
            this.temperature.accept(amount, false);
        }
        return amount;
    }

    public int getFluidColor() {
        return fluidColor;
    }
}
