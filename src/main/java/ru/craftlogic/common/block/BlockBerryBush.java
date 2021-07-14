package ru.craftlogic.common.block;

import net.minecraft.block.Block;
import net.minecraft.block.BlockBush;
import net.minecraft.block.SoundType;
import net.minecraft.block.properties.PropertyBool;
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
import net.minecraft.util.math.AxisAlignedBB;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.MathHelper;
import net.minecraft.world.IBlockAccess;
import net.minecraft.world.World;
import net.minecraftforge.common.EnumPlantType;
import net.minecraftforge.common.ForgeHooks;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;
import ru.craftlogic.CraftConfig;
import ru.craftlogic.api.CraftAPI;
import ru.craftlogic.api.CraftMaterials;
import ru.craftlogic.api.block.Plantable;
import ru.craftlogic.api.entity.Creature;
import ru.craftlogic.api.model.ModelManager;
import ru.craftlogic.api.model.ModelRegistrar;
import ru.craftlogic.api.world.Location;

import java.util.Random;

public abstract class BlockBerryBush extends BlockBush implements ModelRegistrar, Plantable {
    public static final PropertyBool RIPE = PropertyBool.create("ripe");

    public BlockBerryBush(String name) {
        super(CraftMaterials.BERRY_BUSH);
        setRegistryName(name + "_bush");
        setTranslationKey(name + "_bush");
        setHarvestLevel("shovel", 1);
        setCreativeTab(CreativeTabs.DECORATIONS);
        setSoundType(SoundType.PLANT);
        setTickRandomly(true);
        setHardness(0.8F);
        setDefaultState(getBlockState().getBaseState().withProperty(RIPE, false));
    }

    protected IBlockState getOffspring(IBlockState self, World world, BlockPos pos, Random rand, float chance) {
        return self.withProperty(RIPE, false);
    }

    protected boolean tryGrowOffspring(IBlockState self, World world, BlockPos pos, Random rand, float chance) {
        IBlockState offspring = getOffspring(self, world, pos, rand, chance);
        if (canPlaceBlockAt(world, pos) && ForgeHooks.onCropsGrowPre(world, pos, offspring, rand.nextInt((int) (100F / chance / 8) + 1) == 0)) {
            world.setBlockState(pos, offspring);
            ForgeHooks.onCropsGrowPost(world, pos, offspring, offspring);
            return true;
        } else {
            return false;
        }
    }

    @Override
    public abstract AxisAlignedBB getBoundingBox(IBlockState state, IBlockAccess source, BlockPos pos);

    public abstract Item getBerry();

    @Override
    public IBlockState getStateFromMeta(int meta) {
        return getDefaultState().withProperty(RIPE, (meta & 8) > 0);
    }

    @Override
    public int getMetaFromState(IBlockState state) {
        return state.getValue(RIPE) ? 8 : 0;
    }

    @Override
    protected BlockStateContainer createBlockState() {
        return new BlockStateContainer(this, RIPE);
    }

    @Override
    @SideOnly(Side.CLIENT)
    public void registerModel(ModelManager modelManager) {
        modelManager.registerItemModel(Item.getItemFromBlock(this));
    }

    @Override
    public void updateTick(World world, BlockPos pos, IBlockState state, Random rand) {
        if (world.isAreaLoaded(pos, 1)) {
            if (world.getLightFromNeighbors(pos.up()) >= 9) {
                if (!state.getValue(RIPE)) {
                    float chance = getGrowthChance(this, world, pos);
                    if (ForgeHooks.onCropsGrowPre(world, pos, state, rand.nextInt((int) (100F / chance / 2) + 1) == 0)) {
                        world.setBlockState(pos, state.cycleProperty(RIPE));
                        ForgeHooks.onCropsGrowPost(world, pos, state, world.getBlockState(pos));
                    } else if (CraftConfig.blocks.enableBerryBushSpreading) {
                        for (EnumFacing side : EnumFacing.HORIZONTALS) {
                            BlockPos p = pos.offset(side);
                            if (tryGrowOffspring(state, world, p, rand, chance / 5F)
                                || tryGrowOffspring(state, world, p.up(), rand, chance / 8F)
                                || tryGrowOffspring(state, world, p.down(), rand, chance / 8F)) {
                                break;
                            }
                        }
                    }
                }
            }
        }
    }

    @Override
    public boolean onBlockActivated(World world, BlockPos pos, IBlockState state, EntityPlayer player, EnumHand hand, EnumFacing side, float hitX, float hitY, float hitZ) {
        if (state.getValue(RIPE) && player.getHeldItem(hand).isEmpty()) {
            if (!world.isRemote) {
                world.playEvent(2001, pos, Block.getStateId(state));
                world.setBlockState(pos, state.withProperty(RIPE, false));
                ItemStack berries = new ItemStack(getBerry(), 1 + world.rand.nextInt(2));
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

    @Override
    public EnumPlantType getPlantType(Location location) {
        return EnumPlantType.Plains;
    }

    @Override
    public IBlockState getPlant(Location location) {
        return super.getPlant(location.getBlockAccessor(), location.getPos());
    }
}
