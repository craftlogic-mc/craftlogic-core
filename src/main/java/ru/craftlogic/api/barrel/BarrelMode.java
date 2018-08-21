package ru.craftlogic.api.barrel;

import net.minecraft.block.state.IBlockState;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.BlockModelShapes;
import net.minecraft.client.renderer.BlockRendererDispatcher;
import net.minecraft.client.renderer.texture.TextureAtlasSprite;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.util.EnumHand;
import net.minecraft.util.ResourceLocation;
import net.minecraftforge.common.capabilities.ICapabilityProvider;
import net.minecraftforge.fluids.Fluid;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;

import javax.annotation.Nonnull;
import java.util.Random;

public abstract class BarrelMode implements ICapabilityProvider {
    ResourceLocation name;

    public final ResourceLocation getRegistryName() {
        return this.name;
    }
    public abstract NBTTagCompound writeToNBT(NBTTagCompound compound);
    public abstract void readFromNBT(NBTTagCompound compound);
    public abstract int getColor(Barrel barrel);
    @SideOnly(Side.CLIENT)
    public @Nonnull TextureAtlasSprite getTexture(Minecraft mc, Barrel barrel) {
        IBlockState block = getBlock(barrel);
        BlockRendererDispatcher rendererDispatcher = mc.getBlockRendererDispatcher();
        BlockModelShapes modelShapes = rendererDispatcher.getBlockModelShapes();
        return modelShapes.getTexture(block);
    }
    public abstract IBlockState getBlock(Barrel barrel);
    public abstract float getFill(Barrel barrel);
    public abstract boolean isEmpty(Barrel barrel);

    public abstract void onCreated(Object... input);
    public void randomTick(Barrel barrel, Random random) {}
    @SideOnly(Side.CLIENT)
    public void randomDisplayTick(Barrel barrel, Random random) {}
    public abstract boolean interact(Barrel barrel, EntityPlayer player, EnumHand hand);
    public void update(Barrel barrel) {}
    public void fillWithRain(Barrel barrel, Fluid fluid) {}
}
