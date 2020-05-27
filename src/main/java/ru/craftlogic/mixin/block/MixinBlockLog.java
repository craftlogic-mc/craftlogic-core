package ru.craftlogic.mixin.block;

import net.minecraft.block.Block;
import net.minecraft.block.BlockLog;
import net.minecraft.block.material.Material;
import net.minecraft.block.state.IBlockState;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.ItemStack;
import net.minecraft.util.EnumHand;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;
import net.minecraftforge.common.ForgeHooks;
import org.spongepowered.asm.mixin.Mixin;
import ru.craftlogic.CraftConfig;

@Mixin(BlockLog.class)
public class MixinBlockLog extends Block {
    public MixinBlockLog(Material material) {
        super(material);
    }

    @Override
    public float getPlayerRelativeBlockHardness(IBlockState state, EntityPlayer player, World world, BlockPos pos) {
        if (CraftConfig.tweaks.disableHandLogBreaking) {
            ItemStack heldItem = player.getHeldItem(EnumHand.MAIN_HAND);
            return heldItem.getDestroySpeed(state) == 1F ? 0F : super.getPlayerRelativeBlockHardness(state, player, world, pos);
        } else {
            return ForgeHooks.blockStrength(state, player, world, pos);
        }
    }
}
