package ru.craftlogic.common.block;

import net.minecraft.block.BlockStone.EnumType;
import net.minecraft.block.SoundType;
import net.minecraft.block.material.Material;
import net.minecraft.block.properties.IProperty;
import net.minecraft.block.properties.PropertyEnum;
import net.minecraft.block.state.IBlockState;
import net.minecraft.client.renderer.block.statemap.DefaultStateMapper;
import net.minecraft.creativetab.CreativeTabs;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.util.NonNullList;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;
import ru.craftlogic.api.model.ModelManager;

public class BlockBarrelStone extends BlockBarrel {
    public static final PropertyEnum<EnumType> VARIANT = PropertyEnum.create("variant", EnumType.class, EnumType::isNatural);

    public BlockBarrelStone() {
        super(Material.ROCK, "barrel_stone", 1.5F);
        this.setResistance(10F);
        this.setSoundType(SoundType.STONE);
        this.setHasSubtypes(true);
    }

    @Override
    public IBlockState getStateFromMeta(int meta) {
        return this.getDefaultState()
                .withProperty(VARIANT, EnumType.byMetadata(meta > 1 ? meta * 2 - 1 : meta))
                .withProperty(CLOSED, (meta & 8) > 0);
    }

    @Override
    public int getMetaFromState(IBlockState state) {
        int meta = state.getValue(VARIANT).getMetadata();
        return (meta > 1 ? (meta + 1) / 2 : meta) | (state.getValue(CLOSED) ? 8 : 0);
    }

    @Override
    protected IProperty[] getProperties() {
        return new IProperty[] {VARIANT, CLOSED};
    }

    @Override
    @SideOnly(Side.CLIENT)
    public void registerModel(ModelManager modelManager) {
        modelManager.registerStateMapper(this, new DefaultStateMapper());
        int i = 0;
        for (EnumType stoneType : EnumType.values()) {
            if (stoneType.isNatural()) {
                String model = "barrel/" + stoneType.getName();
                modelManager.registerItemModel(Item.getItemFromBlock(this), i++, model);
            }
        }
    }

    @Override
    public void getSubBlocks(CreativeTabs tab, NonNullList<ItemStack> items) {
        for (EnumType stoneType : EnumType.values()) {
            if (stoneType.isNatural()) {
                int meta = stoneType.getMetadata();
                items.add(new ItemStack(this, 1, meta > 1 ? (meta + 1) / 2 : meta));
            }
        }
    }
}
