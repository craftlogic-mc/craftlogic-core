package ru.craftlogic.mixin.entity.player;

import net.minecraft.block.Block;
import net.minecraft.entity.Entity;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.entity.player.PlayerCapabilities;
import net.minecraft.item.ItemStack;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;
import net.minecraftforge.common.MinecraftForge;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Overwrite;
import org.spongepowered.asm.mixin.Shadow;
import ru.craftlogic.api.event.player.PlayerCheckCanEditEvent;
import ru.craftlogic.api.event.player.PlayerSneakEvent;

@Mixin(EntityPlayer.class)
public abstract class MixinEntityPlayer extends Entity {
    @Shadow public PlayerCapabilities capabilities;

    public MixinEntityPlayer(World world) {
        super(world);
    }

    /**
     * @author Radviger
     * @reason More events
     */
    @Overwrite
    public boolean canPlayerEdit(BlockPos pos, EnumFacing side, ItemStack item) {
        if (MinecraftForge.EVENT_BUS.post(new PlayerCheckCanEditEvent((EntityPlayer) (Object)this, pos, side, item))) {
            return false;
        } else if (this.capabilities.allowEdit) {
            return true;
        } else if (item.isEmpty()) {
            return false;
        } else {
            BlockPos offsetPos = pos.offset(side.getOpposite());
            Block block = this.world.getBlockState(offsetPos).getBlock();
            return item.canPlaceOn(block) || item.canEditBlocks();
        }
    }

    @Override
    public void setSneaking(boolean sneaking) {
        MinecraftForge.EVENT_BUS.post(new PlayerSneakEvent((EntityPlayer)(Object)this, sneaking));
        super.setSneaking(sneaking);
    }
}
