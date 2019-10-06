package ru.craftlogic.common.block;

import net.minecraft.block.BlockLeaves;
import net.minecraft.block.BlockPlanks;
import net.minecraft.block.properties.PropertyEnum;
import net.minecraft.block.state.BlockStateContainer;
import net.minecraft.block.state.IBlockState;
import net.minecraft.client.renderer.block.statemap.StateMap;
import net.minecraft.creativetab.CreativeTabs;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.init.Blocks;
import net.minecraft.init.Items;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.stats.StatList;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.BlockRenderLayer;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.NonNullList;
import net.minecraft.util.math.AxisAlignedBB;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.ColorizerFoliage;
import net.minecraft.world.IBlockAccess;
import net.minecraft.world.World;
import net.minecraft.world.biome.BiomeColorHelper;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;
import ru.craftlogic.api.CraftBlocks;
import ru.craftlogic.api.block.Colored;
import ru.craftlogic.api.model.ModelManager;
import ru.craftlogic.api.model.ModelRegistrar;
import ru.craftlogic.api.world.Location;

import javax.annotation.Nullable;
import java.util.Random;

public class BlockLeaves3 extends BlockLeaves implements ModelRegistrar, Colored {
    public static final PropertyEnum<BlockPlanks2.PlanksType2> VARIANT = PropertyEnum.create("variant", BlockPlanks2.PlanksType2.class);

    public BlockLeaves3() {
        setRegistryName("leaves3");
        setDefaultState(getBlockState().getBaseState()
            .withProperty(VARIANT, BlockPlanks2.PlanksType2.PINE)
            .withProperty(CHECK_DECAY, true)
            .withProperty(DECAYABLE, true)
        );
    }

    @Override
    @SideOnly(Side.CLIENT)
    public int getBlockColor(@Nullable Location location, IBlockState state, int tint) {
        return location != null ? BiomeColorHelper.getFoliageColorAtPos(location.getBlockAccessor(), location.getPos()) : ColorizerFoliage.getFoliageColorBasic();
    }

    @Override
    public boolean isOpaqueCube(IBlockState state) {
        return Blocks.LEAVES.isOpaqueCube(Blocks.LEAVES.getDefaultState());
    }

    @SideOnly(Side.CLIENT)
    @Override
    public BlockRenderLayer getRenderLayer() {
        return Blocks.LEAVES.getRenderLayer();
    }

    @SideOnly(Side.CLIENT)
    @Override
    public boolean shouldSideBeRendered(IBlockState state, IBlockAccess blockAccessor, BlockPos pos, EnumFacing side) {
        if (isOpaqueCube(state) && blockAccessor.getBlockState(pos.offset(side)).getBlock() == this) {
            return false;
        } else {
            AxisAlignedBB bb = state.getBoundingBox(blockAccessor, pos);
            switch (side) {
                case DOWN:
                    if (bb.minY > 0) return true;
                    break;
                case UP:
                    if (bb.maxY < 1) return true;
                    break;
                case NORTH:
                    if (bb.minZ > 0) return true;
                    break;
                case SOUTH:
                    if (bb.maxZ < 1) return true;
                    break;
                case WEST:
                    if (bb.minX > 0) return true;
                    break;
                case EAST:
                    if (bb.maxX < 1) return true;
            }
            return !blockAccessor.getBlockState(pos.offset(side)).doesSideBlockRendering(blockAccessor, pos.offset(side), side.getOpposite());
        }
    }

    @Override
    public Item getItemDropped(IBlockState state, Random rand, int fortune) {
        return Item.getItemFromBlock(CraftBlocks.SAPLING2);
    }

    @Override
    protected void dropApple(World world, BlockPos pos, IBlockState state, int chance) {
    }

    @Override
    public int damageDropped(IBlockState state) {
        return state.getValue(VARIANT).getMetadata();
    }

    @Override
    public ItemStack getItem(World world, BlockPos pos, IBlockState state) {
        return new ItemStack(this, 1, state.getBlock().getMetaFromState(state) & 3);
    }

    @Override
    public void getSubBlocks(CreativeTabs itemIn, NonNullList<ItemStack> items) {
    }

    @Override
    protected ItemStack getSilkTouchDrop(IBlockState state) {
        return new ItemStack(Item.getItemFromBlock(this), 1, state.getValue(VARIANT).getMetadata());
    }

    @Override
    public IBlockState getStateFromMeta(int meta) {
        return this.getDefaultState()
            .withProperty(VARIANT, BlockPlanks2.PlanksType2.byMetadata(meta & 3))
            .withProperty(DECAYABLE, (meta & 4) == 0)
            .withProperty(CHECK_DECAY, (meta & 8) > 0);
    }

    @Override
    public int getMetaFromState(IBlockState state) {
        int meta = state.getValue(VARIANT).getMetadata();

        if (!state.getValue(DECAYABLE)) {
            meta |= 4;
        }

        if (state.getValue(CHECK_DECAY)) {
            meta |= 8;
        }

        return meta;
    }

    @Override
    public BlockPlanks.EnumType getWoodType(int meta) {
        return null;
    }

    @Override
    protected BlockStateContainer createBlockState() {
        return new BlockStateContainer(this, VARIANT, CHECK_DECAY, DECAYABLE);
    }

    public String getTranslationKey(ItemStack item) {
        return BlockPlanks2.PlanksType2.byMetadata(item.getMetadata() & 3).getTranslationKey();
    }

    @Override
    public void harvestBlock(World world, EntityPlayer player, BlockPos pos, IBlockState state, @Nullable TileEntity te, ItemStack stack) {
        if (!world.isRemote && stack.getItem() == Items.SHEARS) {
            player.addStat(StatList.getBlockStats(this));
            spawnAsEntity(world, pos, new ItemStack(Item.getItemFromBlock(this), 1, state.getValue(VARIANT).getMetadata()));
        } else {
            super.harvestBlock(world, player, pos, state, te, stack);
        }
    }

    @Override
    public NonNullList<ItemStack> onSheared(ItemStack item, net.minecraft.world.IBlockAccess world, BlockPos pos, int fortune) {
        return NonNullList.withSize(1, new ItemStack(this, 1, world.getBlockState(pos).getValue(VARIANT).getMetadata()));
    }

    @Override
    @SideOnly(Side.CLIENT)
    public void registerModel(ModelManager modelManager) {
        modelManager.registerStateMapper(this, new StateMap.Builder().withName(VARIANT).ignore(CHECK_DECAY).ignore(DECAYABLE).withSuffix("_leaves").build());
        Item item = Item.getItemFromBlock(this);
        modelManager.registerItemVariants(item, "pine_leaves", "willow_leaves");
        for (BlockPlanks2.PlanksType2 type : BlockPlanks2.PlanksType2.values()) {
            modelManager.registerItemModel(item, type.getMetadata(), type.getName() + "_leaves");
        }
    }
}
