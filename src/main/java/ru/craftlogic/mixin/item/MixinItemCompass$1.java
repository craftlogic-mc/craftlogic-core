package ru.craftlogic.mixin.item;

import com.google.common.util.concurrent.AtomicDouble;
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
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.atomic.AtomicInteger;

@Mixin(targets = "net/minecraft/item/ItemCompass$1")
public abstract class MixinItemCompass$1 implements IItemPropertyGetter {
    @SideOnly(Side.CLIENT)
    private Map<UUID, AtomicInteger> updateCountdown;
    @SideOnly(Side.CLIENT)
    private Map<UUID, AtomicDouble> interference;
    @SideOnly(Side.CLIENT)
    @Shadow
    long lastUpdateTick;

    /**
     * @author Radviger
     * @reason Extended compass features
     */
    @Overwrite
    @SideOnly(Side.CLIENT)
    public float apply(ItemStack item, @Nullable World world, @Nullable EntityLivingBase entity) {
        if (entity == null && !item.isOnItemFrame()) {
            return 0F;
        } else {
            if (updateCountdown == null) updateCountdown = new HashMap<>();
            if (interference == null) interference = new HashMap<>();

            boolean handheld = entity != null;
            Entity holder = handheld ? entity : item.getItemFrame();
            if (world == null) {
                world = holder.world;
            }

            BlockPos targetPos = world.getSpawnPoint();

            double sensitivityDistanceSq = 8.0 * 8.0;
            double angle = this.getAngleToPos(targetPos, holder);

            if (!world.provider.isSurfaceWorld()) {
                angle = Math.random();
            } else {
                UUID id = holder.getUniqueID();
                AtomicInteger uc = this.updateCountdown.computeIfAbsent(id, k -> new AtomicInteger(0));
                AtomicDouble itf = this.interference.computeIfAbsent(id, k -> new AtomicDouble(0));
                if (uc.getAndIncrement() >= 15) {
                    uc.set(0);
                    itf.set(0);
                    BlockPos pos = new BlockPos(holder);

                    for (BlockPos.MutableBlockPos p : BlockPos.getAllInBoxMutable(pos.add(-15, -15, -15), pos.add(15, 15, 15))) {
                        if (world.isBlockLoaded(p)) {
                            double distance = pos.distanceSq(p);
                            IBlockState state = world.getBlockState(p);
                            if (state.getBlock() == Blocks.IRON_ORE) {
                                itf.addAndGet(Math.sqrt(distance > 0 ? sensitivityDistanceSq / distance : sensitivityDistanceSq));
                            }
                        }
                    }
                }
                if (itf.get() > 0) {
                    double sin = Math.sin((double)this.lastUpdateTick) / 50.0;
                    angle += sin * itf.get();
                }
            }

            if (handheld) {
                angle = this.wobble(world, angle);
            }

            return MathHelper.positiveModulo((float)angle, 1);
        }
    }

    @SideOnly(Side.CLIENT)
    @Shadow
    private double wobble(World world, double angle) { return 0; }

    @SideOnly(Side.CLIENT)
    @Shadow
    private double getFrameRotation(EntityItemFrame frame) { return 0; }

    @SideOnly(Side.CLIENT)
    private double getAngleToPos(BlockPos pos, Entity holder) {
        double yaw = holder instanceof EntityItemFrame ? this.getFrameRotation((EntityItemFrame) holder) : (double) holder.rotationYaw;
        double target = Math.atan2(pos.getZ() - holder.posZ, pos.getX() - holder.posX) / (Math.PI * 2); /*[-0.5, 0.5]*/
        return 0.5 - (yaw / 360 - 0.25 - target);
    }
}
