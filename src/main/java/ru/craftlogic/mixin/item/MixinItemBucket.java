package ru.craftlogic.mixin.item;

import net.minecraft.creativetab.CreativeTabs;
import net.minecraft.init.Items;
import net.minecraft.item.Item;
import net.minecraft.item.ItemBucket;
import net.minecraft.item.ItemStack;
import net.minecraft.util.NonNullList;
import net.minecraftforge.common.ForgeModContainer;
import net.minecraftforge.fluids.Fluid;
import net.minecraftforge.fluids.FluidRegistry;
import net.minecraftforge.fluids.FluidStack;
import net.minecraftforge.fluids.UniversalBucket;
import net.minecraftforge.fluids.capability.IFluidHandlerItem;
import net.minecraftforge.fluids.capability.wrappers.FluidBucketWrapper;
import org.spongepowered.asm.mixin.Mixin;

@Mixin(ItemBucket.class)
public class MixinItemBucket extends Item {
    @Override
    public void getSubItems(CreativeTabs tab, NonNullList<ItemStack> items) {
        if (this.isInCreativeTab(tab)) {
            String id = this.getRegistryName().toString();
            if (id.startsWith("minecraft:") && id.endsWith("bucket")) {
                if (this == Items.BUCKET) {
                    items.add(new ItemStack(Items.BUCKET));
                    items.add(new ItemStack(Items.WATER_BUCKET));
                    items.add(new ItemStack(Items.LAVA_BUCKET));
                    items.add(new ItemStack(Items.MILK_BUCKET));
                    UniversalBucket universalBucket = ForgeModContainer.getInstance().universalBucket;
                    for (Fluid fluid : FluidRegistry.getRegisteredFluids().values()) {
                        if (fluid != FluidRegistry.WATER && fluid != FluidRegistry.LAVA && !fluid.getName().equals("milk")) {
                            FluidStack fs = new FluidStack(fluid, universalBucket.getCapacity());
                            ItemStack stack = new ItemStack(universalBucket);
                            IFluidHandlerItem fluidHandler = new FluidBucketWrapper(stack);
                            if (fluidHandler.fill(fs, true) == fs.amount) {
                                ItemStack filled = fluidHandler.getContainer();
                                items.add(filled);
                            }
                        }
                    }
                }
            } else {
                items.add(new ItemStack(this));
            }
        }
    }
}
