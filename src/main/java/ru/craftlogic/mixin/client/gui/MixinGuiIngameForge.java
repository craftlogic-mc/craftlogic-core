package ru.craftlogic.mixin.client.gui;

import net.minecraft.block.material.Material;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.FontRenderer;
import net.minecraft.client.gui.GuiIngame;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraftforge.client.GuiIngameForge;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Constant;
import org.spongepowered.asm.mixin.injection.ModifyConstant;
import org.spongepowered.asm.mixin.injection.Redirect;

@Mixin(GuiIngameForge.class)
public abstract class MixinGuiIngameForge extends GuiIngame {
    @Shadow private FontRenderer fontrenderer;

    @Shadow @Final private static int WHITE;

    public MixinGuiIngameForge(Minecraft mc) {
        super(mc);
    }

    /**
     * @author Radviger
     * @reason Fixed glitching message overlay
     */
    @ModifyConstant(method = "renderRecordOverlay", constant = @Constant(intValue = 0, ordinal = 1, expandZeroConditions = Constant.Condition.GREATER_THAN_ZERO), remap = false)
    private int minOpacity(int old) {
        return 8;
    }

    @Redirect(method = "renderAir", at = @At(value = "INVOKE", target = "Lnet/minecraft/entity/player/EntityPlayer;isInsideOfMaterial(Lnet/minecraft/block/material/Material;)Z"))
    protected boolean onCheckAirSupply(EntityPlayer player, Material material) {
        return player.getAir() < 300 || player.isInsideOfMaterial(material);
    }
}
