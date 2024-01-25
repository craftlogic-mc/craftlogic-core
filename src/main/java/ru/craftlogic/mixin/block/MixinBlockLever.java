package ru.craftlogic.mixin.block;

import net.minecraft.block.Block;
import net.minecraft.block.BlockLever;
import net.minecraft.block.material.Material;
import net.minecraft.block.properties.PropertyBool;
import net.minecraft.block.properties.PropertyEnum;
import net.minecraft.block.state.IBlockState;
import net.minecraft.creativetab.CreativeTabs;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.init.Blocks;
import net.minecraft.init.SoundEvents;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.util.*;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Overwrite;
import org.spongepowered.asm.mixin.Shadow;

@Mixin(BlockLever.class)
public class MixinBlockLever extends Block {
    @Shadow @Final
    private static PropertyBool POWERED;
    @Shadow @Final
    private static PropertyEnum<BlockLever.EnumOrientation> FACING;

    public MixinBlockLever(Material material) {
        super(material);
    }

    /**
     * @author Radviger
     * @reason lever backoff mechanism to prevent lag machines with packet spam
     */
    @Overwrite
    public boolean onBlockActivated(World world, BlockPos pos, IBlockState state, EntityPlayer player, EnumHand hand, EnumFacing facing, float hitX, float hitY, float hitZ) {
        CooldownTracker tracker = player.getCooldownTracker();
        if (!tracker.hasCooldown(Item.getItemFromBlock(state.getBlock()))) {
            if (!world.isRemote) {
                state = state.cycleProperty(POWERED);
                world.setBlockState(pos, state, 3);
                float pitch = state.getValue(POWERED) ? 0.6F : 0.5F;
                world.playSound(null, pos, SoundEvents.BLOCK_LEVER_CLICK, SoundCategory.BLOCKS, 0.3F, pitch);
                world.notifyNeighborsOfStateChange(pos, this, false);
                EnumFacing dir = state.getValue(FACING).getFacing();
                world.notifyNeighborsOfStateChange(pos.offset(dir.getOpposite()), this, false);
            }
            tracker.setCooldown(Item.getItemFromBlock(state.getBlock()), 5);
        }
        return true;
    }

    @Override
    public void getSubBlocks(CreativeTabs tab, NonNullList<ItemStack> items) {
        super.getSubBlocks(tab, items);
        items.add(new ItemStack(Blocks.REDSTONE_TORCH));
    }
}
