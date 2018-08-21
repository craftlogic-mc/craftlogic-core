package ru.craftlogic.mixin.entity.projectile;

import net.minecraft.entity.item.EntityExpBottle;
import net.minecraft.entity.item.EntityXPOrb;
import net.minecraft.entity.projectile.EntityThrowable;
import net.minecraft.init.Items;
import net.minecraft.init.SoundEvents;
import net.minecraft.item.Item;
import net.minecraft.util.EnumParticleTypes;
import net.minecraft.util.SoundCategory;
import net.minecraft.util.math.RayTraceResult;
import net.minecraft.world.World;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Overwrite;

import java.util.Random;

@Mixin(EntityExpBottle.class)
public abstract class MixinEntityExpBottle extends EntityThrowable {
    public MixinEntityExpBottle(World world) {
        super(world);
    }

    @Override
    @SideOnly(Side.CLIENT)
    public void handleStatusUpdate(byte status) {
        super.handleStatusUpdate(status);
        if (status == 3) {
            double x = this.posX;
            double y = this.posY;
            double z = this.posZ;
            Random rand = this.world.rand;

            for(int i = 0; i < 8; ++i) {
                this.world.spawnParticle(EnumParticleTypes.ITEM_CRACK, x, y, z, rand.nextGaussian() * 0.15D, rand.nextDouble() * 0.2D, rand.nextGaussian() * 0.15D, Item.getIdFromItem(Items.SPLASH_POTION));
            }

            EnumParticleTypes particle = EnumParticleTypes.SPELL;

            for(int i = 0; i < 100; ++i) {
                double d18 = rand.nextDouble() * 4;
                double d21 = rand.nextDouble() * Math.PI * 2;
                double d24 = Math.cos(d21) * d18;
                double d26 = 0.01D + rand.nextDouble() * 0.5;
                double d28 = Math.sin(d21) * d18;

                this.world.spawnParticle(particle, x + d24 * 0.1, y + 0.3, z + d28 * 0.1, d24, d26, d28);
            }

            this.world.playSound(null, x, y, z, SoundEvents.ENTITY_SPLASH_POTION_BREAK, SoundCategory.NEUTRAL, 1F, rand.nextFloat() * 0.1F + 0.9F);
        }
    }
    
    /**
     * @author Radviger
     * @reason Fix splash potion particles
     */
    @Overwrite
    protected void onImpact(RayTraceResult target) {
        if (!this.world.isRemote) {
            int exp = 3 + this.world.rand.nextInt(5) + this.world.rand.nextInt(5);

            while(exp > 0) {
                int split = EntityXPOrb.getXPSplit(exp);
                exp -= split;
                this.world.spawnEntity(new EntityXPOrb(this.world, this.posX, this.posY, this.posZ, split));
            }

            this.world.setEntityState(this, (byte)3);
            this.setDead();
        }
    }
}
