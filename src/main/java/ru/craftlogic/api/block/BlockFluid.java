package ru.craftlogic.api.block;

import net.minecraft.block.material.Material;
import net.minecraft.client.renderer.block.model.ModelResourceLocation;
import net.minecraftforge.fluids.BlockFluidClassic;
import net.minecraftforge.fluids.Fluid;
import ru.craftlogic.api.ModelAutoReg;
import ru.craftlogic.client.ModelManager;

public class BlockFluid extends BlockFluidClassic implements ModelAutoReg {
    public BlockFluid(String name, Fluid fluid, Material material) {
        super(fluid, material);
        this.setUnlocalizedName(name);
        this.setRegistryName(name);
    }

    @Override
    public void registerModel(ModelManager modelManager) {
        modelManager.registerStateMapper(this, (state, mapper) ->
            new ModelResourceLocation(BlockFluid.this.getRegistryName(), "fluid")
        );
    }
}
