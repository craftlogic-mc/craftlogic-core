package ru.craftlogic.common.block;

import net.minecraft.block.BlockBush;
import net.minecraft.block.SoundType;
import net.minecraft.block.properties.PropertyEnum;
import net.minecraft.block.state.BlockStateContainer;
import net.minecraft.block.state.IBlockState;
import net.minecraft.client.renderer.block.statemap.StateMap;
import net.minecraft.creativetab.CreativeTabs;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.init.Blocks;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.NonNullList;
import net.minecraft.util.math.AxisAlignedBB;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.text.translation.I18n;
import net.minecraft.world.IBlockAccess;
import net.minecraft.world.World;
import net.minecraft.world.gen.feature.WorldGenerator;
import net.minecraftforge.event.terraingen.TerrainGen;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;
import ru.craftlogic.api.block.Growable;
import ru.craftlogic.api.model.ModelManager;
import ru.craftlogic.api.model.ModelRegistrar;
import ru.craftlogic.api.world.Location;
import ru.craftlogic.common.world.WorldGenMegaPine;
import ru.craftlogic.common.world.WorldGenPine;
import ru.craftlogic.common.world.WorldGenWillow;

import java.util.Random;

import static net.minecraft.block.BlockSapling.STAGE;

public class BlockSapling2 extends BlockBush implements Growable, ModelRegistrar {
    public static final PropertyEnum<BlockPlanks2.PlanksType2> VARIANT = PropertyEnum.create("variant", BlockPlanks2.PlanksType2.class);
    private static final AxisAlignedBB BOUNDING = new AxisAlignedBB(0.09999999403953552D, 0.0D, 0.09999999403953552D, 0.8999999761581421D, 0.800000011920929D, 0.8999999761581421D);

    public BlockSapling2() {
        setRegistryName("sapling2");
        setDefaultState(getBlockState().getBaseState().withProperty(VARIANT, BlockPlanks2.PlanksType2.PINE).withProperty(STAGE, 0));
        setCreativeTab(CreativeTabs.DECORATIONS);
        setSoundType(SoundType.PLANT);
    }

    @Override
    public IBlockState getStateForPlacement(World world, BlockPos pos, EnumFacing facing, float hitX, float hitY, float hitZ, int meta, EntityLivingBase placer) {
        return getDefaultState().withProperty(VARIANT, BlockPlanks2.PlanksType2.byMetadata(meta & 7));
    }

    @Override
    public AxisAlignedBB getBoundingBox(IBlockState state, IBlockAccess blockAccessor, BlockPos pos) {
        return BOUNDING;
    }

    @Override
    public String getLocalizedName() {
        return I18n.translateToLocal(getTranslationKey() + "." + BlockPlanks2.PlanksType2.PINE.getName() + ".name");
    }

    public String getTranslationKey(ItemStack item) {
        return BlockPlanks2.PlanksType2.byMetadata(item.getMetadata() & 3).getName();
    }

    @Override
    public void updateTick(World world, BlockPos pos, IBlockState state, Random rand) {
        if (!world.isRemote) {
            super.updateTick(world, pos, state, rand);

            if (!world.isAreaLoaded(pos, 1))
                return; // Forge: prevent loading unloaded chunks when checking neighbor's light
            if (world.getLightFromNeighbors(pos.up()) >= 9 && rand.nextInt(7) == 0) {
                this.grow(world, pos, state, rand);
            }
        }
    }

    public void grow(World worldIn, BlockPos pos, IBlockState state, Random rand) {
        if (state.getValue(STAGE) == 0) {
            worldIn.setBlockState(pos, state.cycleProperty(STAGE), 4);
        } else {
            this.generateTree(worldIn, pos, state, rand);
        }
    }

    public void generateTree(World world, BlockPos pos, IBlockState state, Random rand) {
        if (!TerrainGen.saplingGrowTree(world, rand, pos)) return;
        WorldGenerator gen = null;
        int i = 0;
        int j = 0;
        boolean huge = false;

        switch (state.getValue(VARIANT)) {
            default:
            case PINE:
                loop:

                for (i = 0; i >= -1; --i) {
                    for (j = 0; j >= -1; --j) {
                        if (isTwoByTwoOfType(world, pos, i, j, BlockPlanks2.PlanksType2.PINE)) {
                            gen = new WorldGenMegaPine(false);
                            huge = true;
                            break loop;
                        }
                    }
                }

                if (!huge) {
                    i = 0;
                    j = 0;
                    gen = new WorldGenPine(true);
                }

                break;
            case WILLOW:
                gen = new WorldGenWillow(true);
                break;
        }

        IBlockState air = Blocks.AIR.getDefaultState();

        if (huge) {
            world.setBlockState(pos.add(i, 0, j), air, 4);
            world.setBlockState(pos.add(i + 1, 0, j), air, 4);
            world.setBlockState(pos.add(i, 0, j + 1), air, 4);
            world.setBlockState(pos.add(i + 1, 0, j + 1), air, 4);
        } else {
            world.setBlockState(pos, air, 4);
        }

        if (!gen.generate(world, rand, pos.add(i, 0, j))) {
            if (huge) {
                world.setBlockState(pos.add(i, 0, j), state, 4);
                world.setBlockState(pos.add(i + 1, 0, j), state, 4);
                world.setBlockState(pos.add(i, 0, j + 1), state, 4);
                world.setBlockState(pos.add(i + 1, 0, j + 1), state, 4);
            } else {
                world.setBlockState(pos, state, 4);
            }
        }
    }

    private boolean isTwoByTwoOfType(World world, BlockPos pos, int i, int j, BlockPlanks2.PlanksType2 type) {
        return this.isTypeAt(world, pos.add(i, 0, j), type) && this.isTypeAt(world, pos.add(i + 1, 0, j), type) && this.isTypeAt(world, pos.add(i, 0, j + 1), type) && this.isTypeAt(world, pos.add(i + 1, 0, j + 1), type);
    }

    public boolean isTypeAt(World world, BlockPos pos, BlockPlanks2.PlanksType2 type) {
        IBlockState state = world.getBlockState(pos);
        return state.getBlock() == this && state.getValue(VARIANT) == type;
    }

    @Override
    public int damageDropped(IBlockState state) {
        return state.getValue(VARIANT).getMetadata();
    }

    @Override
    public void getSubBlocks(CreativeTabs tab, NonNullList<ItemStack> items) {}

    @Override
    public boolean canGrow(Location location, boolean isClient) {
        return true;
    }

    @Override
    public boolean canUseBonemeal(Location location, Random random) {
        return (double) random.nextFloat() < 0.45D;
    }

    @Override
    public void grow(Location location, Random random) {
        this.grow(location.getWorld(), location.getPos(), location.getBlockState(), random);
    }

    @Override
    public IBlockState getStateFromMeta(int meta) {
        return this.getDefaultState().withProperty(VARIANT, BlockPlanks2.PlanksType2.byMetadata(meta & 7)).withProperty(STAGE, (meta & 8) >> 3);
    }

    @Override
    public int getMetaFromState(IBlockState state) {
        return state.getValue(VARIANT).getMetadata() | state.getValue(STAGE) << 3;
    }

    @Override
    protected BlockStateContainer createBlockState() {
        return new BlockStateContainer(this, VARIANT, STAGE);
    }

    @Override
    @SideOnly(Side.CLIENT)
    public void registerModel(ModelManager modelManager) {
        modelManager.registerStateMapper(this, new StateMap.Builder().withName(VARIANT).ignore(STAGE).withSuffix("_sapling").build());
        Item item = Item.getItemFromBlock(this);
        modelManager.registerItemVariants(item, "pine_sapling", "willow_sapling");
        for (BlockPlanks2.PlanksType2 type : BlockPlanks2.PlanksType2.values()) {
            modelManager.registerItemModel(item, type.getMetadata(), type.getName() + "_sapling");
        }
    }
}