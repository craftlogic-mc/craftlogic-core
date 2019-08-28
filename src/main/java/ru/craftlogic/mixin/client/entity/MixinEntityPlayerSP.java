package ru.craftlogic.mixin.client.entity;

import com.mojang.authlib.GameProfile;
import net.minecraft.client.Minecraft;
import net.minecraft.client.entity.EntityPlayerSP;
import net.minecraft.client.gui.GuiEnchantment;
import net.minecraft.client.gui.GuiRepair;
import net.minecraft.client.gui.inventory.GuiCrafting;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.world.IInteractionObject;
import net.minecraft.world.World;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Overwrite;
import org.spongepowered.asm.mixin.Shadow;
import ru.craftlogic.common.inventory.ContainerVirtualWorkbench;

@Mixin(EntityPlayerSP.class)
public abstract class MixinEntityPlayerSP extends EntityPlayer {
    @Shadow protected Minecraft mc;

    public MixinEntityPlayerSP(World world, GameProfile profile) {
        super(world, profile);
    }

    /**
     * @author Radviger
     * @reason Virtual workbench
     */
    @Overwrite
    public void displayGui(IInteractionObject menu) {
        String id = menu.getGuiID();
        switch (id) {
            case "minecraft:crafting_table":
                this.mc.displayGuiScreen(new GuiCrafting(this.inventory, this.world));
                break;
            case "minecraft:enchanting_table":
                this.mc.displayGuiScreen(new GuiEnchantment(this.inventory, this.world, menu));
                break;
            case "minecraft:anvil":
                this.mc.displayGuiScreen(new GuiRepair(this.inventory, this.world));
                break;
            case "craftlogic:virtual_workbench":
                GuiCrafting screen = new GuiCrafting(this.inventory, this.world);
                screen.inventorySlots = new ContainerVirtualWorkbench(this.inventory, this.world);
                this.mc.displayGuiScreen(screen);
                break;
        }

    }
}
