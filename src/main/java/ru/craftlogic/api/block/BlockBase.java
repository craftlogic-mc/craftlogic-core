package ru.craftlogic.api.block;

import net.minecraft.block.Block;
import net.minecraft.block.material.Material;
import net.minecraft.block.state.BlockFaceShape;
import net.minecraft.block.state.IBlockState;
import net.minecraft.creativetab.CreativeTabs;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.init.Blocks;
import net.minecraft.init.Items;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.EnumHand;
import net.minecraft.util.math.AxisAlignedBB;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.RayTraceResult;
import net.minecraft.util.math.Vec3d;
import net.minecraft.world.IBlockAccess;
import net.minecraft.world.World;
import net.minecraftforge.fluids.Fluid;
import net.minecraftforge.fluids.FluidRegistry;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;
import ru.craftlogic.api.block.holders.TileEntityHolder;
import ru.craftlogic.api.item.ItemBlockBase;
import ru.craftlogic.api.model.ModelAutoReg;
import ru.craftlogic.api.model.ModelManager;
import ru.craftlogic.api.tile.TileEntityBase;
import ru.craftlogic.api.world.Location;
import ru.craftlogic.api.world.LocationReadOnly;

import javax.annotation.Nullable;
import java.util.Random;

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

    @Override final
    public boolean eventReceived(IBlockState state, World world, BlockPos pos, int id, int value) {
        return this.eventReceived(new Location(world, pos), id, value);
    }

    protected boolean eventReceived(Location location, int id, int value) {
        TileEntity tile = location.getTileEntity();
        return tile != null && tile.receiveClientEvent(id, value);
    }

    @Override final
    public boolean onBlockActivated(World world, BlockPos pos, IBlockState state, EntityPlayer player, EnumHand hand, EnumFacing side, float hitX, float hitY, float hitZ) {
        return this.onBlockActivated(new Location(world, pos), player, hand, new RayTraceResult(new Vec3d(hitX, hitY, hitZ), side, pos));
    }

    protected boolean onBlockActivated(Location location, EntityPlayer player, EnumHand hand, RayTraceResult target) {
        TileEntityBase tile = location.getTileEntity(TileEntityBase.class);
        return tile != null && tile.onActivated(player, hand, target);
    }

    @Override final
    public void onBlockPlacedBy(World world, BlockPos pos, IBlockState state, EntityLivingBase placer, ItemStack stack) {
        this.onBlockPlacedBy(new Location(world, pos), placer, stack);
    }

    protected void onBlockPlacedBy(Location location, EntityLivingBase placer, ItemStack stack) {
        TileEntityBase tile = location.getTileEntity(TileEntityBase.class);
        if (tile != null) {
            tile.onPlacedBy(placer, stack);
        }
    }

    @Override final
    public IBlockState getActualState(IBlockState state, IBlockAccess blockAccessor, BlockPos pos) {
        return this.getActualState(new LocationReadOnly(blockAccessor, pos, state));
    }

    protected IBlockState getActualState(Location location) {
        TileEntityBase tile = location.getTileEntity(TileEntityBase.class);
        return tile != null ? tile.getActualState(location.getBlockState()) : location.getBlockState();
    }

    @Override final
    public boolean isSideSolid(IBlockState state, IBlockAccess blockAccessor, BlockPos pos, EnumFacing side) {
        return this.getBlockFaceShape(blockAccessor, state, pos, side) == BlockFaceShape.SOLID;
    }

    @Override final
    public BlockFaceShape getBlockFaceShape(IBlockAccess blockAccessor, IBlockState state, BlockPos pos, EnumFacing side) {
        return this.getBlockFaceShape(new LocationReadOnly(blockAccessor, pos, state), side);
    }

    protected BlockFaceShape getBlockFaceShape(Location location, EnumFacing side) {
        return BlockFaceShape.SOLID;
    }

    @Override final
    public AxisAlignedBB getBoundingBox(IBlockState state, IBlockAccess blockAccessor, BlockPos pos) {
        return state.getBlock() == this ? this.getBoundingBox(new LocationReadOnly(blockAccessor, pos, state)) : FULL_BLOCK_AABB;
    }

    protected AxisAlignedBB getBoundingBox(Location location) {
        return FULL_BLOCK_AABB;
    }

    @Nullable
    @Override
    public AxisAlignedBB getCollisionBoundingBox(IBlockState state, IBlockAccess blockAccessor, BlockPos pos) {
        return this.getCollisionBoundingBox(new LocationReadOnly(blockAccessor, pos, state));
    }

    @Nullable
    public AxisAlignedBB getCollisionBoundingBox(Location location) {
        return this.getBoundingBox(location);
    }

    @Override final
    public void randomTick(World world, BlockPos pos, IBlockState state, Random rand) {
        this.randomTick(new Location(world, pos), rand);
    }

    protected void randomTick(Location location, Random rand) {
        TileEntityBase tile = location.getTileEntity(TileEntityBase.class);
        if (tile != null) {
            tile.randomTick(rand);
        }
    }

    @Override
    public boolean canPlaceBlockAt(World world, BlockPos pos) {
        return this.canPlaceBlockAt(new LocationReadOnly(world, pos, null));
    }

    public boolean canPlaceBlockAt(Location location) {
        return true;
    }

    @Override final
    public void neighborChanged(IBlockState state, World world, BlockPos pos, Block neighborBlock, BlockPos neighborPos) {
        this.neighborChanged(new Location(world, pos), new Location(world, neighborPos), neighborBlock);
    }

    public void neighborChanged(Location selfLocation, Location neighborLocation, Block neighborBlock) {}

    @Override final
    public void updateTick(World world, BlockPos pos, IBlockState state, Random rand) {
        this.updateTick(new Location(world, pos), rand);
    }

    protected void updateTick(Location location, Random rand) {}

    @Override final
    public void breakBlock(World world, BlockPos pos, IBlockState state) {
        this.breakBlock(new Location(world, pos), state);
    }

    public void breakBlock(Location location, IBlockState state) {
        if (this.hasTileEntity(state)) {
            location.removeTileEntity();
        }
    }

    @Override final
    public IBlockState getStateForPlacement(World world, BlockPos pos, EnumFacing side, float hitX, float hitY, float hitZ, int meta, EntityLivingBase placer) {
        return this.getStateForPlacement(
            new Location(world, pos),
            new RayTraceResult(new Vec3d(hitX, hitY, hitZ), side, pos),
                meta, placer
        );
    }

    public IBlockState getStateForPlacement(Location location, RayTraceResult target, int meta, EntityLivingBase placer) {
        return this.getStateFromMeta(meta);
    }

    @Override final
    public boolean canConnectRedstone(IBlockState state, IBlockAccess blockAccessor, BlockPos pos, @Nullable EnumFacing side) {
        return this.canConnectRedstone(new LocationReadOnly(blockAccessor, pos, state), side);
    }

    public boolean canConnectRedstone(Location location, @Nullable EnumFacing side) {
        return location.getBlockState().canProvidePower() && side == null;
    }

    @Override final
    public int getWeakPower(IBlockState state, IBlockAccess blockAccessor, BlockPos pos, EnumFacing side) {
        return this.getWeakPower(new LocationReadOnly(blockAccessor, pos, state), side);
    }

    public int getWeakPower(Location location, EnumFacing side) {
        return 0;
    }

    @Override final
    public int getStrongPower(IBlockState state, IBlockAccess blockAccessor, BlockPos pos, EnumFacing side) {
        return this.getStrongPower(new LocationReadOnly(blockAccessor, pos, state), side);
    }

    public int getStrongPower(Location location, EnumFacing side) {
        return 0;
    }

    @Override final
    public boolean hasTileEntity(IBlockState state) {
        return this instanceof TileEntityHolder && ((TileEntityHolder)this).getTileEntityInfo(state) != null;
    }

    @Nullable
    @Override final
    public TileEntity createTileEntity(World world, IBlockState state) {
        return this instanceof TileEntityHolder ?
            ((TileEntityHolder)this).getTileEntityInfo(state).create(world, state) : null;
    }

    @Override
    public int damageDropped(IBlockState state) {
        return this.hasSubtypes ? this.getMetaFromState(state) : 0;
    }

    @Override
    @SideOnly(Side.CLIENT)
    public void randomDisplayTick(IBlockState state, World world, BlockPos pos, Random rand) {
        this.randomDisplayTick(new LocationReadOnly(world, pos, state), rand);
    }

    @SideOnly(Side.CLIENT)
    public void randomDisplayTick(Location location, Random rand) {
        TileEntityBase tile = location.getTileEntity(TileEntityBase.class);
        if (tile != null) {
            tile.randomDisplayTick(rand);
        }
    }

    @Override final
    public int getFlammability(IBlockAccess blockAccessor, BlockPos pos, EnumFacing side) {
        return this.getFlammability(new LocationReadOnly(blockAccessor, pos, null), side);
    }

    public int getFlammability(Location location, EnumFacing side) {
        return Blocks.FIRE.getFlammability(this);
    }

    @Override final
    public boolean isFlammable(IBlockAccess blockAccessor, BlockPos pos, EnumFacing side) {
        return this.isFlammable(new LocationReadOnly(blockAccessor, pos, null), side);
    }

    public boolean isFlammable(Location location, EnumFacing side) {
        return this.getFlammability(location, side) > 0;
    }

    @Override final
    public int getFireSpreadSpeed(IBlockAccess blockAccessor, BlockPos pos, EnumFacing side) {
        return this.getFireSpreadSpeed(new LocationReadOnly(blockAccessor, pos, null), side);
    }

    @Override final
    public void fillWithRain(World world, BlockPos pos) {
        this.fillWithRain(world, pos, FluidRegistry.WATER);
    }

    final
    public void fillWithRain(World world, BlockPos pos, Fluid fluid) {
        this.fillWithRain(new Location(world, pos), fluid);
    }

    protected void fillWithRain(Location location, Fluid fluid) {
        TileEntityBase tile = location.getTileEntity(TileEntityBase.class);
        if (tile != null) {
            tile.fillWithRain(fluid);
        }
    }

    public int getFireSpreadSpeed(Location location, EnumFacing side) {
        return Blocks.FIRE.getEncouragement(this);
    }

    @Override final
    public int getLightValue(IBlockState state, IBlockAccess blockAccessor, BlockPos pos) {
        return this.getLightValue(new LocationReadOnly(blockAccessor, pos, state));
    }

    protected int getLightValue(Location location) {
        return location.getBlockState().getLightValue();
    }

    protected Item asItem() {
        return Item.getItemFromBlock(this);
    }
}
