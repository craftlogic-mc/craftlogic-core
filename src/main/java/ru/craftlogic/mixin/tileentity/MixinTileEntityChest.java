package ru.craftlogic.mixin.tileentity;

import net.minecraft.block.BlockChest;
import net.minecraft.block.state.IBlockState;
import net.minecraft.tileentity.TileEntityChest;
import net.minecraft.tileentity.TileEntityLockableLoot;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.ITickable;
import net.minecraft.util.math.BlockPos;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Overwrite;
import org.spongepowered.asm.mixin.Shadow;
import ru.craftlogic.api.world.TileEntities;
import ru.craftlogic.common.block.ChestPart;

import javax.annotation.Nullable;

import static ru.craftlogic.common.block.ChestProperties.PART;

@Mixin(TileEntityChest.class)
public abstract class MixinTileEntityChest extends TileEntityLockableLoot implements ITickable {
    @Shadow
    public boolean adjacentChestChecked;
    @Shadow
    public TileEntityChest adjacentChestZNeg, adjacentChestXPos, adjacentChestXNeg, adjacentChestZPos;

    @Overwrite
    public void checkForAdjacentChests() {
        if (!this.adjacentChestChecked) {
            if (this.world == null || !this.world.isAreaLoaded(this.pos, 1)) {
                return;
            }

            this.adjacentChestChecked = true;
            this.adjacentChestXNeg = this.adjacentChestZNeg = this.adjacentChestXPos = this.adjacentChestZPos = null;

            IBlockState state = this.world.getBlockState(this.pos);
            if (state.getBlock() instanceof BlockChest) {
                ChestPart part = state.getValue(PART);
                if (part != ChestPart.SINGLE) {
                    EnumFacing side = part.rotate(state.getValue(BlockChest.FACING));
                    switch (side) {
                        case NORTH:
                            this.adjacentChestZNeg = this.getAdjacentChest(EnumFacing.NORTH);
                            break;
                        case SOUTH:
                            this.adjacentChestZPos = this.getAdjacentChest(EnumFacing.SOUTH);
                            break;
                        case WEST:
                            this.adjacentChestXNeg = this.getAdjacentChest(EnumFacing.WEST);
                            break;
                        case EAST:
                            this.adjacentChestXPos = this.getAdjacentChest(EnumFacing.EAST);
                            break;
                    }
                }
            }
        }
    }

    @Overwrite
    @Nullable
    protected TileEntityChest getAdjacentChest(EnumFacing side) {
        BlockPos offsetPos = this.pos.offset(side);
        IBlockState state = this.world.getBlockState(this.pos);
        IBlockState offsetState = this.world.getBlockState(offsetPos);
        if (state.getBlock() == offsetState.getBlock()) {
            ChestPart part = state.getValue(PART);
            if (part != ChestPart.SINGLE) {
                if (offsetState.getValue(PART).opposite() == part) {
                    TileEntityChest chest = TileEntities.getTileEntity(this.world, offsetPos, TileEntityChest.class);
                    if (chest != null && chest.getChestType() == this.getChestType()) {
                        ((MixinTileEntityChest) (Object) chest).setNeighbor((TileEntityChest) (Object) this, side.getOpposite());
                        return chest;
                    }
                }
            }
        }
        return null;
    }

    @Shadow
    public BlockChest.Type getChestType() { return null; }

    @Shadow
    private boolean isChestAt(BlockPos pos) { return false; }

    @Shadow
    private void setNeighbor(TileEntityChest chest, EnumFacing side) {}
}
