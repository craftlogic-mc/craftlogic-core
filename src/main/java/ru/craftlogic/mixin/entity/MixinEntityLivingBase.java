package ru.craftlogic.mixin.entity;

import net.minecraft.enchantment.EnchantmentHelper;
import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.init.Items;
import net.minecraft.init.SoundEvents;
import net.minecraft.item.Item;
import net.minecraft.item.ItemArmor;
import net.minecraft.item.ItemShield;
import net.minecraft.item.ItemStack;
import net.minecraft.util.DamageSource;
import net.minecraft.util.EnumHand;
import net.minecraft.util.SoundEvent;
import net.minecraft.util.math.Vec3d;
import net.minecraft.world.World;
import org.objectweb.asm.Opcodes;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Overwrite;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Constant;
import org.spongepowered.asm.mixin.injection.ModifyConstant;
import org.spongepowered.asm.mixin.injection.Redirect;
import ru.craftlogic.api.entity.Creature;

@Mixin(EntityLivingBase.class)
public abstract class MixinEntityLivingBase extends Entity implements Creature {
    @Shadow
    protected boolean isJumping;

    public MixinEntityLivingBase(World world) {
        super(world);
    }

    @Override
    public boolean isJumping() {
        return isJumping;
    }

    @ModifyConstant(method = "onEntityUpdate", constant = @Constant(intValue = 300))
    public int onRestoreAirSupply(int old) {
        return increaseAirSupply(getAir());
    }

    @Override
    public int increaseAirSupply(int air) {
        if (air < 300) {
            int i = EnchantmentHelper.getRespirationModifier((EntityLivingBase) (Object) this);
            return Math.min(300, i > 0 && this.rand.nextInt(i + 1) > 0 ? air + 8 : air + 4);
        } else {
            return 300;
        }
    }

    /**
     * @author Pudo
     * @reason
     */

    @Overwrite
    protected void playEquipSound(ItemStack stack) {
        if (!stack.isEmpty()) {
            Item item = stack.getItem();
            if (item instanceof ItemArmor) {
                this.playSound(((ItemArmor)item).getArmorMaterial().getSoundEvent(),1.0F, 1.0F);
            } else if (item == Items.ELYTRA) {
                this.playSound(SoundEvents.ITEM_ARMOR_EQIIP_ELYTRA,1.0F, 1.0F);
            }
        }
    }

    /**
     * @author
     * @reason
     */
    @Overwrite
    private boolean canBlockDamageSource(DamageSource damageSourceIn) {
        EntityLivingBase entity = (EntityLivingBase) ((Object) this);
        if (entity instanceof EntityPlayer) {
            if (entity.getHeldItem(EnumHand.OFF_HAND).getItem() instanceof ItemShield || entity.getHeldItem(EnumHand.MAIN_HAND).getItem() instanceof ItemShield) {
                if (!damageSourceIn.isUnblockable() && entity.isActiveItemStackBlocking()) {
                    Vec3d vec3d = damageSourceIn.getDamageLocation();
                    if (vec3d != null) {
                        Vec3d vec3d1 = this.getLook(1.0F);
                        Vec3d vec3d2 = vec3d.subtractReverse(new Vec3d(this.posX, this.posY, this.posZ)).normalize();
                        vec3d2 = new Vec3d(vec3d2.x, 0.0D, vec3d2.z);
                        return vec3d2.dotProduct(vec3d1) < 0.0D;
                    }
                }
            }
        }
        return false;
    }


    @Redirect(method = "attackEntityFrom", at = @At(value = "FIELD", target = "Lnet/minecraft/entity/EntityLivingBase;lastDamage:F", opcode = Opcodes.GETFIELD))
    public float lastDamage(EntityLivingBase instance) {
        return Float.MAX_VALUE;
    }
}
