package ru.craftlogic.common.block;

import net.minecraft.block.Block;
import net.minecraft.block.BlockBush;
import net.minecraft.block.SoundType;
import net.minecraft.block.material.Material;
import net.minecraft.block.properties.PropertyEnum;
import net.minecraft.block.state.BlockStateContainer;
import net.minecraft.block.state.IBlockState;
import net.minecraft.creativetab.CreativeTabs;
import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.init.MobEffects;
import net.minecraft.init.SoundEvents;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.potion.PotionEffect;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.EnumHand;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.MathHelper;
import net.minecraft.world.ColorizerFoliage;
import net.minecraft.world.IBlockAccess;
import net.minecraft.world.World;
import net.minecraftforge.common.ForgeHooks;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;
import ru.craftlogic.api.CraftAPI;
import ru.craftlogic.api.CraftItems;
import ru.craftlogic.api.block.Colored;
import ru.craftlogic.api.entity.Creature;
import ru.craftlogic.api.model.ModelManager;
import ru.craftlogic.api.model.ModelRegistrar;
import ru.craftlogic.api.util.Nameable;
import ru.craftlogic.api.world.Location;

import javax.annotation.Nullable;
import java.util.Random;

public class BlockBerryBush extends BlockBush implements ModelRegistrar, Colored {
    public static final PropertyEnum<Stage> STAGE = PropertyEnum.create("stage", Stage.class);

    public BlockBerryBush() {
        super(Material.GRASS);
        setRegistryName("berry_bush");
        setHarvestLevel("axe", 1);
        setTranslationKey("berry_bush");
        setCreativeTab(CreativeTabs.DECORATIONS);
        setSoundType(SoundType.PLANT);
        setTickRandomly(true);
        setHardness(0.8F);
    }

    @Override
    public IBlockState getStateFromMeta(int meta) {
        return getDefaultState().withProperty(STAGE, Stage.values()[meta % Stage.values().length]);
    }

    @Override
    public int getMetaFromState(IBlockState state) {
        return state.getValue(STAGE).ordinal();
    }

    @Override
    protected BlockStateContainer createBlockState() {
        return new BlockStateContainer(this, STAGE);
    }

    @Override
    @SideOnly(Side.CLIENT)
    public void registerModel(ModelManager modelManager) {
        modelManager.registerItemModel(Item.getItemFromBlock(this));
    }

    @Override
    @SideOnly(Side.CLIENT)
    public int getBlockColor(@Nullable Location location, IBlockState state, int tint) {
        return tint == 1 ? 0xFF0000 : (location != null ? location.getFoliageColor() : 0xFFFFFF);
    }

    @Override
    public int getItemColor(ItemStack stack, int tint) {
        return tint == 1 ? 0xFF0000 : ColorizerFoliage.getFoliageColorBasic();
    }

    @Override
    public void updateTick(World world, BlockPos pos, IBlockState state, Random rand) {
        if (world.isAreaLoaded(pos, 1)) {
            Stage stage = state.getValue(STAGE);
            if (world.getLightFromNeighbors(pos.up()) >= 9) {
                if (!stage.isRipe()) {
                    float chance = getGrowthChance(this, world, pos);
                    if (ForgeHooks.onCropsGrowPre(world, pos, state, rand.nextInt((int) (25F / chance) + 1) == 0)) {
                        world.setBlockState(pos, state.cycleProperty(STAGE));
                        ForgeHooks.onCropsGrowPost(world, pos, state, world.getBlockState(pos));
                    }
                }
            }
        }
    }

    @Override
    public boolean onBlockActivated(World world, BlockPos pos, IBlockState state, EntityPlayer player, EnumHand hand, EnumFacing side, float hitX, float hitY, float hitZ) {
        if (state.getValue(STAGE).isRipe() && player.getHeldItem(hand).isEmpty()) {
            if (!world.isRemote) {
                world.setBlockState(pos, state.withProperty(STAGE, Stage.BIG));
                ItemStack berries = new ItemStack(CraftItems.BERRY, 1 + world.rand.nextInt(3));
                if (!player.inventory.addItemStackToInventory(berries)) {
                    player.dropItem(berries, false);
                }
            }
            return true;
        }
        return false;
    }

    @Override
    public boolean isPassable(IBlockAccess world, BlockPos pos) {
        return true;
    }

    @Override
    public void onEntityCollision(World world, BlockPos pos, IBlockState state, Entity entity) {
        if (entity instanceof EntityLivingBase) {
            EntityLivingBase livingEntity = (EntityLivingBase)entity;

            if (livingEntity.fallDistance > 3F) {
                entity.playSound(SoundEvents.BLOCK_GRASS_BREAK, SoundType.PLANT.getVolume() * 0.6f, SoundType.PLANT.getPitch() * 0.65f);
            } else if (world.getTotalWorldTime() % 6 == 0 && (entity.posX != entity.prevPosX || entity.posY != entity.prevPosY || entity.posZ != entity.prevPosZ)) {
                entity.playSound(SoundEvents.BLOCK_GRASS_HIT, SoundType.PLANT.getVolume() * 0.5f, SoundType.PLANT.getPitch() * 0.45f);
            }

            if (!((Creature)livingEntity).isJumping()) {
                float speedReductionHorizontal = 0.9F;
                float speedReductionVertical = 0.9F;
                entity.motionX *= speedReductionHorizontal;
                entity.motionY *= speedReductionVertical;
                entity.motionZ *= speedReductionHorizontal;
            }

            int fallDamageThreshold = 20;
            if (livingEntity.fallDistance > fallDamageThreshold) {
                livingEntity.fallDistance -= fallDamageThreshold;
                PotionEffect pe = livingEntity.getActivePotionEffect(MobEffects.JUMP_BOOST);
                float fallDamageReduction = 0.5F;
                int amount = MathHelper.ceil(livingEntity.fallDistance * fallDamageReduction * ((pe == null) ? 1F : 0.9F));
                livingEntity.attackEntityFrom(CraftAPI.DAMAGE_SOURCE_FALL_INTO_LEAVES, amount);
            }

            if (livingEntity.fallDistance > 1F) { livingEntity.fallDistance = 1F; }

            if (entity.isBeingRidden()) {
                for (Entity ent : entity.getPassengers()) {
                    onEntityCollision(world, pos, state, ent);
                }
            }
        }
    }

    protected static float getGrowthChance(Block block, World world, BlockPos pos) {
        float chance = 1F;
        BlockPos soilPos = pos.down();

        for (int i = -1; i <= 1; ++i) {
            for (int j = -1; j <= 1; ++j) {
                float f1 = 0F;
                IBlockState s = world.getBlockState(soilPos.add(i, 0, j));

                if (s.getBlock().canSustainPlant(s, world, soilPos.add(i, 0, j), net.minecraft.util.EnumFacing.UP, (net.minecraftforge.common.IPlantable) block)) {
                    f1 = 1F;

                    if (s.getBlock().isFertile(world, soilPos.add(i, 0, j))) {
                        f1 = 3F;
                    }
                }

                if (i != 0 || j != 0) {
                    f1 /= 4F;
                }

                chance += f1;
            }
        }

        BlockPos n = pos.north();
        BlockPos s = pos.south();
        BlockPos w = pos.west();
        BlockPos e = pos.east();
        boolean hasX = block == world.getBlockState(w).getBlock() || block == world.getBlockState(e).getBlock();
        boolean hasZ = block == world.getBlockState(n).getBlock() || block == world.getBlockState(s).getBlock();

        if (hasX && hasZ) {
            chance /= 2F;
        } else {
            boolean flag2 = block == world.getBlockState(w.north()).getBlock() || block == world.getBlockState(e.north()).getBlock() || block == world.getBlockState(e.south()).getBlock() || block == world.getBlockState(w.south()).getBlock();

            if (flag2) {
                chance /= 2F;
            }
        }

        return chance;
    }

    public enum Stage implements Nameable {
        SMALL, MEDIUM, BIG, BIG_RIPE;

        public boolean isRipe() {
            return this == BIG_RIPE;
        }
    }
}
