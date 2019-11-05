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
import org.spongepowered.asm.mixin.Overwrite;

@Mixin(ItemLilyPad.class)
public abstract class MixinItemLilyPad extends ItemBlock {
    public MixinItemLilyPad(Block block) {
        super(block);
    }

    /**
     * @author Radviger
     * @reason Disable waterlily placing
     */
    @Overwrite
    public ActionResult<ItemStack> onItemRightClick(World world, EntityPlayer player, EnumHand hand) {
        return new ActionResult<>(EnumActionResult.PASS, player.getHeldItem(hand));
    }
}
