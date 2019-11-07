package ru.craftlogic.mixin.block;

import net.minecraft.block.BlockBush;
import net.minecraft.block.BlockFlower;
import org.spongepowered.asm.mixin.Mixin;

@Mixin(BlockFlower.class)
public abstract class MixinBlockFlower extends BlockBush {

}
