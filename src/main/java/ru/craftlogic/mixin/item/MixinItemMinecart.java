package ru.craftlogic.mixin.item;

import net.minecraft.block.BlockRailBase;
import net.minecraft.block.state.IBlockState;
import net.minecraft.creativetab.CreativeTabs;
import net.minecraft.entity.item.EntityMinecart;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.init.Items;
import net.minecraft.item.Item;
import net.minecraft.item.ItemMinecart;
import net.minecraft.item.ItemStack;
import net.minecraft.util.*;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Overwrite;
import org.spongepowered.asm.mixin.Shadow;
import ru.craftlogic.api.CraftSounds;

@Mixin(ItemMinecart.class)
public class MixinItemMinecart extends Item {
    @Shadow @Final
    private EntityMinecart.Type minecartType;

    /**
     * @author Radviger
     * @reason Custom minecart sounds
     */
    @Overwrite
    public EnumActionResult onItemUse(EntityPlayer player, World world, BlockPos pos, EnumHand hand, EnumFacing side, float hitX, float hitY, float hitZ) {
        IBlockState state = world.getBlockState(pos);
        if (!BlockRailBase.isRailBlock(state)) {
            return EnumActionResult.FAIL;
        } else {
            ItemStack heldItem = player.getHeldItem(hand);
            if (!world.isRemote) {
                BlockRailBase.EnumRailDirection dir = state.getBlock() instanceof BlockRailBase ? ((BlockRailBase)state.getBlock()).getRailDirection(world, pos, state, null) : BlockRailBase.EnumRailDirection.NORTH_SOUTH;
                double y = 0.0;

                if (dir.isAscending()) {
                    y = 0.5;
                }

                EntityMinecart cart = EntityMinecart.create(world, (double)pos.getX() + 0.5, (double)pos.getY() + 0.0625 + y, (double)pos.getZ() + 0.5, this.minecartType);
                if (heldItem.hasDisplayName()) {
                    cart.setCustomNameTag(heldItem.getDisplayName());
                }

                world.playSound(null, pos, CraftSounds.CART_PLACE, SoundCategory.PLAYERS, 1F, 0.7F + world.rand.nextFloat() * 0.3F);
                world.spawnEntity(cart);
            }

            heldItem.shrink(1);
            return EnumActionResult.SUCCESS;
        }
    }

    @Override
    public void getSubItems(CreativeTabs tab, NonNullList<ItemStack> items) {
        if (this.isInCreativeTab(tab)) {
            String id = this.getRegistryName().toString();
            if (id.startsWith("minecraft:") && id.endsWith("minecart")) {
                if (this == Items.MINECART) {
                    items.add(new ItemStack(Items.MINECART));
                    items.add(new ItemStack(Items.CHEST_MINECART));
                    items.add(new ItemStack(Items.FURNACE_MINECART));
                    items.add(new ItemStack(Items.HOPPER_MINECART));
                    items.add(new ItemStack(Items.TNT_MINECART));
                }
            } else {
                items.add(new ItemStack(this));
            }
        }
    }
}
