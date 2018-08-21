package ru.craftlogic.api;

import com.google.common.collect.BiMap;
import com.google.common.collect.HashBiMap;
import net.minecraft.block.material.Material;
import net.minecraft.util.ResourceLocation;
import net.minecraftforge.fluids.BlockFluidBase;
import net.minecraftforge.fluids.Fluid;
import net.minecraftforge.fluids.FluidRegistry;
import net.minecraftforge.fml.relauncher.Side;
import ru.craftlogic.api.block.BlockFluid;
import ru.craftlogic.api.fluid.FluidBase;

import static ru.craftlogic.api.CraftAPI.MOD_ID;
import static ru.craftlogic.api.CraftAPI.wrapWithActiveModId;

public class CraftFluids {
    static BiMap<String, Integer> FLUID_NAME_TO_ID = HashBiMap.create();

    static {
        FluidRegistry.enableUniversalBucket();
    }

    public static FluidBase CRUDE_OIL, MILK;

    static void init(Side side) {
        CRUDE_OIL = registerFluid("crude_oil", CraftMaterials.CRUDE_OIL).setViscosity(2000).setDensity(1000);
        MILK = registerFluid("milk", CraftMaterials.MILK);
    }

    public static FluidBase registerFluid(String name, Material material) {
        return registerFluid(name, material, 0xFFFFFF);
    }

    public static FluidBase registerFluid(String name, Material material, int color) {
        ResourceLocation still = wrapWithActiveModId("blocks/fluid/" + name + "_still", MOD_ID);
        ResourceLocation flowing = wrapWithActiveModId("blocks/fluid/" + name + "_flow", MOD_ID);
        FluidBase fluid = registerFluid(new FluidBase(wrapWithActiveModId(name, MOD_ID), still, flowing, color), true);
        BlockFluidBase block = new BlockFluid(name, fluid, material);
        fluid.setBlock(block);
        CraftBlocks.registerBlock(block);
        return fluid;
    }

    public static <F extends FluidBase> F registerFluid(F fluid, boolean addBucket) {
        FluidRegistry.registerFluid(fluid);
        if (addBucket) {
            FluidRegistry.addBucketForFluid(fluid);
        }
        return fluid;
    }

    public static int getFluidId(Fluid fluid) {
        if (fluid == null || !FLUID_NAME_TO_ID.containsKey(fluid.getName())) {
            return -1;
        } else {
            return FLUID_NAME_TO_ID.get(fluid.getName());
        }
    }

    public static Fluid getFluidById(int id) {
        if (id >= 0) {
            String name = FLUID_NAME_TO_ID.inverse().get(id);
            if (name != null) {
                return FluidRegistry.getFluid(name);
            }
        }
        return null;
    }
}
