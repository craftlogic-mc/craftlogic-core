package ru.craftlogic.api.block;

import net.minecraft.block.Block;
import net.minecraft.block.material.Material;
import net.minecraft.block.state.BlockFaceShape;
import net.minecraft.block.state.IBlockState;
import net.minecraft.creativetab.CreativeTabs;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.init.Items;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.EnumHand;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.IBlockAccess;
import net.minecraft.world.World;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;
import ru.craftlogic.api.ModelAutoReg;
import ru.craftlogic.api.block.holders.TileEntityHolder;
import ru.craftlogic.api.item.ItemBlockBase;
import ru.craftlogic.api.tile.TileEntityBase;
import ru.craftlogic.client.ModelManager;

import javax.annotation.Nullable;

public class BlockBase extends Block implements ModelAutoReg {
    private boolean hasSubtypes;

    public BlockBase(Material material, String name, float hardness, CreativeTabs tab) {
        super(material);
        this.setCreativeTab(tab);
        this.setHardness(hardness);
        this.setRegistryName(name);
        this.setUnlocalizedName(name);
    }

    public BlockBase setHasSubtypes(boolean hasSubtypes) {
        this.hasSubtypes = hasSubtypes;
        return this;
    }

    public boolean getHasSubtypes() {
        return this.hasSubtypes;
    }

    public String getUnlocalizedName(ItemStack stack) {
        return this.getUnlocalizedName();
    }

    @Override
    @SideOnly(Side.CLIENT)
    public void registerModel(ModelManager modelManager) {
        Item item = this.asItem();
        if (item != Items.AIR && item instanceof ItemBlockBase) {
            modelManager.registerItemModel(item);
        }
    }

    @Override
    public boolean eventReceived(IBlockState state, World world, BlockPos pos, int id, int value) {
        TileEntity tile = world.getTileEntity(pos);
        return tile != null && tile.receiveClientEvent(id, value);
    }

    @Override
    public boolean onBlockActivated(World world, BlockPos pos, IBlockState state, EntityPlayer player, EnumHand hand, EnumFacing side, float hitX, float hitY, float hitZ) {
        TileEntity tile = world.getTileEntity(pos);
        return tile instanceof TileEntityBase && ((TileEntityBase) tile).onActivated(player, hand, side, hitX, hitY, hitZ);
    }

    @Override
    public void onBlockPlacedBy(World world, BlockPos pos, IBlockState state, EntityLivingBase placer, ItemStack stack) {
        TileEntity tile = world.getTileEntity(pos);
        if (tile instanceof TileEntityBase) {
            ((TileEntityBase) tile).onPlacedBy(placer, stack);
        }
    }

    @Override
    public IBlockState getActualState(IBlockState state, IBlockAccess blockAccessor, BlockPos pos) {
        TileEntity tile = blockAccessor.getTileEntity(pos);
        return tile instanceof TileEntityBase ? ((TileEntityBase) tile).getActualState(state) : state;
    }

    @Override
    public final boolean isSideSolid(IBlockState state, IBlockAccess blockAccessor, BlockPos pos, EnumFacing side) {
        return this.getBlockFaceShape(blockAccessor, state, pos, side) == BlockFaceShape.SOLID;
    }

    @Override
    public final boolean hasTileEntity(IBlockState state) {
        return this instanceof TileEntityHolder && ((TileEntityHolder)this).getAssociatedTileEntityType(state) != null;
    }

    @Nullable
    @Override
    public final TileEntity createTileEntity(World world, IBlockState state) {
        return this instanceof TileEntityHolder ?
                ((TileEntityHolder)this).getAssociatedTileEntityType(state).create(world, state) : null;
    }

    @Override
    public int damageDropped(IBlockState state) {
        return this.hasSubtypes ? this.getMetaFromState(state) : 0;
    }

    protected Item asItem() {
        return Item.getItemFromBlock(this);
    }
}
