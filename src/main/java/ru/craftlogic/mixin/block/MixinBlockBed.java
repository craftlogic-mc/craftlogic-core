package ru.craftlogic.mixin.block;

import net.minecraft.block.Block;
import net.minecraft.block.BlockBed;
import net.minecraft.block.material.Material;
import org.spongepowered.asm.mixin.Mixin;

@Mixin(BlockBed.class)
public abstract class MixinBlockBed extends Block {
    public MixinBlockBed(Material material) {
        super(material);
    }
}
