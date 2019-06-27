package ru.craftlogic.mixin.block;

import net.minecraft.block.Block;
import net.minecraft.block.BlockWeb;
import net.minecraft.block.SoundType;
import net.minecraft.block.material.Material;
import net.minecraft.block.state.IBlockState;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.stats.StatList;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Overwrite;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import javax.annotation.Nullable;

@Mixin(BlockWeb.class)
public class MixinBlockWeb extends Block {
    public MixinBlockWeb(Material material) {
        super(material);
    }

    @Inject(method = "<init>", at = @At("RETURN"))
    public void constructor(CallbackInfo info) {
        this.setSoundType(SoundType.SLIME);
    }

    /**
     * @author Radviger
     * @reason Custom cobweb
     */
    @Overwrite
    protected boolean canSilkHarvest() {
        return false;
    }

    /**
     * @author Radviger
     * @reason Custom cobweb
     */
    @Overwrite
    public void harvestBlock(World world, EntityPlayer player, BlockPos pos, IBlockState state, @Nullable TileEntity tileEntity, ItemStack item) {
        if (!world.isRemote) {
            player.addStat(StatList.getBlockStats(this));
            spawnAsEntity(world, pos, new ItemStack(Item.getItemFromBlock(this), 1));
        }
    }
}
