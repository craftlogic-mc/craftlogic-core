package ru.craftlogic.mixin.block;

import net.minecraft.block.BlockBush;
import net.minecraft.block.BlockLilyPad;
import net.minecraft.block.state.IBlockState;
import net.minecraft.entity.Entity;
import net.minecraft.util.SoundCategory;
import net.minecraft.util.math.AxisAlignedBB;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Overwrite;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import ru.craftlogic.CraftConfig;
import ru.craftlogic.api.CraftSounds;

import javax.annotation.Nullable;
import java.util.List;
import java.util.Random;

@Mixin(BlockLilyPad.class)
public abstract class MixinBlockLilyPad extends BlockBush {

    @Inject(method = "addCollisionBoxToList", at = @At("HEAD"), cancellable = true)
    public void onAddCollisions(IBlockState state, World worldIn, BlockPos pos, AxisAlignedBB entityBox, List<AxisAlignedBB> collidingBoxes, Entity entityIn, boolean isActualState, CallbackInfo ci) {
        if (!CraftConfig.blocks.enableLilyPadCollisions) {
            ci.cancel();
        }
    }


    @Override
    public void updateTick(World world, BlockPos pos, IBlockState state, Random rand) {
        super.updateTick(world, pos, state, rand);
        if (!world.isRemote && !world.isDaytime() && world.canBlockSeeSky(pos.up())) {
            world.playSound(null, pos, CraftSounds.CRITTER, SoundCategory.AMBIENT, 1, 0.9F + 0.2F * rand.nextFloat());
        }
    }
}
