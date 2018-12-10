package ru.craftlogic.mixin.block;

import net.minecraft.block.Block;
import net.minecraft.block.BlockDispenser;
import net.minecraft.block.BlockDropper;
import net.minecraft.block.material.Material;
import net.minecraft.creativetab.CreativeTabs;
import net.minecraft.item.ItemStack;
import net.minecraft.tileentity.TileEntityDropper;
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

@Mixin(BlockDropper.class)
public class MixinBlockDropper extends Block {
    public MixinBlockDropper(Material material) {
        super(material);
    }

    @Override
    public void getSubBlocks(CreativeTabs tab, NonNullList<ItemStack> items) {}

    @Inject(method = "dispense", at = @At("HEAD"), cancellable = true)
    protected void onDispense(World world, BlockPos pos, CallbackInfo info) {
        UUID owner = null;
        TileEntityDropper dropper = TileEntities.getTileEntity(world, pos, TileEntityDropper.class);
        if (dropper != null) {
            owner = ((Ownable)dropper).getOwner();
        }
        DispenserShootEvent event = new DispenserShootEvent(world, pos, world.getBlockState(pos).getValue(BlockDispenser.FACING), owner);
        if (MinecraftForge.EVENT_BUS.post(event)) {
            info.cancel();
        }
    }
}
