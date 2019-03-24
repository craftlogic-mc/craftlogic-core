package ru.craftlogic.api.block;

import net.minecraft.block.material.Material;
import net.minecraft.block.state.IBlockState;
import net.minecraft.client.renderer.block.model.ModelResourceLocation;
import net.minecraft.entity.Entity;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.IBlockAccess;
import net.minecraftforge.fluids.BlockFluidClassic;
import net.minecraftforge.fluids.Fluid;
import ru.craftlogic.api.model.ModelAutoReg;
import ru.craftlogic.api.model.ModelManager;

import javax.annotation.Nullable;

public class BlockFluid extends BlockFluidClassic implements ModelAutoReg {
    public BlockFluid(String name, Fluid fluid, Material material) {
        super(fluid, material);
        this.setTranslationKey(name);
        this.setRegistryName(name);
    }

    @Override
    public void registerModel(ModelManager modelManager) {
        modelManager.registerStateMapper(this, (state, mapper) ->
            new ModelResourceLocation(BlockFluid.this.getRegistryName(), "fluid")
        );
    }

    @Nullable
    public Boolean isEntityInsideMaterial(IBlockAccess blockAccessor, BlockPos pos, IBlockState state, Entity entity, double y, Material material, boolean p_isEntityInsideMaterial_8_) {
        return material == Material.WATER;
    }
}
