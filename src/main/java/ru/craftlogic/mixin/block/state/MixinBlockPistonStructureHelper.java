package ru.craftlogic.mixin.block.state;

import net.minecraft.block.BlockPistonBase;
import net.minecraft.block.material.EnumPushReaction;
import net.minecraft.block.state.BlockPistonStructureHelper;
import net.minecraft.block.state.IBlockState;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.fml.common.eventhandler.Event;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Overwrite;
import org.spongepowered.asm.mixin.Shadow;
import ru.craftlogic.api.event.block.PistonCheckCanMoveEvent;

import java.util.List;

@Mixin(BlockPistonStructureHelper.class)
public abstract class MixinBlockPistonStructureHelper {
    @Shadow @Final private World world;
    @Shadow @Final private List<BlockPos> toDestroy;
    @Shadow @Final private List<BlockPos> toMove;
    @Shadow @Final private BlockPos blockToMove;
    @Shadow @Final private EnumFacing moveDirection;

    @Shadow protected abstract boolean addBlockLine(BlockPos origin, EnumFacing direction);
    @Shadow protected abstract boolean addBranchingBlocks(BlockPos fromPos);

    @Shadow @Final private BlockPos pistonPos;

    /**
     * @author Radviger
     * @reason Piston moving events
     */
    @Overwrite
    public boolean canMove() {
        toMove.clear();
        toDestroy.clear();
        IBlockState state = world.getBlockState(blockToMove);
        if (!BlockPistonBase.canPush(state, world, blockToMove, moveDirection, false, moveDirection)) {
            if (state.getPushReaction() == EnumPushReaction.DESTROY) {
                toDestroy.add(blockToMove);
                return postEvent();
            } else {
                return false;
            }
        } else if (!addBlockLine(blockToMove, moveDirection)) {
            return false;
        } else {
            for(int i = 0; i < toMove.size(); ++i) {
                BlockPos pos = toMove.get(i);
                if (world.getBlockState(pos).getBlock().isStickyBlock(world.getBlockState(pos)) && !addBranchingBlocks(pos)) {
                    return false;
                }
            }
            return postEvent();
        }
    }

    private boolean postEvent() {
        PistonCheckCanMoveEvent event = new PistonCheckCanMoveEvent(world, pistonPos, blockToMove, moveDirection, toMove, toDestroy);
        MinecraftForge.EVENT_BUS.post(event);
        return event.getResult() != Event.Result.DENY;
    }
}
