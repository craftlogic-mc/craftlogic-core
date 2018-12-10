package ru.craftlogic.mixin.block;

import net.minecraft.block.Block;
import net.minecraft.block.BlockDispenser;
import net.minecraft.block.material.Material;
import net.minecraft.block.state.IBlockState;
import net.minecraft.creativetab.CreativeTabs;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.init.Blocks;
import net.minecraft.item.ItemStack;
import net.minecraft.tileentity.TileEntityDispenser;
import net.minecraft.util.NonNullList;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;
import net.minecraftforge.common.MinecraftForge;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import ru.craftlogic.api.event.block.DispenserShootEvent;
import ru.craftlogic.api.tile.Ownable;
import ru.craftlogic.api.world.TileEntities;

import java.util.UUID;

@Mixin(BlockDispenser.class)
public abstract class MixinBlockDispenser extends Block {
    public MixinBlockDispenser(Material material) {
        super(material);
    }

    @Override
    public void getSubBlocks(CreativeTabs tab, NonNullList<ItemStack> items) {
        super.getSubBlocks(tab, items);
        items.add(new ItemStack(Blocks.DROPPER));
        items.add(new ItemStack(Blocks.OBSERVER));
    }

    @Inject(method = "dispense", at = @At("HEAD"), cancellable = true)
    protected void onDispense(World world, BlockPos pos, CallbackInfo info) {
        UUID owner = null;
        TileEntityDispenser dropper = TileEntities.getTileEntity(world, pos, TileEntityDispenser.class);
        if (dropper != null) {
            owner = ((Ownable)dropper).getOwner();
        }
        DispenserShootEvent event = new DispenserShootEvent(world, pos, world.getBlockState(pos).getValue(BlockDispenser.FACING), owner);
        if (MinecraftForge.EVENT_BUS.post(event)) {
            info.cancel();
        }
    }

    @Inject(method = "onBlockPlacedBy", at = @At("RETURN"))
    public void onPlacedBy(World world, BlockPos pos, IBlockState state, EntityLivingBase placer, ItemStack item, CallbackInfo info) {
        TileEntityDispenser dispenser = TileEntities.getTileEntity(world, pos, TileEntityDispenser.class);
        if (dispenser != null && placer instanceof EntityPlayer) {
            ((Ownable)dispenser).setOwner(placer.getUniqueID());
        }
    }
}
