package ru.craftlogic.mixin.item;

import net.minecraft.block.Block;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.ItemBlock;
import net.minecraft.item.ItemLilyPad;
import net.minecraft.item.ItemStack;
import net.minecraft.util.ActionResult;
import net.minecraft.util.EnumActionResult;
import net.minecraft.util.EnumHand;
import net.minecraft.world.World;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;
import ru.craftlogic.CraftConfig;

@Mixin(ItemLilyPad.class)
public abstract class MixinItemLilyPad extends ItemBlock {
    public MixinItemLilyPad(Block block) {
        super(block);
    }

    @Inject(method = "onItemRightClick", at = @At("HEAD"), cancellable = true)
    public void disablePlacing(World world, EntityPlayer player, EnumHand hand, CallbackInfoReturnable<ActionResult<ItemStack>> cir) {
        if (!CraftConfig.items.enableFlowerPlacing) {
            cir.setReturnValue(new ActionResult<>(EnumActionResult.PASS, player.getHeldItem(hand)));
        }
    }
}
