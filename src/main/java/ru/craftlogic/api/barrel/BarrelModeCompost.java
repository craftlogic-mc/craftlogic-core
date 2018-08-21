package ru.craftlogic.api.barrel;

import net.minecraft.block.BlockDirt;
import net.minecraft.block.state.IBlockState;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.init.Blocks;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.EnumHand;
import net.minecraft.util.ResourceLocation;
import net.minecraft.world.World;
import net.minecraftforge.common.capabilities.Capability;
import ru.craftlogic.api.CraftRecipes;
import ru.craftlogic.api.tile.TileEntityBase;
import ru.craftlogic.common.recipe.RecipeBarrelCompost;
import ru.craftlogic.common.recipe.RecipeGridBarrel;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import java.util.Random;

public class BarrelModeCompost extends BarrelMode {
    private RecipeBarrelCompost recipe;
    private int color;
    private int fill, maxFill = 1000;
    private int time, maxTime;

    public BarrelModeCompost(Barrel barrel) {}

    @Override
    public void onCreated(Object... input) {
        RecipeBarrelCompost recipe = (RecipeBarrelCompost) input[0];
        this.recipe = recipe;
        this.color = recipe.getColor();
        this.fill = recipe.getGain();
        this.maxTime = recipe.getTimeRequired();
    }

    @Override
    public IBlockState getBlock(Barrel barrel) {
        return Blocks.DIRT.getDefaultState().withProperty(BlockDirt.VARIANT, BlockDirt.DirtType.COARSE_DIRT);
    }

    @Override
    public NBTTagCompound writeToNBT(NBTTagCompound compound) {
        if (this.recipe != null) {
            compound.setString("recipe", this.recipe.getName().toString());
            compound.setInteger("color", this.color);
            compound.setInteger("fill", this.fill);
            compound.setInteger("maxFill", this.maxFill);
            compound.setInteger("time", this.time);
            compound.setInteger("maxTime", this.maxTime);
        }
        return compound;
    }

    @Override
    public void readFromNBT(NBTTagCompound compound) {
        if (compound.hasKey("recipe")) {
            ResourceLocation recipeName = new ResourceLocation(compound.getString("recipe"));
            this.recipe = CraftRecipes.getByName(RecipeGridBarrel.class, recipeName);
            this.color = compound.getInteger("color");
            this.fill = compound.getInteger("fill");
            this.maxFill = compound.getInteger("maxFill");
            this.time = compound.getInteger("time");
            this.maxTime = compound.getInteger("maxTime");
        }
    }

    @Override
    public void randomTick(Barrel barrel, Random random) {}

    @Override
    public int getColor(Barrel barrel) {
        float progress = this.getProgress(barrel);
        return this.getShiftedColor(this.color, 0xFFFFFF, progress);
    }

    @Override
    public float getFill(Barrel barrel) {
        return (float)this.fill/(float)this.maxFill;
    }

    public float getProgress(Barrel barrel) {
        return (float)this.time/(float)this.maxTime;
    }

    public boolean isFull(Barrel barrel) {
        return this.fill >= this.maxFill;
    }

    public boolean isReady(Barrel barrel) {
        return this.time >= this.maxTime;
    }

    @Override
    public boolean isEmpty(Barrel barrel) {
        return this.fill <= 0;
    }

    @Override
    public boolean interact(Barrel barrel, EntityPlayer player, EnumHand hand) {
        ItemStack heldItem = player.getHeldItem(hand);
        boolean ready = this.isReady(barrel);
        boolean full = this.isFull(barrel);
        boolean closed = barrel.isClosed();
        World world = player.world;
        if (ready && full && !closed) {
            if (heldItem.isEmpty()) {
                if (!world.isRemote) {
                    ItemStack drop = new ItemStack(Blocks.DIRT, BlockDirt.DirtType.COARSE_DIRT.getMetadata());
                    if (!player.addItemStackToInventory(drop)) {
                        player.dropItem(drop, false);
                    }
                    barrel.clear();
                }
                return true;
            }
        } else if (!full && !closed) {
            RecipeGridBarrel grid = new RecipeGridBarrel(heldItem, barrel);
            RecipeBarrelCompost recipe = CraftRecipes.getMatchingRecipe(grid);
            if (recipe != null) {
                int gain = recipe.getGain();
                if (!world.isRemote) {
                    if (this.fill(gain, true) != 0) {
                        recipe.consume(grid);
                        this.fill(gain, false);
                        this.color = this.getAverageColor(this.color, recipe.getColor());
                        this.maxTime += recipe.getTimeRequired();
                        if (barrel instanceof TileEntityBase) {
                            ((TileEntityBase) barrel).markForUpdate();
                        }
                    }
                }
                return true;
            }
        }
        return false;
    }

    public int fill(int amount, boolean simulate) {
        amount = Math.min(this.maxFill - this.fill, amount);
        if (!simulate) this.fill += amount;
        return amount;
    }

    public int getAverageColor(int first, int second) {
        int fr = (first >> 16) & 255;
        int fg = (first >> 8) & 255;
        int fb = first & 255;

        int sr = (second >> 16) & 255;
        int sg = (second >> 8) & 255;
        int sb = second & 255;

        int r = (fr + sr) / 2;
        int g = (fg + sg) / 2;
        int b = (fb + sb) / 2;
        return r << 16 | g << 8 | b;
    }

    public int getShiftedColor(int from, int to, float progress) {
        int fr = (from >> 16) & 255;
        int fg = (from >> 8) & 255;
        int fb = from & 255;

        int tr = (to >> 16) & 255;
        int tg = (to >> 8) & 255;
        int tb = to & 255;

        int dr = tr - fr;
        int dg = tg - fg;
        int db = tb - fb;

        int r = (int)((float)fr + (float)dr * progress);
        int g = (int)((float)fg + (float)dg * progress);
        int b = (int)((float)fb + (float)db * progress);
        return r << 16 | g << 8 | b;
    }

    @Override
    public void update(Barrel barrel) {
        if (this.time < this.maxTime) {
            this.time++;
        }
    }

    @Override
    public boolean hasCapability(@Nonnull Capability<?> capability, @Nullable EnumFacing side) {
        return false;
    }

    @Nullable
    @Override
    public <T> T getCapability(@Nonnull Capability<T> capability, @Nullable EnumFacing side) {
        return null;
    }
}
