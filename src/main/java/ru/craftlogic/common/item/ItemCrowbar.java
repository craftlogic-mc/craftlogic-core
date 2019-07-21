package ru.craftlogic.common.item;

import com.google.common.collect.Multimap;
import net.minecraft.block.BlockDoor;
import net.minecraft.block.state.IBlockState;
import net.minecraft.creativetab.CreativeTabs;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.entity.SharedMonsterAttributes;
import net.minecraft.entity.ai.attributes.AttributeModifier;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.init.Blocks;
import net.minecraft.inventory.EntityEquipmentSlot;
import net.minecraft.item.EnumAction;
import net.minecraft.item.ItemStack;
import net.minecraft.util.ActionResult;
import net.minecraft.util.EnumActionResult;
import net.minecraft.util.EnumHand;
import net.minecraft.util.math.RayTraceResult;
import net.minecraft.util.math.Vec3d;
import net.minecraft.world.World;
import ru.craftlogic.api.item.ItemBase;
import ru.craftlogic.api.world.Location;

import static net.minecraft.util.EnumFacing.DOWN;
import static net.minecraft.util.EnumFacing.UP;

public class ItemCrowbar extends ItemBase {
    public ItemCrowbar() {
        super("crowbar", CreativeTabs.TOOLS);
        this.setMaxStackSize(1);
    }

    @Override
    public ActionResult<ItemStack> onItemRightClick(World world, EntityPlayer player, EnumHand hand) {
        ItemStack heldItem = player.getHeldItem(hand);
        player.setActiveHand(hand);
        return new ActionResult<>(EnumActionResult.SUCCESS, heldItem);
    }

    @Override
    public EnumAction getItemUseAction(ItemStack item) {
        return EnumAction.BLOCK;
    }

    @Override
    public int getMaxItemUseDuration(ItemStack item) {
        return 200;
    }

    @Override
    public void onPlayerStoppedUsing(ItemStack item, World world, EntityLivingBase entity, int duration) {
        if (!world.isRemote && entity.getItemInUseCount() == this.getMaxItemUseDuration(item)) {
            System.out.println("Trying to open a door");
            double distance = 4.5;
            if (entity instanceof EntityPlayer && ((EntityPlayer) entity).capabilities.isCreativeMode) {
                distance = 5.0;
            }
            Vec3d start = entity.getPositionEyes(1F);
            Vec3d look = entity.getLook(1F);
            Vec3d end = start.add(look.x * distance, look.y * distance, look.z * distance);
            RayTraceResult target = world.rayTraceBlocks(start, end, false, false, true);
            if (target != null && target.typeOfHit == RayTraceResult.Type.BLOCK) {
                System.out.println("found block");
                Location loc = new Location(world, target.getBlockPos());
                if (loc.isSameBlock(Blocks.IRON_DOOR)) {
                    System.out.println("iron door");
                    IBlockState state = loc.getBlockState();
                    boolean lower = state.getValue(BlockDoor.HALF) == BlockDoor.EnumDoorHalf.LOWER;
                    loc = lower ? loc : loc.offset(DOWN);
                    state = loc.getBlockState();
                    if (state.getBlock() == Blocks.IRON_DOOR) {
                        state = state.cycleProperty(BlockDoor.OPEN);
                        loc.setBlockState(state, 10);
                        world.markBlockRangeForRenderUpdate(loc.getPos(), loc.offset(lower ? UP : DOWN).getPos());
                        loc.playEvent(state.getValue(BlockDoor.OPEN) ? 1005 : 1011, 0);
                    }
                }
            }
        }
    }

    @Override
    public Multimap<String, AttributeModifier> getItemAttributeModifiers(EntityEquipmentSlot slot) {
        Multimap<String, AttributeModifier> attributes = super.getItemAttributeModifiers(slot);
        if (slot == EntityEquipmentSlot.MAINHAND) {
            attributes.put(
                SharedMonsterAttributes.ATTACK_DAMAGE.getName(),
                new AttributeModifier(ATTACK_DAMAGE_MODIFIER, "Weapon modifier", 5.5, 0)
            );
            attributes.put(
                SharedMonsterAttributes.ATTACK_SPEED.getName(),
                new AttributeModifier(ATTACK_SPEED_MODIFIER, "Weapon modifier", -2.4000000953674316D, 0)
            );
        }

        return attributes;
    }
}
