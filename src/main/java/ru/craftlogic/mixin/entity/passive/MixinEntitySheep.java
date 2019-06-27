package ru.craftlogic.mixin.entity.passive;

import net.minecraft.block.BlockCrops;
import net.minecraft.entity.ai.EntityAIEatGrass;
import net.minecraft.entity.passive.EntityAnimal;
import net.minecraft.entity.passive.EntitySheep;
import net.minecraft.init.Blocks;
import net.minecraft.item.EnumDyeColor;
import net.minecraft.world.World;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Overwrite;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import ru.craftlogic.api.entity.Sheep;
import ru.craftlogic.common.entity.ai.EntityAIEatCrops;
import ru.craftlogic.common.entity.ai.EntityAIEatGrassAdvanced;

@Mixin(EntitySheep.class)
public abstract class MixinEntitySheep extends EntityAnimal implements Sheep {
    private EntityAIEatCrops<MixinEntitySheep> cropsEatAI;

    public MixinEntitySheep(World world) {
        super(world);
    }

    @Shadow public abstract void setSheared(boolean sheared);

    @Shadow public abstract EnumDyeColor getFleeceColor();

    @Shadow private EntityAIEatGrass entityAIEatGrass;

    @Shadow private int sheepTimer;

    /**
     * @author Radviger
     * @reason Advanced grass eating AI
     */
    @Overwrite
    protected void updateAITasks() {
        this.sheepTimer = this.entityAIEatGrass.getEatingGrassTimer() + this.cropsEatAI.getEatingTimer();
        super.updateAITasks();
    }

    @Inject(method = "initEntityAI", at = @At("RETURN"))
    protected void onAiInit(CallbackInfo info) {
        this.cropsEatAI = new EntityAIEatCrops<>(this, 1.2F, 16, (pos, state) ->
            state.getBlock() == Blocks.RED_FLOWER
            || state.getBlock() == Blocks.YELLOW_FLOWER
            || state.getBlock() == Blocks.WHEAT && state.getValue(BlockCrops.AGE) == ((BlockCrops) Blocks.WHEAT).getMaxAge()
        , () -> true);
        this.tasks.addTask(9, this.cropsEatAI);

        this.tasks.removeTask(this.entityAIEatGrass);
        this.entityAIEatGrass = new EntityAIEatGrassAdvanced<>(this, true, () -> true);
        this.targetTasks.addTask(5, this.entityAIEatGrass);
    }

    @Override
    public int getEatTimer() {
        return sheepTimer;
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
