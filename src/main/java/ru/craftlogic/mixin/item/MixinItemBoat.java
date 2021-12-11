package ru.craftlogic.mixin.item;

import net.minecraft.block.Block;
import net.minecraft.entity.Entity;
import net.minecraft.entity.item.EntityBoat;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.init.Blocks;
import net.minecraft.item.Item;
import net.minecraft.item.ItemBoat;
import net.minecraft.item.ItemStack;
import net.minecraft.stats.StatList;
import net.minecraft.util.ActionResult;
import net.minecraft.util.EnumActionResult;
import net.minecraft.util.EnumHand;
import net.minecraft.util.math.AxisAlignedBB;
import net.minecraft.util.math.RayTraceResult;
import net.minecraft.util.math.Vec3d;
import net.minecraft.world.World;
import net.minecraftforge.common.MinecraftForge;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Overwrite;
import org.spongepowered.asm.mixin.Shadow;
import ru.craftlogic.api.event.player.PlayerPlaceBoatEvent;

import java.util.List;

@Mixin(ItemBoat.class)
public class MixinItemBoat {
    @Shadow @Final private EntityBoat.Type type;

    /**
     * @author Radviger
     * @reason Boat placement event
     */
    @Overwrite
    public ActionResult<ItemStack> onItemRightClick(World world, EntityPlayer player, EnumHand hand) {
        ItemStack heldItem = player.getHeldItem(hand);
        double reachDistance = 5;
        float partialTicks = 1;
        Vec3d start = player.getPositionEyes(partialTicks);
        Vec3d look = player.getLook(partialTicks);
        Vec3d end = start.add(look.x * reachDistance, look.y * reachDistance, look.z * reachDistance);
        RayTraceResult target = world.rayTraceBlocks(start, end, true);
        if (target == null) {
            return new ActionResult<>(EnumActionResult.PASS, heldItem);
        } else {
            boolean collided = false;
            List<Entity> entities = world.getEntitiesWithinAABBExcludingEntity(player, player.getEntityBoundingBox().expand(look.x * reachDistance, look.y * reachDistance, look.z * reachDistance).grow(1.0D));

            for (Entity entity : entities) {
                if (entity.canBeCollidedWith()) {
                    AxisAlignedBB box = entity.getEntityBoundingBox().grow(entity.getCollisionBorderSize());
                    if (box.contains(start)) {
                        collided = true;
                    }
                }
            }

            if (collided || target.typeOfHit != RayTraceResult.Type.BLOCK) {
                return new ActionResult<>(EnumActionResult.PASS, heldItem);
            } else {
                Block block = world.getBlockState(target.getBlockPos()).getBlock();
                boolean water = block == Blocks.WATER || block == Blocks.FLOWING_WATER;
                if (!MinecraftForge.EVENT_BUS.post(new PlayerPlaceBoatEvent(player, target, water))) {
                    Vec3d pos = target.hitVec;
                    EntityBoat boat = new EntityBoat(world, pos.x, water ? pos.y - 0.12D : pos.y, pos.z);
                    boat.setBoatType(type);
                    boat.rotationYaw = player.rotationYaw;
                    if (world.getCollisionBoxes(boat, boat.getEntityBoundingBox().grow(-0.1D)).isEmpty()) {
                        if (!world.isRemote) {
                            world.spawnEntity(boat);
                        }

                        if (!player.capabilities.isCreativeMode) {
                            heldItem.shrink(1);
                        }

                        player.addStat(StatList.getObjectUseStats((Item) (Object) this));
                        return new ActionResult<>(EnumActionResult.SUCCESS, heldItem);
                    }
                }
                return new ActionResult<>(EnumActionResult.FAIL, heldItem);
            }
        }
    }
}
