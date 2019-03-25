package ru.craftlogic.mixin.client.gui.inventory;

import net.minecraft.block.Block;
import net.minecraft.client.gui.GuiTextField;
import net.minecraft.client.gui.inventory.GuiContainer;
import net.minecraft.client.gui.inventory.GuiContainerCreative;
import net.minecraft.client.gui.inventory.GuiContainerCreative.ContainerCreative;
import net.minecraft.creativetab.CreativeTabs;
import net.minecraft.inventory.Container;
import net.minecraft.inventory.Slot;
import net.minecraft.item.ItemStack;
import net.minecraft.util.ResourceLocation;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import java.util.List;

@Mixin(GuiContainerCreative.class)
public abstract class MixinGuiContainerCreative extends GuiContainer {
    @Shadow private static int selectedTabIndex;
    @Shadow private GuiTextField searchField;
    @Shadow private float currentScroll;
    @Shadow private List<Slot> originalSlots;
    @Shadow private Slot destroyItemSlot;

    public MixinGuiContainerCreative(Container container) {
        super(container);
    }

    @Inject(method = "<init>", at = @At("RETURN"))
    public void constructor(CallbackInfo info) {
        ContainerCreative container = (ContainerCreative)this.inventorySlots;
        container.itemList.sort(this::sortItems);
    }

    @Inject(method = "updateCreativeSearch", at = @At("RETURN"))
    public void updateCreativeSearch(CallbackInfo info) {
        ContainerCreative container = (ContainerCreative)this.inventorySlots;
        container.itemList.sort(this::sortItems);
    }

    @Inject(method = "setCurrentCreativeTab", at = @At("RETURN"))
    public void setCurrentCreativeTab(CreativeTabs tab, CallbackInfo info) {
        if (tab != null && tab != CreativeTabs.HOTBAR) {
            ContainerCreative container = (ContainerCreative) this.inventorySlots;
            container.itemList.sort(this::sortItems);
        }
    }

    private int sortItems(ItemStack a, ItemStack b) {
        ResourceLocation idA = a.getItem().getRegistryName();
        ResourceLocation idB = b.getItem().getRegistryName();
        String nameA = idA.getPath();
        String nameB = idB.getPath();
        boolean bA = Block.REGISTRY.containsKey(idA);
        boolean bB = Block.REGISTRY.containsKey(idB);
        if (bA != bB) {
            return Boolean.compare(bA, bB);
        } else {
            Block blockA = Block.REGISTRY.getObject(idA);
            Block blockB = Block.REGISTRY.getObject(idB);
            if (bA && bB) {
                if (blockA.getClass() != blockB.getClass()) {
                    return blockA.getClass().getName().compareTo(blockB.getClass().getName());
                }
            } else {
                if (a.getItem().getClass() != b.getItem().getClass()) {
                    return a.getItem().getClass().getName().compareTo(b.getItem().getClass().getName());
                }
            }
            /*if (!idA.getNamespace().equals(idB.getNamespace())) {
                return idA.getNamespace().compareTo(idB.getNamespace());
            } else*/ if (!nameA.equals(nameB)) {
                return nameA.compareTo(nameB);
            } else {
                return Integer.compare(a.getMetadata(), b.getMetadata());
            }
        }
    }
}
