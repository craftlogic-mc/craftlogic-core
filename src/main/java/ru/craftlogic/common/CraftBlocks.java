package ru.craftlogic.common;

import net.minecraft.block.Block;
import net.minecraft.block.material.MapColor;
import net.minecraft.block.material.Material;
import net.minecraft.block.material.MaterialLiquid;
import net.minecraft.util.ResourceLocation;
import net.minecraftforge.fluids.Fluid;
import net.minecraftforge.fluids.FluidRegistry;
import ru.craftlogic.api.block.BlockFluid;
import ru.craftlogic.common.block.*;

import static ru.craftlogic.CraftLogic.MODID;
import static ru.craftlogic.CraftLogic.registerBlock;
import static ru.craftlogic.CraftLogic.registerBlockWithItem;

public class CraftBlocks {
    public static Fluid FLUID_OIL = new Fluid("oil",
            new ResourceLocation(MODID, "blocks/fluid/oil_still"),
            new ResourceLocation(MODID, "blocks/fluid/oil_flow")
    ).setViscosity(2000).setDensity(1000);
    public static final Material MATERIAL_OIL = new MaterialLiquid(MapColor.BLACK);

    public static Block OIL;
    public static Block FURNACE;
    public static Block CHIMNEY;
    public static Block UNFIRED_POTTERY;
    public static Block CAULDRON;
    public static Block SMELTING_VAT;
    public static BlockGourd MELON, PUMPKIN;

    static void init() {
        FluidRegistry.registerFluid(FLUID_OIL);

        OIL = registerBlock(new BlockFluid("oil", FLUID_OIL, MATERIAL_OIL));

        FURNACE = registerBlockWithItem(new BlockFurnace());
        CHIMNEY = registerBlockWithItem(new BlockChimney());
        UNFIRED_POTTERY = registerBlockWithItem(new BlockUnfiredPottery());
        CAULDRON = registerBlockWithItem(new BlockCauldron());
        SMELTING_VAT = registerBlockWithItem(new BlockSmeltingVat());
        PUMPKIN = registerBlock(new BlockGourd(BlockGourd.GourdVariant.PUMPKIN));
        MELON = registerBlock(new BlockGourd(BlockGourd.GourdVariant.MELON));

        FluidRegistry.addBucketForFluid(FLUID_OIL);
    }
}
