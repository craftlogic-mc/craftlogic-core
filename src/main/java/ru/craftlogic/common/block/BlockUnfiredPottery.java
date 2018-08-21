package ru.craftlogic.common.block;

import net.minecraft.block.BlockDirt;
import net.minecraft.block.SoundType;
import net.minecraft.block.material.Material;
import net.minecraft.block.properties.IProperty;
import net.minecraft.block.properties.PropertyBool;
import net.minecraft.block.properties.PropertyEnum;
import net.minecraft.block.state.BlockStateContainer;
import net.minecraft.block.state.IBlockState;
import net.minecraft.client.renderer.block.model.ModelResourceLocation;
import net.minecraft.creativetab.CreativeTabs;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.init.Blocks;
import net.minecraft.init.Items;
import net.minecraft.init.SoundEvents;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.util.BlockRenderLayer;
import net.minecraft.util.EnumHand;
import net.minecraft.util.NonNullList;
import net.minecraft.util.SoundCategory;
import net.minecraft.util.math.AxisAlignedBB;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.RayTraceResult;
import net.minecraft.world.IBlockAccess;
import net.minecraft.world.World;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;
import ru.craftlogic.api.block.BlockBase;
import ru.craftlogic.api.block.holders.TileEntityHolder;
import ru.craftlogic.api.model.ModelManager;
import ru.craftlogic.api.util.Nameable;
import ru.craftlogic.api.util.TileEntityInfo;
import ru.craftlogic.api.world.Location;
import ru.craftlogic.api.CraftBlocks;
import ru.craftlogic.common.tileentity.TileEntityUnfiredPottery;

import java.util.HashMap;
import java.util.Map;

import static ru.craftlogic.api.CraftAPI.MOD_ID;

public class BlockUnfiredPottery extends BlockBase implements TileEntityHolder<TileEntityUnfiredPottery> {
    public static final PropertyEnum<PotteryType> VARIANT = PropertyEnum.create("variant", PotteryType.class);
    public static final PropertyBool COVERED = PropertyBool.create("covered");
    public static final PropertyBool DONE = PropertyBool.create("done");

    public static final AxisAlignedBB CAULDRON_BOUNDING = new AxisAlignedBB(0.125, 0, 0.125, 0.875, 0.625, 0.875);
    public static final AxisAlignedBB VAT_BOUNDING = new AxisAlignedBB(0.0625, 0.0, 0.0625, 0.9375, 0.3125, 0.9375);
    public static final AxisAlignedBB FLOWERPOT_AABB = new AxisAlignedBB(0.3125, 0.0, 0.3125, 0.6875, 0.375, 0.6875);

    public BlockUnfiredPottery() {
        super(Material.CLAY, "unfired_pottery", 0.5F, CreativeTabs.DECORATIONS);
        this.setHasSubtypes(true);
        this.setSoundType(SoundType.GROUND);
        this.setDefaultState(this.blockState.getBaseState()
            .withProperty(VARIANT, PotteryType.CAULDRON)
            .withProperty(COVERED, false)
            .withProperty(DONE, false)
        );
    }

    @Override
    protected boolean onBlockActivated(Location location, EntityPlayer player, EnumHand hand, RayTraceResult target) {
        ItemStack heldItem = player.getHeldItem(hand);
        if (heldItem.getItem() == Item.getItemFromBlock(Blocks.DIRT) && !location.getBlockProperty(COVERED)) {
            if (!location.isWorldRemote()) {
                if (!player.capabilities.isCreativeMode) {
                    heldItem.shrink(1);
                }
                location.playSound(SoundEvents.BLOCK_GRAVEL_PLACE, SoundCategory.BLOCKS, 1F, 1F);
                location.setBlockProperty(COVERED, true);
            }
            return true;
        } else {
            return super.onBlockActivated(location, player, hand, target);
        }
    }

    @Override
    public String getUnlocalizedName(ItemStack stack) {
        switch (PotteryType.get(stack.getMetadata())) {
            case CAULDRON:
                return "tile.unfired_cauldron";
            case SMELTING_VAT:
                return "tile.unfired_smelting_vat";
            case FLOWERPOT:
                return "tile.unfired_flowerpot";
        }
        return super.getUnlocalizedName(stack);
    }

    @Override
    protected AxisAlignedBB getBoundingBox(Location location) {
        if (!location.getBlockProperty(COVERED)) {
            switch (location.getBlockProperty(VARIANT)) {
                case CAULDRON:
                    return CAULDRON_BOUNDING;
                case SMELTING_VAT:
                    return VAT_BOUNDING;
                case FLOWERPOT:
                    return FLOWERPOT_AABB;
            }
        }
        return super.getBoundingBox(location);
    }

    @Override
    public Material getMaterial(IBlockState state) {
        return state.getValue(COVERED) ? Material.GROUND : Material.CLAY;
    }

    @Override
    public boolean isFullBlock(IBlockState state) {
        return state.getValue(COVERED);
    }

    @Override
    public boolean isFullCube(IBlockState state) {
        return state.getValue(COVERED);
    }

    @Override
    public boolean isOpaqueCube(IBlockState state) {
        return state.getValue(COVERED);
    }

    @Override
    public boolean isTopSolid(IBlockState state) {
        return state.getValue(COVERED);
    }

    @Override
    @SideOnly(Side.CLIENT)
    public BlockRenderLayer getBlockLayer() {
        return BlockRenderLayer.CUTOUT;
    }

    @Override
    public void getSubBlocks(CreativeTabs tab, NonNullList<ItemStack> blocks) {
        for (PotteryType type : PotteryType.values()) {
            blocks.add(new ItemStack(this, 1, type.ordinal()));
        }
    }

    @Override
    public boolean canPlaceBlockAt(World world, BlockPos pos) {
        return world.getBlockState(pos.down()).getBlock() == CraftBlocks.FURNACE;
    }

    @Override
    public IBlockState getStateFromMeta(int meta) {
        return this.getDefaultState()
                .withProperty(VARIANT, PotteryType.get(meta & 3))
                .withProperty(COVERED, (meta & 4) > 0)
                .withProperty(DONE, (meta & 8) > 0);
    }

    @Override
    public int getMetaFromState(IBlockState state) {
        int meta = state.getValue(VARIANT).ordinal();
        if (state.getValue(COVERED)) {
            meta |= 4;
        }
        if (state.getValue(DONE)) {
            meta |= 8;
        }
        return meta;
    }

    @Override
    protected BlockStateContainer createBlockState() {
        return new BlockStateContainer(this, VARIANT, COVERED, DONE);
    }

    @Override
    public void registerModel(ModelManager modelManager) {
        Item item = this.asItem();
        modelManager.registerItemModel(item, 0, "unfired_cauldron");
        modelManager.registerItemModel(item, 1, "unfired_smelting_vat");
        modelManager.registerItemModel(item, 2, "unfired_flowerpot");
        modelManager.registerStateMapper(this, (state, mapper) -> {
            Map<IProperty<?>, Comparable<?>> props = new HashMap<>(state.getProperties());
            boolean done = (Boolean) props.remove(DONE);
            boolean covered = (Boolean) props.remove(COVERED);
            if (covered) {
                return new ModelResourceLocation("minecraft:" + (done ? "coarse_dirt" : "dirt"));
            } else {
                String propString = mapper.getPropertyString(props);
                return new ModelResourceLocation(MOD_ID + ":unfired_pottery", propString);
            }
        });
    }

    @Override
    public void getDrops(NonNullList<ItemStack> drops, IBlockAccess blockAccessor, BlockPos pos, IBlockState state, int fortune) {
        boolean done = state.getValue(DONE);
        boolean hasDirt = state.getValue(COVERED);
        PotteryType variant = state.getValue(VARIANT);

        if (done) {
            switch (variant) {
                case CAULDRON:
                    drops.add(new ItemStack(CraftBlocks.CAULDRON));
                    break;
                case SMELTING_VAT:
                    drops.add(new ItemStack(CraftBlocks.SMELTING_VAT));
                    break;
                case FLOWERPOT:
                    drops.add(new ItemStack(Items.FLOWER_POT));
                    break;
            }
            if (hasDirt) {
                IBlockState dirt = Blocks.DIRT.getDefaultState().withProperty(BlockDirt.VARIANT, BlockDirt.DirtType.COARSE_DIRT);
                drops.add(new ItemStack(Blocks.DIRT, 1, Blocks.DIRT.getMetaFromState(dirt)));
            }
        } else {
            drops.add(new ItemStack(Items.CLAY_BALL, variant.getClayCount()));
            if (hasDirt) {
                drops.add(new ItemStack(Blocks.DIRT));
            }
        }
    }

    @Override
    public TileEntityInfo<TileEntityUnfiredPottery> getTileEntityInfo(IBlockState state) {
        return new TileEntityInfo<>(TileEntityUnfiredPottery.class, state, TileEntityUnfiredPottery::new);
    }

    public enum PotteryType implements Nameable {
        CAULDRON(1000, 3),
        SMELTING_VAT(800, 2),
        FLOWERPOT(600, 1);

        private final int requiredTemperature;
        private final int clayCount;

        PotteryType(int requiredTemperature, int clayCount) {
            this.requiredTemperature = requiredTemperature;
            this.clayCount = clayCount;
        }

        public static PotteryType get(int id) {
            return values()[id % values().length];
        }

        public int getRequiredTemperature() {
            return this.requiredTemperature;
        }

        public int getClayCount() {
            return clayCount;
        }
    }
}
