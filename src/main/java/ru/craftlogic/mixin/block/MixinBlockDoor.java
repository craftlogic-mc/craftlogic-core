package ru.craftlogic.mixin.block;

import net.minecraft.block.BlockDoor;
import net.minecraft.block.material.Material;
import org.spongepowered.asm.mixin.Mixin;

@Mixin(BlockDoor.class)
public class MixinBlockDoor extends BlockDoor {
    protected MixinBlockDoor(Material material) {
        super(material);
    }
}
