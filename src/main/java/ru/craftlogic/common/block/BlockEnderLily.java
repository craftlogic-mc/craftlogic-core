package ru.craftlogic.common.block;

import net.minecraft.block.material.Material;
import net.minecraft.creativetab.CreativeTabs;
import ru.craftlogic.api.block.BlockBase;

public class BlockEnderLily extends BlockBase {
    public BlockEnderLily() {
        super(Material.GRASS, "ender_lily", 0.5F, CreativeTabs.MATERIALS);
    }
}
