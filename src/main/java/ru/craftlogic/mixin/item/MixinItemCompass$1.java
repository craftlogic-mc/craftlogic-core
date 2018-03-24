package ru.craftlogic.mixin.item;

import net.minecraft.block.state.IBlockState;
import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.entity.item.EntityItemFrame;
import net.minecraft.init.Blocks;
import net.minecraft.item.IItemPropertyGetter;
import net.minecraft.item.ItemStack;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.MathHelper;
import net.minecraft.world.World;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Overwrite;
import org.spongepowered.asm.mixin.Shadow;

import javax.annotation.Nullable;

@Mixin(targets = "net/minecraft/item/ItemCompass$1")
public abstract class MixinItemCompass$1 implements IItemPropertyGetter {
    @SideOnly(Side.CLIENT)
    private BlockPos lastOre;
    @SideOnly(Side.CLIENT)
    private int updateCountdown, interference;

    @Overwrite
    @SideOnly(Side.CLIENT)
    public float apply(ItemStack item, @Nullable World world, @Nullable EntityLivingBase entity) {
        if (entity == null && !item.isOnItemFrame()) {
            return 0F;
        } else {
            boolean handheld = entity != null;
            Entity holder = handheld ? entity : item.getItemFrame();
            if (world == null) {
                world = holder.world;
            }

            if (updateCountdown++ >= 10) {
                updateCountdown = 0;
                interference = 0;
                lastOre = null;
                BlockPos pos = new BlockPos(holder);

                for (BlockPos.MutableBlockPos p : BlockPos.getAllInBoxMutable(pos.add(-10, -10, -10), pos.add(10, 10, 10))) {
                    if (world.isBlockLoaded(p)) {
                        IBlockState state = world.getBlockState(p);
                        if (state.getBlock() == Blocks.IRON_ORE) {
                            ++interference;
                        }
                        if (interference > 0 && world.rand.nextDouble() >= (1.0 / (double)interference)) {
                            lastOre = p.toImmutable();
                        }
                    }
                }
            }

            double angle;
            if (world.rand.nextDouble() >= (1.0 / (double)interference) && lastOre != null) {
                angle = this.getPosToAngle(lastOre, holder);
            } else if (world.provider.isSurfaceWorld() && interference < 15) {
                angle = this.getPosToAngle(world.getSpawnPoint(), holder);
            } else {
                angle = Math.random();
            }

            if (handheld) {
                angle = this.wobble(world, angle);
            }

            return MathHelper.positiveModulo((float)angle, 1F);
        }
    }

    @SideOnly(Side.CLIENT)
    @Shadow
    private double wobble(World world, double angle) { return 0; }

    @SideOnly(Side.CLIENT)
    @Shadow
    private double getFrameRotation(EntityItemFrame frame) { return 0; }

    @SideOnly(Side.CLIENT)
    private double getPosToAngle(BlockPos pos, Entity holder) {
        double yaw = holder instanceof EntityItemFrame ? this.getFrameRotation((EntityItemFrame) holder) : (double) holder.rotationYaw;
        yaw = MathHelper.positiveModulo(yaw / 360, 1);
        double targetAngle = Math.atan2((double)pos.getZ() - holder.posZ, (double)pos.getX() - holder.posX) / (Math.PI * 2);
        return 0.5 - (yaw - 0.25 - targetAngle);
    }
}
