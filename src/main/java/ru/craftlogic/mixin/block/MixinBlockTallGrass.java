package ru.craftlogic.mixin.block;

import net.minecraft.block.BlockBush;
import net.minecraft.block.BlockTallGrass;
import net.minecraft.block.SoundType;
import net.minecraft.block.state.IBlockState;
import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.init.Blocks;
import net.minecraft.init.Items;
import net.minecraft.init.SoundEvents;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.NonNullList;
import net.minecraft.util.math.AxisAlignedBB;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.IBlockAccess;
import net.minecraft.world.World;
import net.minecraft.world.biome.Biome;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Overwrite;
import org.spongepowered.asm.mixin.Shadow;
import ru.craftlogic.api.CraftItems;
import ru.craftlogic.api.entity.Creature;
import ru.craftlogic.api.world.Location;

import javax.annotation.Nonnull;
import java.util.Random;

@Mixin(BlockTallGrass.class)
public class MixinBlockTallGrass extends BlockBush {

    @Shadow @Final protected static AxisAlignedBB TALL_GRASS_AABB;

    /**
     * @author Radviger
     * @reason Proper plant selection boxes
     */
    @Overwrite
    public AxisAlignedBB getBoundingBox(IBlockState state, IBlockAccess blockAccessor, BlockPos pos) {
        return TALL_GRASS_AABB.offset(state.getOffset(blockAccessor, pos));
    }

    @Override
    public void updateTick(World world, BlockPos pos, IBlockState state, Random rand) {
        super.updateTick(world, pos, state, rand);
        Location location = new Location(world, pos);
        if (world.isRemote && location.isAreaLoaded(3)) {
            Location up = location.offset(EnumFacing.UP);
            if (up.getLightFromNeighbors() < 4 && up.getBlockLightOpacity() > 2) {
                location.setBlockToAir();
            } else {
                if (up.getLightFromNeighbors() >= 9) {
                    for (int i = 0; i < 4; ++i) {
                        Location r = location.randomize(rand, 3);
                        Biome biome = r.getBiome();
                        if (rand.nextInt(100 + (biome.getEnableSnow() ? 200 : 0)) == 0) {
                            Location rup = r.offset(EnumFacing.UP);
                            Location rdown = r.offset(EnumFacing.DOWN);

                            if (r.isHeightValid() && r.isBlockLoaded() && rdown.isSameBlock(Blocks.GRASS)) {
                                if (rup.getLightFromNeighbors() >= 4 && rup.getBlockLightOpacity() <= 2 && r.canBlockBePlaced(Blocks.TALLGRASS)) {
                                    r.setBlockState(state);
                                }
                            }
                        }
                    }
                }
            }
        }
    }

    /**
     * @author Radviger
     * @reason Grass drop
     */
    @Overwrite
    public Item getItemDropped(IBlockState state, Random random, int fortune) {
        return Items.AIR;
    }

    /**
     * @author Radviger
     * @reason No seeds drop from grass
     */
    @Overwrite(remap = false)
    public void getDrops(NonNullList<ItemStack> drops, IBlockAccess blockAccessor, BlockPos pos, IBlockState state, int fortune) {}

    /**
     * @author Radviger
     * @reason Grass drop
     */
    @Nonnull
    @Overwrite(remap = false)
    public NonNullList<ItemStack> onSheared(@Nonnull ItemStack item, IBlockAccess world, BlockPos pos, int fortune) {
        return NonNullList.withSize(1, new ItemStack(CraftItems.GRASS));
    }

    /**
     * @author Radviger
     * @reason Plant passthrough sounds and entity slowdown
     */
    @Override
    public void onEntityCollision(World world, BlockPos pos, IBlockState state, Entity entity) {
        if (entity instanceof EntityLivingBase) {
            EntityLivingBase livingEntity = (EntityLivingBase)entity;

            if (world.getTotalWorldTime() % 12 == 0 && (entity.posX != entity.prevPosX || entity.posY != entity.prevPosY || entity.posZ != entity.prevPosZ)) {
                entity.playSound(SoundEvents.BLOCK_GRASS_HIT, SoundType.PLANT.getVolume() * 0.5f, SoundType.PLANT.getPitch() * 0.45f);
            }

            if (!((Creature)livingEntity).isJumping()) {
                float speedReductionHorizontal = 0.9F;
                float speedReductionVertical = 0.9F;
                entity.motionX *= speedReductionHorizontal;
                entity.motionY *= speedReductionVertical;
                entity.motionZ *= speedReductionHorizontal;
            }

            if (entity.isBeingRidden()) {
                for (Entity ent : entity.getPassengers()) {
                    onEntityCollision(world, pos, state, ent);
                }
            }
        }
    }
}
