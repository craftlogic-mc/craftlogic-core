package ru.craftlogic.mixin.block;

import net.minecraft.block.Block;
import net.minecraft.block.BlockVine;
import net.minecraft.block.material.Material;
import net.minecraft.block.state.IBlockState;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.IBlockAccess;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Overwrite;

@Mixin(BlockVine.class)
public abstract class MixinBlockVine extends Block {
    public MixinBlockVine(Material material) {
        super(material);
    }

    /**
     * @author Radviger
     * @reason Passable vines
     */
    @Overwrite(remap = false)
    public boolean isLadder(IBlockState state, IBlockAccess world, BlockPos pos, EntityLivingBase entity) {
        return false;
    }
}
