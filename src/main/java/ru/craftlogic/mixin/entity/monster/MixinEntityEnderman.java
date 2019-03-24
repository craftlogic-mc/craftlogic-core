package ru.craftlogic.mixin.entity.monster;

import net.minecraft.entity.monster.EntityEnderman;
import net.minecraft.entity.monster.EntityMob;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.init.Blocks;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.util.math.Vec3d;
import net.minecraft.world.World;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Overwrite;

@Mixin(EntityEnderman.class)
public abstract class MixinEntityEnderman extends EntityMob {
    public MixinEntityEnderman(World world) {
        super(world);
    }

    /**
     * @author Radviger
     * @reason
     */
    @Overwrite
    private boolean shouldAttackPlayer(EntityPlayer player) {
        ItemStack helm = player.inventory.armorInventory.get(3);
        if (helm.getItem() == Item.getItemFromBlock(Blocks.LIT_PUMPKIN)) {
            return false;
        } else {
            Vec3d look = player.getLook(1.0F).normalize();
            Vec3d eyeHeight = new Vec3d(this.posX - player.posX, this.getEntityBoundingBox().minY + (double)this.getEyeHeight() - (player.posY + (double)player.getEyeHeight()), this.posZ - player.posZ);
            double d0 = eyeHeight.length();
            eyeHeight = eyeHeight.normalize();
            double distance = look.dotProduct(eyeHeight);
            return distance > 1.0D - 0.025D / d0 && player.canEntityBeSeen(this);
        }
    }
}
