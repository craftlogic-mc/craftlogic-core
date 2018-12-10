package ru.craftlogic.mixin.block;

import net.minecraft.block.Block;
import net.minecraft.block.BlockPistonBase;
import net.minecraft.block.material.Material;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;
import net.minecraftforge.common.MinecraftForge;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;
import ru.craftlogic.api.event.block.PistonMoveEvent;

@Mixin(BlockPistonBase.class)
public abstract class MixinBlockPistonBase extends Block {
    public MixinBlockPistonBase(Material material) {
        super(material);
    }

    @Inject(method = "doMove", at = @At("HEAD"), cancellable = true)
    private void onMove(World world, BlockPos pos, EnumFacing facing, boolean push, CallbackInfoReturnable<Boolean> info) {
        PistonMoveEvent event = new PistonMoveEvent(world, pos, facing, push);
        if (MinecraftForge.EVENT_BUS.post(event)) {
            info.setReturnValue(false);
        }
    }
}
