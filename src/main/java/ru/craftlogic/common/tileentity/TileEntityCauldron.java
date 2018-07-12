package ru.craftlogic.common.tileentity;

import net.minecraft.block.state.IBlockState;
import net.minecraft.util.EnumFacing;
import net.minecraft.world.World;
import ru.craftlogic.api.block.HeatAcceptor;
import ru.craftlogic.api.block.Updatable;
import ru.craftlogic.api.tile.TileEntityBase;

public class TileEntityCauldron extends TileEntityBase implements HeatAcceptor, Updatable {
    private int temperature, hotTemperature = 100, maxTemperature = 100;
    private int fluidColor;

    public TileEntityCauldron(World world, IBlockState state) {
        super(world, state);
    }

    @Override
    public void update() {

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
    public int acceptHeat(EnumFacing side, int amount) {
        if (this.world.getWorldTime() % 4 == 0) {
            this.temperature += amount;
        }
        return amount;
    }

    public int getFluidColor() {
        return fluidColor;
    }
}
