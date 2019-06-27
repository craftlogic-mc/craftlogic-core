package ru.craftlogic.api.item;

import net.minecraft.creativetab.CreativeTabs;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.util.EnumActionResult;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.EnumHand;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.RayTraceResult;
import net.minecraft.util.math.Vec3d;
import net.minecraft.world.World;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;
import ru.craftlogic.api.model.ModelRegistrar;
import ru.craftlogic.api.model.ModelManager;

public class ItemBase extends Item implements ModelRegistrar {
    protected final String name;

    public ItemBase(String name, CreativeTabs tab) {
        this.name = name;
        this.setCreativeTab(tab);
        this.setRegistryName(name);
        this.setTranslationKey(name);
    }

    @Override
    @SideOnly(Side.CLIENT)
    public void registerModel(ModelManager modelManager) {
        modelManager.registerItemModel(this);
    }

    @Override
    public final EnumActionResult onItemUse(EntityPlayer player, World world, BlockPos pos, EnumHand hand, EnumFacing side, float hitX, float hitY, float hitZ) {
        return this.onItemUse(world, pos, new RayTraceResult(new Vec3d(hitX, hitY, hitZ), side, pos), player, hand);
    }

    public EnumActionResult onItemUse(World world, BlockPos pos, RayTraceResult target, EntityPlayer player, EnumHand hand) {
        return EnumActionResult.PASS;
    }

    public static ItemStack damage(ItemStack item, int amount) {
        if (item.getItemDamage() + amount < item.getMaxDamage()) {
            ItemStack result = item.copy();
            result.setItemDamage(item.getItemDamage() + amount);
            return result;
        } else {
            return ItemStack.EMPTY;
        }
    }
}
