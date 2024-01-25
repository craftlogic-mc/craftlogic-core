package ru.craftlogic.mixin.entity.player;

import net.minecraft.block.Block;
import net.minecraft.entity.EntityLivingBase;
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
import org.spongepowered.asm.mixin.injection.Constant;
import org.spongepowered.asm.mixin.injection.ModifyConstant;
import ru.craftlogic.api.event.player.PlayerCheckCanEditEvent;
import ru.craftlogic.api.event.player.PlayerSneakEvent;

@Mixin(EntityPlayer.class)
public abstract class MixinEntityPlayer extends EntityLivingBase {
    @Shadow
    public PlayerCapabilities capabilities;
    
    public MixinEntityPlayer(World world) {
        super(world);
    }

    /**
     * @author Radviger
     * @reason More events
     */
    @Overwrite
    public boolean canPlayerEdit(BlockPos pos, EnumFacing side, ItemStack item) {
        if (MinecraftForge.EVENT_BUS.post(new PlayerCheckCanEditEvent((EntityPlayer) (Object) this, pos, side, item))) {
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
        MinecraftForge.EVENT_BUS.post(new PlayerSneakEvent((EntityPlayer) (Object) this, sneaking));
        super.setSneaking(sneaking);
    }

    @ModifyConstant(method = "dropItem(Lnet/minecraft/item/ItemStack;ZZ)Lnet/minecraft/entity/item/EntityItem;", constant = @Constant(floatValue = 0.5F))
    public float itemVelocity(float old) {
        return 0.1F;
    }

    @ModifyConstant(method = "dropItem(Lnet/minecraft/item/ItemStack;ZZ)Lnet/minecraft/entity/item/EntityItem;", constant = @Constant(intValue = 40))
    public int itemPickUpDelay(int old) {
        return 20;
    }
}
