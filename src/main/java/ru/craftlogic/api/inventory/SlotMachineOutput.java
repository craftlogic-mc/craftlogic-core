package ru.craftlogic.api.inventory;

import net.minecraft.entity.item.EntityXPOrb;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.inventory.IInventory;
import net.minecraft.inventory.Slot;
import net.minecraft.item.ItemStack;
import ru.craftlogic.api.recipe.RecipeGrid;

import static java.lang.Math.random;
import static net.minecraft.util.math.MathHelper.ceil;
import static net.minecraft.util.math.MathHelper.floor;

public class SlotMachineOutput<M extends RecipeGrid & IInventory> extends Slot {

    private final EntityPlayer player;
    private final M machine;
    private int amount;

    public SlotMachineOutput(EntityPlayer player, M machine, int id, int x, int y) {
        super(machine, id, x, y);
        this.player = player;
        this.machine = machine;
    }

    @Override
    public boolean isItemValid(ItemStack stack) {
        return false;
    }

    @Override
    public ItemStack decrStackSize(int amount) {
        if (this.getHasStack()) {
            this.amount += Math.min(amount, this.getStack().getCount());
        }

        return super.decrStackSize(amount);
    }

    @Override
    protected void onSwapCraft(int amount) {
        this.amount += amount;
    }

    @Override
    public ItemStack onTake(EntityPlayer player, ItemStack stack) {
        this.onCrafting(stack);
        return super.onTake(player, stack);
    }

    @Override
    protected void onCrafting(ItemStack ist, int num) {
        this.amount += num;
        this.onCrafting(ist);
    }

    @Override
    protected void onCrafting(ItemStack stack) {
        stack.onCrafting(this.player.world, this.player, this.amount);
        float exp = this.machine.takeExp(Float.MAX_VALUE, false);
        int j = 0;
        if (exp != 0 && exp < 1) {
            j = floor(exp);
            if (j < ceil(exp) && random() < (double) (exp - (float) j)) {
                ++j;
            }
        }

        while(j > 0) {
            int a = EntityXPOrb.getXPSplit(j);
            j -= a;
            this.player.world.spawnEntity(new EntityXPOrb(this.player.world, this.player.posX, this.player.posY + 0.5D, this.player.posZ + 0.5D, a));
        }
        this.amount = 0;
    }
}
