package ru.craftlogic.util;

import net.minecraft.block.Block;
import net.minecraft.block.material.Material;
import net.minecraft.block.state.IBlockState;
import net.minecraft.client.gui.GuiScreen;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.init.Blocks;
import net.minecraft.init.Items;
import net.minecraft.init.SoundEvents;
import net.minecraft.item.*;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.EnumParticleTypes;
import net.minecraft.util.SoundCategory;
import net.minecraft.util.SoundEvent;
import net.minecraft.world.World;
import net.minecraftforge.event.ForgeEventFactory;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;
import ru.craftlogic.api.block.HeatConductor;
import ru.craftlogic.api.block.holders.ScreenHolder;
import ru.craftlogic.api.inventory.InventoryFieldHolder.InventoryFieldAdder;
import ru.craftlogic.api.inventory.InventoryHolder;
import ru.craftlogic.api.item.Tool;
import ru.craftlogic.api.sound.LoopingSoundSource;
import ru.craftlogic.api.world.Location;
import ru.craftlogic.client.screen.ScreenFurnace;
import ru.craftlogic.common.inventory.ContainerFurnace;

import static java.lang.Math.random;

public interface Furnace extends InventoryHolder, LoopingSoundSource, ScreenHolder, HeatConductor, InventoryFieldAdder  {
    int getFuel();
    int getMaxFuel();

    @Override
    default ContainerFurnace createContainer(EntityPlayer player, int subId) {
        return new ContainerFurnace(player.inventory, this);
    }

    @Override
    @SideOnly(Side.CLIENT)
    default GuiScreen createScreen(EntityPlayer player, int subId) {
        return new ScreenFurnace(player.inventory, this, this.createContainer(player, subId));
    }

    @Override
    default boolean isSoundActive(SoundEvent sound) {
        return this.getTemperature() >= this.getMaxTemperature() / 3;
    }

    default float getScaledTemperature() {
        return (float)this.getTemperature() / (float)this.getMaxTemperature();
    }

    @Override
    default float getSoundPitch(SoundEvent sound) {
        return getScaledTemperature();
    }

    @Override
    default boolean isItemValidForSlot(int slot, ItemStack stack) {
        return slot == 0 && isItemFuel(stack);
    }

    default void dropTemperatureWithEffect(int amount) {
        Location location = getLocation();
        World world = location.getWorld();
        Location upperLocation = location.offset(EnumFacing.UP);

        upperLocation.playSound(SoundEvents.BLOCK_FIRE_EXTINGUISH, SoundCategory.BLOCKS, 0.5F, 2.6F + (world.rand.nextFloat() - world.rand.nextFloat()) * 0.8F);

        for (int i = 0; i < 8; ++i) {
            upperLocation.spawnParticle(EnumParticleTypes.SMOKE_LARGE, random(), random(), random(), 0.0, 0.0, 0.0);
        }
        this.dropTemperature(amount, false);
    }

    void dropTemperature(int amount, boolean simulate);

    static int getItemBurnTemperature(ItemStack stack) {
        if (!stack.isEmpty()) {
            Item item = stack.getItem();
            Block block = Block.getBlockFromItem(item);
            if (block != Blocks.AIR) {
                IBlockState state = block.getStateFromMeta(stack.getMetadata());
                Material material = state.getMaterial();
                if (material == Material.WOOD) {
                    return 270;
                } else if (material == Material.CLOTH || material == Material.CARPET) {
                    return 120;
                } else if (material == Material.LEAVES || material == Material.PLANTS) {
                    return 250;
                } else if (block == Blocks.COAL_BLOCK) {
                    return 470;
                }
            } else if (item instanceof Tool && ((Tool)item).getToolMaterial(stack) == Item.ToolMaterial.WOOD) {
                return 270;
            } else if (item == Items.STICK || item == Items.BOW || item == Items.FISHING_ROD || item == Items.SIGN
                    || item == Items.BOWL || item instanceof ItemDoor && item != Items.IRON_DOOR || item instanceof ItemBoat) {
                return 270;
            } else if (item == Items.COAL) {
                return 470;
            } else {
                return (int)((float)getItemBurnTime(stack) / 3.4F);
            }
        }
        return 0;
    }

    static int getItemBurnTime(ItemStack stack) {
        if (!stack.isEmpty()) {
            int burnTime = ForgeEventFactory.getItemBurnTime(stack);
            if (burnTime >= 0) {
                return burnTime;
            } else {
                Item item = stack.getItem();
                if (item == Item.getItemFromBlock(Blocks.WOODEN_SLAB)) {
                    return 150;
                } else if (item == Item.getItemFromBlock(Blocks.WOOL)) {
                    return 100;
                } else if (item == Item.getItemFromBlock(Blocks.CARPET)) {
                    return 204;
                } else if (item == Item.getItemFromBlock(Blocks.LADDER)) {
                    return 300;
                } else if (item == Item.getItemFromBlock(Blocks.WOODEN_BUTTON)) {
                    return 100;
                } else if (Block.getBlockFromItem(item).getDefaultState().getMaterial() == Material.WOOD) {
                    return 300;
                } else if (item == Item.getItemFromBlock(Blocks.COAL_BLOCK)) {
                    return 16000;
                } else if (item instanceof ItemTool && "WOOD".equals(((ItemTool) item).getToolMaterialName())) {
                    return 200;
                } else if (item instanceof ItemSword && "WOOD".equals(((ItemSword) item).getToolMaterialName())) {
                    return 200;
                } else if (item instanceof ItemHoe && "WOOD".equals(((ItemHoe) item).getMaterialName())) {
                    return 200;
                } else if (item == Items.STICK) {
                    return 100;
                } else if (item == Items.BOW || item == Items.FISHING_ROD) {
                    return 300;
                } else if (item == Items.SIGN) {
                    return 200;
                } else if (item == Items.COAL) {
                    return 1600;
                } else if (item == Item.getItemFromBlock(Blocks.SAPLING) || item == Items.BOWL) {
                    return 100;
                } else if (item instanceof ItemDoor && item != Items.IRON_DOOR) {
                    return 200;
                } else if (item instanceof ItemBoat) {
                    return 400;
                }
            }
        }
        return 0;
    }

    static boolean isItemFuel(ItemStack stack) {
        return getItemBurnTime(stack) > 0;
    }

    enum FurnaceSlot implements SlotIdentifier {
        FUEL, ASH
    }

    enum FurnaceField implements InventoryHolder.FieldIdentifier {
        FUEL, MAX_FUEL, TEMPERATURE, MAX_TEMPERATURE
    }
}
