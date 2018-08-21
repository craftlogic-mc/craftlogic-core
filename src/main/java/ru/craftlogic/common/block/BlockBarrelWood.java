package ru.craftlogic.common.block;

import net.minecraft.block.BlockPlanks;
import net.minecraft.block.SoundType;
import net.minecraft.block.material.Material;
import net.minecraft.block.properties.PropertyEnum;
import net.minecraft.block.state.BlockStateContainer;
import net.minecraft.block.state.IBlockState;
import net.minecraft.client.renderer.block.statemap.DefaultStateMapper;
import net.minecraft.creativetab.CreativeTabs;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.util.NonNullList;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;
import ru.craftlogic.api.model.ModelManager;

public class BlockBarrelWood extends BlockBarrel {
    public static final PropertyEnum<BlockPlanks.EnumType> VARIANT = BlockPlanks.VARIANT;

    public BlockBarrelWood() {
        super(Material.WOOD, "barrel_wood", 2F);
        this.setResistance(5F);
        this.setSoundType(SoundType.WOOD);
        this.setHasSubtypes(true);
    }

    @Override
    public int damageDropped(IBlockState state) {
        return state.getValue(VARIANT).getMetadata();
    }

    @Override
    public IBlockState getStateFromMeta(int meta) {
        return this.getDefaultState()
                .withProperty(VARIANT, BlockPlanks.EnumType.byMetadata(meta & 7))
                .withProperty(CLOSED, (meta & 8) > 0);
    }

    @Override
    public int getMetaFromState(IBlockState state) {
        int meta = state.getValue(VARIANT).getMetadata();
        if (state.getValue(CLOSED)) {
            meta |= 8;
        }
        return meta;
    }

    @Override
    protected BlockStateContainer createBlockState() {
        return new BlockStateContainer(this, VARIANT, CLOSED);
    }

    @Override
    @SideOnly(Side.CLIENT)
    public void registerModel(ModelManager modelManager) {
        modelManager.registerStateMapper(this, new DefaultStateMapper());
        for (BlockPlanks.EnumType planksType : BlockPlanks.EnumType.values()) {
            String model = "barrel/" + planksType.getName();
            modelManager.registerItemModel(Item.getItemFromBlock(this), planksType.ordinal(), model);
        }
    }

    @Override
    public void getSubBlocks(CreativeTabs tab, NonNullList<ItemStack> items) {
        for (BlockPlanks.EnumType planksType : BlockPlanks.EnumType.values()) {
            items.add(new ItemStack(this, 1, planksType.getMetadata()));
        }
    }
}