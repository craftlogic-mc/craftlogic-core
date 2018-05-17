package ru.craftlogic.mixin.block;

import net.minecraft.block.Block;
import net.minecraft.block.BlockStem;
import net.minecraft.block.material.Material;
import org.spongepowered.asm.mixin.Mixin;

@Mixin(BlockStem.class)
public class MixinBlockStem extends Block {
    public MixinBlockStem(Material material) {
        super(material);
    }
}
