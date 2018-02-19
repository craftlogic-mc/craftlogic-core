package ru.craftlogic.api.tile;

import net.minecraft.tileentity.TileEntity;
import net.minecraft.world.World;
import ru.industrial.api.energy.tile.EnergyConnectable;

public abstract class TileEntityEnergyBase extends TileEntityBase implements EnergyConnectable {
    protected boolean loadedToEnet;

    protected TileEntityEnergyBase(World world) {
        super(world);
    }

    @Override
    public TileEntity getTile() {
        return this;
    }

    @Override
    public void invalidate() {
        super.invalidate();
        if (this.loadedToEnet && !this.world.isRemote) {
            this.unloadFromEnet();
            this.loadedToEnet = false;
        }
    }

    @Override
    public void onChunkUnload() {
        if (this.loadedToEnet && !this.world.isRemote) {
            this.unloadFromEnet();
            this.loadedToEnet = false;
        }
        super.onChunkUnload();
    }

    @Override
    public void validate() {
        super.validate();
        if (!this.loadedToEnet && !this.world.isRemote) {
            this.loadToEnet();
            this.loadedToEnet = true;
        }
    }
}