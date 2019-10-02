package ru.craftlogic.mixin.entity.passive;

import net.minecraft.block.BlockBeetroot;
import net.minecraft.block.BlockCrops;
import net.minecraft.entity.passive.EntityAnimal;
import net.minecraft.entity.passive.EntityPig;
import net.minecraft.init.Blocks;
import net.minecraft.world.World;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import ru.craftlogic.api.entity.Pig;
import ru.craftlogic.common.entity.ai.EntityAIEatCrops;

@Mixin(EntityPig.class)
public abstract class MixinEntityPig extends EntityAnimal implements Pig {
    private int eatTimer;
    private EntityAIEatCrops cropsEatAI;

    public MixinEntityPig(World world) {
        super(world);
    }

    @Inject(method = "initEntityAI", at = @At("RETURN"))
    protected void onAiInit(CallbackInfo info) {
        this.cropsEatAI = new EntityAIEatCrops<>(this, 1.2F, 16, (pos, state) ->
            state.getBlock() == Blocks.POTATOES && state.getValue(BlockCrops.AGE) == ((BlockCrops) Blocks.POTATOES).getMaxAge()
            || state.getBlock() == Blocks.CARROTS && state.getValue(BlockCrops.AGE) == ((BlockCrops) Blocks.CARROTS).getMaxAge()
            || state.getBlock() == Blocks.BEETROOTS && state.getValue(BlockBeetroot.BEETROOT_AGE) == ((BlockBeetroot) Blocks.BEETROOTS).getMaxAge()
            || state.getBlock() == Blocks.RED_FLOWER
            || state.getBlock() == Blocks.YELLOW_FLOWER
            || state.getBlock() == Blocks.RED_MUSHROOM
            || state.getBlock() == Blocks.BROWN_MUSHROOM
        , () -> true);
        this.tasks.addTask(9, this.cropsEatAI);
    }

    @Override
    protected void updateAITasks() {
        this.eatTimer = this.cropsEatAI.getEatingTimer();
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
}
