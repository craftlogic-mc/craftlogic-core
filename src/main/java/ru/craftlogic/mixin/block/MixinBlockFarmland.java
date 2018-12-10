package ru.craftlogic.mixin.block;

import net.minecraft.block.Block;
import net.minecraft.block.BlockFarmland;
import net.minecraft.block.material.Material;
import net.minecraft.entity.Entity;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;
import net.minecraftforge.common.MinecraftForge;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Overwrite;
import org.spongepowered.asm.mixin.Shadow;
import ru.craftlogic.api.event.block.FarmlandTrampleEvent;

@Mixin(BlockFarmland.class)
public abstract class MixinBlockFarmland extends Block {
    public MixinBlockFarmland(Material material) {
        super(material);
    }

    @Shadow
    protected static void turnToDirt(World world, BlockPos pos) {}

    /**
     * @author Radviger
     * @reason Farmland trample event
     */
    @Overwrite
    public void onFallenUpon(World world, BlockPos pos, Entity entity, float damage) {
        if (!world.isRemote && entity.canTrample(world, this, pos, damage)) {
            if (!MinecraftForge.EVENT_BUS.post(new FarmlandTrampleEvent(world, pos, entity))) {
                turnToDirt(world, pos);
            }
        }

        super.onFallenUpon(world, pos, entity, damage);
    }
}
