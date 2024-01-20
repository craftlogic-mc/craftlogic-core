package ru.craftlogic.mixin.entity.monster;

import net.minecraft.block.Block;
import net.minecraft.entity.monster.EntityEnderman;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.init.Blocks;
import org.objectweb.asm.Opcodes;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Redirect;

@Mixin(EntityEnderman.class)
public class MixinEntityEnderman {
    @Redirect(method = "shouldAttackPlayer", at = @At(value = "FIELD", target = "Lnet/minecraft/init/Blocks;PUMPKIN:Lnet/minecraft/block/Block;", opcode = Opcodes.GETSTATIC))
    private Block maskBlock(EntityPlayer target) {
        return Blocks.LIT_PUMPKIN;
    }
}
