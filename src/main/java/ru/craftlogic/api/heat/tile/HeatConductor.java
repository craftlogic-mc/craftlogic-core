package ru.craftlogic.api.heat.tile;

import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.util.EnumFacing;
import ru.craftlogic.api.block.Updatable;
import ru.craftlogic.api.util.NBTReadWrite;
import ru.craftlogic.api.world.Location;

public abstract class HeatConductor implements NBTReadWrite, Updatable {
    private double heat = 0.0;

    public abstract HeatConnectable getParent();

    public abstract double getCapacity();

    public double getLoss() {
        return 0.1;
    }

    protected void melt(Location location) {
        location.setBlockToAir();
    }

    public void applyHeat(double heat) {
        if ((this.heat += heat) > getCapacity()) {
            this.melt(getParent().getLocation());
        }
    }

    public double getHeat() {
        return heat;
    }

    @Override
    public void update() {
        HeatConnectable parent = getParent();
        Location location = parent.getLocation();

        if (heat > 0) {
            HeatConductor[] receivers = new HeatConductor[6];
            int count = 0;

            for (EnumFacing side : EnumFacing.values()) {
                Location l = location.offset(side);
                HeatConnectable target = l.getTileEntity(HeatConnectable.class);
                if (target != null && parent.canConnectHeatTo(target, side)) {
                    HeatConductor conductor = target.getHeatConductor(side.getOpposite());
                    if (conductor != null) {
                        receivers[side.ordinal()] = conductor;
                        count++;
                    }
                }
            }

            double h = heat / count;

            for (HeatConductor receiver : receivers) {
                if (receiver != null) {
                    receiver.applyHeat(h);
                    this.applyHeat(-h);
                }
            }
        }
    }

    @Override
    public void readFromNBT(NBTTagCompound data) {
        this.heat = data.getDouble("heat");
    }

    @Override
    public NBTTagCompound writeToNBT(NBTTagCompound data) {
        data.setDouble("heat", heat);
        return data;
    }
}
