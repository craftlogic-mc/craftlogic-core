package ru.craftlogic.common.block;

import net.minecraft.block.SoundType;
import net.minecraft.block.material.Material;
import net.minecraft.creativetab.CreativeTabs;
import ru.craftlogic.api.block.BlockBase;

public class BlockStoneBrickLight extends BlockBase {
    public BlockStoneBrickLight() {
        super(Material.ROCK, "stonebrick_light", 1.5F, CreativeTabs.BUILDING_BLOCKS);
        setResistance(10F);
        setLightLevel(1F);
        setSoundType(SoundType.STONE);
    }
}
