package ru.craftlogic.mixin.entity.passive;

import net.minecraft.block.BlockCrops;
import net.minecraft.entity.ai.EntityAIEatGrass;
import net.minecraft.entity.passive.EntityAnimal;
import net.minecraft.entity.passive.EntityCow;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.init.Blocks;
import net.minecraft.init.Items;
import net.minecraft.init.SoundEvents;
import net.minecraft.item.ItemStack;
import net.minecraft.network.datasync.DataParameter;
import net.minecraft.network.datasync.DataSerializers;
import net.minecraft.network.datasync.EntityDataManager;
import net.minecraft.util.EnumHand;
import net.minecraft.world.World;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Overwrite;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import ru.craftlogic.api.entity.Cow;
import ru.craftlogic.common.entity.ai.EntityAIEatCrops;
import ru.craftlogic.common.entity.ai.EntityAIEatGrassAdvanced;

@Mixin(EntityCow.class)
public abstract class MixinEntityCow extends EntityAnimal implements Cow {
    private static final DataParameter<Integer> MILK = EntityDataManager.createKey(EntityCow.class, DataSerializers.VARINT);

    private int eatTimer;
    private EntityAIEatGrass grassEatAI;
    private EntityAIEatCrops<MixinEntityCow> cropsEatAI;

    public MixinEntityCow(World world) {
        super(world);
    }

    @Override
    protected void entityInit() {
        super.entityInit();
        this.dataManager.register(MILK, 0);
    }

    @Inject(method = "initEntityAI", at = @At("RETURN"))
    protected void onAiInit(CallbackInfo info) {
        this.grassEatAI = new EntityAIEatGrassAdvanced<>(this, true, () -> !this.hasMilk());
        this.cropsEatAI = new EntityAIEatCrops<>(this, 1.2F, 16, (pos, state) ->
            state.getBlock() == Blocks.RED_FLOWER
            || state.getBlock() == Blocks.YELLOW_FLOWER
            || state.getBlock() == Blocks.WHEAT && state.getValue(BlockCrops.AGE) == ((BlockCrops) Blocks.WHEAT).getMaxAge()
        , () -> !this.hasMilk());
        this.tasks.addTask(8, this.grassEatAI);
        this.tasks.addTask(9, this.cropsEatAI);
    }

    @Override
    protected void updateAITasks() {
        this.eatTimer = this.grassEatAI.getEatingGrassTimer() + this.cropsEatAI.getEatingTimer();
        super.updateAITasks();
    }

    @Override
    public void onLivingUpdate() {
        if (this.world.isRemote) {
            this.eatTimer = Math.max(0, this.eatTimer - 1);
        }

        super.onLivingUpdate();
    }

    @Override
    @SideOnly(Side.CLIENT)
    public void handleStatusUpdate(byte status) {
        if (status == 10) {
            this.eatTimer = getMaxEatTimer();
        } else {
            super.handleStatusUpdate(status);
        }
    }

    @Override
    public int getEatTimer() {
        return eatTimer;
    }

    @Override
    public int getMaxEatTimer() {
        return 40;
    }

    @Override
    public float getRotationPitch() {
        return rotationPitch;
    }

    @Override
    public boolean hasMilk() {
        return this.dataManager.get(MILK) >= 1000;
    }

    /**
     * @author Radviger
     * @reason Custom cows
     */
    @Overwrite
    public boolean processInteract(EntityPlayer player, EnumHand hand) {
        ItemStack heldItem = player.getHeldItem(hand);
        if (heldItem.getItem() == Items.BUCKET && !player.capabilities.isCreativeMode && !this.isChild()) {
            int milk = this.dataManager.get(MILK);
            if (milk >= 1000) {
                this.dataManager.set(MILK, milk - 1000);
                player.playSound(SoundEvents.ENTITY_COW_MILK, 1F, 1F);
                heldItem.shrink(1);
                if (heldItem.isEmpty()) {
                    player.setHeldItem(hand, new ItemStack(Items.MILK_BUCKET));
                } else if (!player.inventory.addItemStackToInventory(new ItemStack(Items.MILK_BUCKET))) {
                    player.dropItem(new ItemStack(Items.MILK_BUCKET), false);
                }
            } else {
                player.playSound(SoundEvents.ENTITY_COW_HURT, 1F, 1F);
            }

            return true;
        } else {
            return super.processInteract(player, hand);
        }
    }

    @Override
    public void eatGrassBonus() {
        if (!this.isChild()) {
            this.dataManager.set(MILK, Math.min(1000, this.dataManager.get(MILK) + 500));
        }
    }

    @Override
    public int getTalkInterval() {
        return hasMilk() ? 40 : 120;
    }
}
