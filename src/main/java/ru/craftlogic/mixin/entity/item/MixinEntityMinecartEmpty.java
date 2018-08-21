package ru.craftlogic.mixin.entity.item;

import net.minecraft.entity.item.EntityMinecart;
import net.minecraft.entity.item.EntityMinecartEmpty;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.util.EnumHand;
import net.minecraft.util.SoundCategory;
import net.minecraft.world.World;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Overwrite;
import ru.craftlogic.api.CraftSounds;

@Mixin(EntityMinecartEmpty.class)
public abstract class MixinEntityMinecartEmpty extends EntityMinecart {
    public MixinEntityMinecartEmpty(World world) {
        super(world);
    }

    /**
     * @author Radviger
     * @reason Custom minecart sounds
     */
    @Overwrite
    public boolean processInitialInteract(EntityPlayer player, EnumHand hand) {
        if (super.processInitialInteract(player, hand)) {
            return true;
        } else if (player.isSneaking()) {
            return false;
        } else if (this.isBeingRidden()) {
            return true;
        } else {
            if (!this.world.isRemote) {
                if (player.startRiding(this)) {
                    this.world.playSound(null, this.posX, this.posY, this.posZ, CraftSounds.CART_LOADING, SoundCategory.AMBIENT, 1F, 0.7F + this.world.rand.nextFloat() * 0.3F);
                }
            }

            return true;
        }
    }

    @Override
    public void removePassengers() {
        if (!this.getPassengers().isEmpty()) {
            this.world.playSound(null, this.posX, this.posY, this.posZ, CraftSounds.CART_LOADING, SoundCategory.AMBIENT, 1F, 0.7F + this.world.rand.nextFloat() * 0.3F);
        }
        super.removePassengers();
    }
}
