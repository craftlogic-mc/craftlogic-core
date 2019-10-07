package ru.craftlogic.common.block;

import net.minecraft.block.BlockPlanks;
import net.minecraft.block.BlockSlab;
import net.minecraft.block.SoundType;
import net.minecraft.block.material.MapColor;
import net.minecraft.block.material.Material;
import net.minecraft.block.properties.IProperty;
import net.minecraft.block.properties.PropertyEnum;
import net.minecraft.block.state.BlockStateContainer;
import net.minecraft.block.state.IBlockState;
import net.minecraft.client.renderer.block.statemap.StateMap;
import net.minecraft.creativetab.CreativeTabs;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.util.NonNullList;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.IBlockAccess;
import net.minecraft.world.World;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;
import ru.craftlogic.api.CraftBlocks;
import ru.craftlogic.api.model.ModelManager;
import ru.craftlogic.api.model.ModelRegistrar;
import ru.craftlogic.common.block.BlockPlanks2.PlanksType2;

import java.util.Random;

public class BlockWoodSlab2 extends BlockSlab implements ModelRegistrar {
    public static final PropertyEnum<PlanksType2> VARIANT = PropertyEnum.create("variant", PlanksType2.class);
    private boolean isDouble;

    public BlockWoodSlab2(boolean isDouble) {
        super(Material.WOOD);
        this.isDouble = isDouble;
        IBlockState baseState = getBlockState().getBaseState();

        if (!isDouble()) {
            baseState = baseState.withProperty(HALF, BlockSlab.EnumBlockHalf.BOTTOM);
        }

        setRegistryName(isDouble() ? "double_wooden_slab2" : "wooden_slab2");
        setDefaultState(baseState.withProperty(VARIANT, PlanksType2.PINE));
        setCreativeTab(CreativeTabs.BUILDING_BLOCKS);
        setSoundType(SoundType.WOOD);
    }

    @Override
    public boolean isDouble() {
        return isDouble;
    }

    @Override
    public MapColor getMapColor(IBlockState state, IBlockAccess worldIn, BlockPos pos) {
        return state.getValue(VARIANT).getMapColor();
    }

    @Override
    public Item getItemDropped(IBlockState state, Random rand, int fortune) {
        return Item.getItemFromBlock(CraftBlocks.WOODEN_SLAB2);
    }

    @Override
    public ItemStack getItem(World worldIn, BlockPos pos, IBlockState state) {
        return new ItemStack(CraftBlocks.WOODEN_SLAB2, 1, state.getValue(VARIANT).getMetadata());
    }

    @Override
    public String getTranslationKey(int meta) {
        return "tile.wooden_slab." + PlanksType2.byMetadata(meta).getName();
    }

    @Override
    public IProperty<?> getVariantProperty() {
        return VARIANT;
    }

    @Override
    public Comparable<?> getTypeForItem(ItemStack stack) {
        return BlockPlanks.EnumType.byMetadata(stack.getMetadata() & 7);
    }

    @Override
    public void getSubBlocks(CreativeTabs tab, NonNullList<ItemStack> items) {
        for (PlanksType2 type : PlanksType2.values()) {
            items.add(new ItemStack(this, 1, type.getMetadata()));
        }
    }

    @Override
    public IBlockState getStateFromMeta(int meta) {
        IBlockState state = getDefaultState().withProperty(VARIANT, PlanksType2.byMetadata(meta & 7));

        if (!isDouble()) {
            state = state.withProperty(HALF, (meta & 8) == 0 ? BlockSlab.EnumBlockHalf.BOTTOM : BlockSlab.EnumBlockHalf.TOP);
        }

        return state;
    }

    @Override
    public int getMetaFromState(IBlockState state) {
        int meta = state.getValue(VARIANT).getMetadata();

        if (!isDouble() && state.getValue(HALF) == BlockSlab.EnumBlockHalf.TOP) {
            meta |= 8;
        }

        return meta;
    }

    @Override
    protected BlockStateContainer createBlockState() {
        return this.isDouble() ? new BlockStateContainer(this, VARIANT) : new BlockStateContainer(this, HALF, VARIANT);
    }

    @Override
    public int damageDropped(IBlockState state) {
        return state.getValue(VARIANT).getMetadata();
    }

    @Override
    @SideOnly(Side.CLIENT)
    public void registerModel(ModelManager modelManager) {
        if (!isDouble()) {
            modelManager.registerStateMapper(this, new StateMap.Builder().withName(VARIANT).ignore(VARIANT).withSuffix("_slab").build());
            Item item = Item.getItemFromBlock(this);
            modelManager.registerItemVariants(item, "pine_slab", "willow_slab");
            modelManager.registerItemModel(item, 0, "pine_slab");
            modelManager.registerItemModel(item, 1, "willow_slab");
        } else {
            modelManager.registerStateMapper(this, new StateMap.Builder().withName(VARIANT).ignore(VARIANT).ignore(HALF).withSuffix("_double_slab").build());
        }
    }
}