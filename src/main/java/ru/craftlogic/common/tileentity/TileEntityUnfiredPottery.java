package ru.craftlogic.common.tileentity;

import net.minecraft.block.Block;
import net.minecraft.block.state.IBlockState;
import net.minecraft.init.Blocks;
import net.minecraft.util.EnumFacing;
import net.minecraft.world.World;
import ru.craftlogic.CraftLogic;
import ru.craftlogic.api.block.HeatAcceptor;
import ru.craftlogic.api.tile.TileEntityBase;
import ru.craftlogic.api.world.Location;
import ru.craftlogic.common.block.BlockUnfiredPottery;

import static ru.craftlogic.common.block.BlockUnfiredPottery.COVERED;
import static ru.craftlogic.common.block.BlockUnfiredPottery.DONE;
import static ru.craftlogic.common.block.BlockUnfiredPottery.VARIANT;

public class TileEntityUnfiredPottery extends TileEntityBase implements HeatAcceptor {
    private int temperature;

    public TileEntityUnfiredPottery(World world, IBlockState state) {
        super(world, state);
    }

    @Override
    public int acceptHeat(EnumFacing side, int amount) {
        Location location = this.getLocation();
        BlockUnfiredPottery.PotteryType variant = location.getBlockProperty(VARIANT);
        int requiredTemperature = variant.getRequiredTemperature();
        boolean covered = location.getBlockProperty(COVERED);
        this.temperature += covered ? amount : 1;
        if (this.temperature >= requiredTemperature && this.world.rand.nextInt(10) == 0) {
            if (!covered) {
                switch (variant) {
                    case CAULDRON:
                        location.setBlock(CraftLogic.BLOCK_CAULDRON);
                        break;
                    case SMELTING_VAT:
                    case FLOWERPOT:
                    default:
                        location.setBlockToAir();
                        break;
                }
                location.playEvent(2001, Block.getStateId(Blocks.CLAY.getDefaultState()));
            } else {
                location.setBlockProperty(DONE, true);
            }
        }
        return amount;
    }

    @Override
    public int getTemperature() {
        return temperature;
    }

    @Override
    public int getMaxTemperature() {
        Location location = this.getLocation();
        BlockUnfiredPottery.PotteryType variant = location.getBlockProperty(VARIANT);
        return variant.getRequiredTemperature();
    }
}
