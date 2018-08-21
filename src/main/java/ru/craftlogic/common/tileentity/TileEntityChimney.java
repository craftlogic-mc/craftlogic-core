package ru.craftlogic.common.tileentity;

import net.minecraft.block.state.IBlockState;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.EnumParticleTypes;
import net.minecraft.world.World;
import ru.craftlogic.api.block.SmokeAcceptor;
import ru.craftlogic.api.block.Updatable;
import ru.craftlogic.api.tile.TileEntityBase;
import ru.craftlogic.api.world.Location;
import ru.craftlogic.common.block.BlockChimney;

public class TileEntityChimney extends TileEntityBase implements Updatable, SmokeAcceptor {
    private int smoke, maxSmoke;

    public TileEntityChimney(World world, IBlockState state) {
        super(world, state);
        this.maxSmoke = state.getValue(BlockChimney.VARIANT).getCapacity();
    }

    @Override
    public void update() {
        this.ticksExisted++;
        if (!this.world.isRemote && this.smoke > 0) {
            Location up = getLocation().offset(EnumFacing.UP);
            SmokeAcceptor smokeAcceptor = up.getTileEntity(SmokeAcceptor.class);
            if (smokeAcceptor != null) {
                this.smoke -= smokeAcceptor.acceptSmoke(EnumFacing.DOWN, this.smoke);
            } else {
                if (up.isAir() || !up.isSideSolid(EnumFacing.DOWN)) {
                    boolean large = this.smoke > 10;
                    EnumParticleTypes p = large ? EnumParticleTypes.SMOKE_LARGE : EnumParticleTypes.SMOKE_NORMAL;
                    up.spawnParticle(p, 0, -0.5, 0, 0, 0, 0);
                    this.smoke -= Math.min(this.smoke, 5);
                }
            }
        }
    }

    @Override
    public int acceptSmoke(EnumFacing side, int amount) {
        amount = Math.min(this.maxSmoke - this.smoke, amount);
        this.smoke += amount;
        return amount;
    }
}
