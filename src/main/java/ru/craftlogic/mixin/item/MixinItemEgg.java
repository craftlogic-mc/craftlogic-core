package ru.craftlogic.mixin.item;

import net.minecraft.client.util.ITooltipFlag;
import net.minecraft.item.Item;
import net.minecraft.item.ItemEgg;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.text.translation.I18n;
import net.minecraft.world.World;
import net.minecraftforge.fml.common.registry.EntityEntry;
import net.minecraftforge.fml.common.registry.ForgeRegistries;
import org.spongepowered.asm.mixin.Mixin;

import javax.annotation.Nullable;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.lang.reflect.Modifier;
import java.util.List;

@Mixin(ItemEgg.class)
public class MixinItemEgg extends Item {
    @Override
    public void addInformation(ItemStack stack, @Nullable World world, List<String> info, ITooltipFlag flag) {
        NBTTagCompound compound = stack.getSubCompound("BirdData");
        if (compound != null) {
            EntityEntry entry = ForgeRegistries.ENTITIES.getValue(new ResourceLocation(compound.getString("id")));
            if (entry != null) {
                info.add(I18n.translateToLocalFormatted("tooltip.bird", entry.getName()));
                try {
                    Method m = entry.getEntityClass().getMethod("addEggInfo",
                        NBTTagCompound.class,
                        List.class,
                        World.class
                    );
                    if ((m.getModifiers() & Modifier.STATIC) > 0) {
                        m.setAccessible(true);
                        m.invoke(null, compound, info, world);
                    }
                } catch (NoSuchMethodException | IllegalAccessException | InvocationTargetException ignored) { }
            }
        }
    }
}
