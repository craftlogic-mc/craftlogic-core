package ru.craftlogic.common.block;

import net.minecraft.block.BlockFalling;
import net.minecraft.block.SoundType;
import net.minecraft.block.material.Material;
import net.minecraft.block.state.IBlockState;
import net.minecraft.creativetab.CreativeTabs;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;

public class BlockCobblestone extends BlockFalling {
    public BlockCobblestone() {
        super(Material.ROCK);
        this.setHardness(2F);
        this.setResistance(10F);
        this.setSoundType(SoundType.STONE);
        this.setUnlocalizedName("stonebrick");
        this.setCreativeTab(CreativeTabs.BUILDING_BLOCKS);
    }

    @SideOnly(Side.CLIENT)
    @Override
    public int getDustColor(IBlockState state) {
        return 0xFF5D5A59;
    }
}
