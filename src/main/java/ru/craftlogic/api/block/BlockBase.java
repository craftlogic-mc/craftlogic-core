package ru.craftlogic.api.block;

import com.google.common.base.Predicate;
import net.minecraft.block.Block;
import net.minecraft.block.SoundType;
import net.minecraft.block.material.Material;
import net.minecraft.block.properties.IProperty;
import net.minecraft.block.state.BlockFaceShape;
import net.minecraft.block.state.BlockStateContainer;
import net.minecraft.block.state.IBlockState;
import net.minecraft.client.particle.ParticleManager;
import net.minecraft.creativetab.CreativeTabs;
import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityLiving;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.init.Blocks;
import net.minecraft.init.Items;
import net.minecraft.item.EnumDyeColor;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.pathfinding.PathNodeType;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.BlockRenderLayer;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.EnumHand;
import net.minecraft.util.NonNullList;
import net.minecraft.util.math.AxisAlignedBB;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.RayTraceResult;
import net.minecraft.util.math.Vec3d;
import net.minecraft.world.Explosion;
import net.minecraft.world.IBlockAccess;
import net.minecraft.world.World;
import net.minecraft.world.WorldServer;
import net.minecraftforge.common.IPlantable;
import net.minecraftforge.fluids.Fluid;
import net.minecraftforge.fluids.FluidRegistry;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;
import ru.craftlogic.api.block.holders.TileEntityHolder;
import ru.craftlogic.api.item.ItemBlockBase;
import ru.craftlogic.api.model.ModelManager;
import ru.craftlogic.api.model.ModelRegistrar;
import ru.craftlogic.api.tile.TileEntityBase;
import ru.craftlogic.api.world.Location;
import ru.craftlogic.api.world.LocationReadOnly;

import javax.annotation.Nullable;
import java.util.List;
import java.util.Random;

public class BlockBase extends Block implements ModelRegistrar {
    private boolean hasSubtypes;

    public BlockBase(Material material, String name, float hardness, CreativeTabs tab) {
        super(material);
        this.setCreativeTab(tab);
        this.setHardness(hardness);
        this.setRegistryName(name);
        this.setTranslationKey(name);
    }

    public BlockBase setHasSubtypes(boolean hasSubtypes) {
        this.hasSubtypes = hasSubtypes;
        return this;
    }

    public boolean getHasSubtypes() {
        return this.hasSubtypes;
    }

    public String getTranslationKey(ItemStack stack) {
        return this.getTranslationKey();
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
        if (tile != null) {
            if (tile.onActivated(player, hand, target)) {
                return true;
            } else if (this instanceof Partial) {
                Partial.Part part = ((Partial) this).getPart(location, target);
                if (part != null) {
                    return part.onActivated(location, player, hand);
                }
            }
        }
        return false;
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
        return getBlockFaceShape(blockAccessor, state, pos, side) == BlockFaceShape.SOLID;
    }

    @Override final
    public BlockFaceShape getBlockFaceShape(IBlockAccess blockAccessor, IBlockState state, BlockPos pos, EnumFacing side) {
        return getBlockFaceShape(new LocationReadOnly(blockAccessor, pos, state), side);
    }

    protected BlockFaceShape getBlockFaceShape(Location location, EnumFacing side) {
        return BlockFaceShape.SOLID;
    }

    @Override final
    public AxisAlignedBB getBoundingBox(IBlockState state, IBlockAccess blockAccessor, BlockPos pos) {
        return state.getBlock() == this ? getBoundingBox(new LocationReadOnly(blockAccessor, pos, state)) : FULL_BLOCK_AABB;
    }

    protected AxisAlignedBB getBoundingBox(Location location) {
        return FULL_BLOCK_AABB;
    }

    @Nullable
    @Override final
    public AxisAlignedBB getCollisionBoundingBox(IBlockState state, IBlockAccess blockAccessor, BlockPos pos) {
        return getCollisionBoundingBox(new LocationReadOnly(blockAccessor, pos, state));
    }

    @Nullable
    public AxisAlignedBB getCollisionBoundingBox(Location location) {
        return getBoundingBox(location);
    }

    @Override final
    public void randomTick(World world, BlockPos pos, IBlockState state, Random rand) {
        randomTick(new Location(world, pos), rand);
    }

    protected void randomTick(Location location, Random rand) {
        TileEntityBase tile = location.getTileEntity(TileEntityBase.class);
        if (tile != null) {
            tile.randomTick(rand);
        }
    }

    @Override final
    public boolean canPlaceBlockAt(World world, BlockPos pos) {
        return this.canPlaceBlockAt(new LocationReadOnly(world, pos, null));
    }

    protected boolean canPlaceBlockAt(Location location) {
        return location.getBlock().isReplaceable(location.getWorld(), location.getPos());
    }

    @Override final
    public boolean canPlaceBlockOnSide(World world, BlockPos pos, EnumFacing side) {
        return canPlaceBlockOnSide(new LocationReadOnly(world, pos, null), side);
    }

    protected boolean canPlaceBlockOnSide(Location location, EnumFacing side) {
        return canPlaceBlockAt(location);
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
        return getStateForPlacement(
            new Location(world, pos),
            new RayTraceResult(new Vec3d(hitX, hitY, hitZ), side, pos),
            meta, placer, null
        );
    }

    @Override final
    public IBlockState getStateForPlacement(World world, BlockPos pos, EnumFacing side, float hitX, float hitY, float hitZ, int meta, EntityLivingBase placer, EnumHand hand) {
        return getStateForPlacement(
            new Location(world, pos),
            new RayTraceResult(new Vec3d(hitX, hitY, hitZ), side, pos),
            meta, placer, hand
        );
    }

    public IBlockState getStateForPlacement(Location location, RayTraceResult target, int meta, EntityLivingBase placer, @Nullable EnumHand hand) {
        return getStateFromMeta(meta);
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
    public ItemStack getPickBlock(IBlockState state, RayTraceResult target, World world, BlockPos pos, EntityPlayer player) {
        return this.getPickBlock(new LocationReadOnly(world, pos, state), target, player);
    }

    protected ItemStack getPickBlock(Location location, RayTraceResult target, EntityPlayer player) {
        return this.getItem(location.getWorld(), location.getPos(), location.getBlockState());
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
        return getLightValue(new LocationReadOnly(blockAccessor, pos, state));
    }

    protected int getLightValue(Location location) {
        return location.getBlockState().getLightValue();
    }

    @Override final
    public boolean isPassable(IBlockAccess blockAccessor, BlockPos pos) {
        return isPassable(new LocationReadOnly(blockAccessor, pos, null));
    }

    protected boolean isPassable(Location location) {
        return !material.blocksMovement();
    }

    @Override final
    public boolean isReplaceable(IBlockAccess blockAccessor, BlockPos pos) {
        return isReplaceable(new LocationReadOnly(blockAccessor, pos, null));
    }

    protected boolean isReplaceable(Location location) {
        return location.getBlockMaterial().isReplaceable();
    }

    @Override final
    public float getBlockHardness(IBlockState state, World world, BlockPos pos) {
        return getBlockHardness(new LocationReadOnly(world, pos, state));
    }

    protected float getBlockHardness(Location location) {
        return blockHardness;
    }

    @Override final
    public int getPackedLightmapCoords(IBlockState state, IBlockAccess blockAccessor, BlockPos pos) {
        return getPackedLightmapCoords(new LocationReadOnly(blockAccessor, pos, state));
    }

    protected int getPackedLightmapCoords(Location location) {
        return super.getPackedLightmapCoords(location.getBlockState(), location.getBlockAccessor(), location.getPos());
    }

    @Override final
    public boolean shouldSideBeRendered(IBlockState state, IBlockAccess blockAccessor, BlockPos pos, EnumFacing side) {
        return shouldSideBeRendered(new LocationReadOnly(blockAccessor, pos, state), side);
    }

    protected boolean shouldSideBeRendered(Location location, EnumFacing side) {
        return super.shouldSideBeRendered(location.getBlockState(), location.getBlockAccessor(), location.getPos(), side);
    }

    @Override final
    public void addCollisionBoxToList(IBlockState state, World world, BlockPos pos, AxisAlignedBB box, List<AxisAlignedBB> boxes, @Nullable Entity entity, boolean flag) {
        addCollisionBoxToList(new LocationReadOnly(world, pos, state), box, boxes, entity, flag);
    }

    protected void addCollisionBoxToList(Location location, AxisAlignedBB box, List<AxisAlignedBB> boxes, @Nullable Entity entity, boolean flag) {
        super.addCollisionBoxToList(location.getBlockState(), location.getWorld(), location.getPos(), box, boxes, entity, flag);
    }

    @Override final
    public AxisAlignedBB getSelectedBoundingBox(IBlockState state, World world, BlockPos pos) {
        return getSelectedBoundingBox(new LocationReadOnly(world, pos, state));
    }

    public AxisAlignedBB getSelectedBoundingBox(Location location) {
        return super.getSelectedBoundingBox(location.getBlockState(), location.getWorld(), location.getPos());
    }

    @Override final
    public void onPlayerDestroy(World world, BlockPos pos, IBlockState state) {
        onPlayerDestroy(new LocationReadOnly(world, pos, state));
    }

    protected void onPlayerDestroy(Location location) {}

    @Override
    public void onBlockAdded(World world, BlockPos pos, IBlockState state) {
        super.onBlockAdded(world, pos, state);
    }

    @Override final
    public float getPlayerRelativeBlockHardness(IBlockState state, EntityPlayer player, World world, BlockPos pos) {
        return getPlayerRelativeBlockHardness(new LocationReadOnly(world, pos, state), player);
    }

    protected float getPlayerRelativeBlockHardness(Location location, EntityPlayer player) {
        return super.getPlayerRelativeBlockHardness(location.getBlockState(), player, location.getWorld(), location.getPos());
    }

    @Override final
    public void dropBlockAsItemWithChance(World world, BlockPos pos, IBlockState state, float chance, int fortune) {
        dropBlockAsItemWithChance(new LocationReadOnly(world, pos, state), chance, fortune);
    }

    protected void dropBlockAsItemWithChance(Location location, float chance, int fortune) {
        super.dropBlockAsItemWithChance(location.getWorld(), location.getPos(), location.getBlockState(), chance, fortune);
    }

    @Override final
    public void dropXpOnBlockBreak(World world, BlockPos pos, int fortune) {
        dropXpOnBlockBreak(new Location(world, pos), fortune);
    }

    protected void dropXpOnBlockBreak(Location location, int fortune) {
        super.dropXpOnBlockBreak(location.getWorld(), location.getPos(), fortune);
    }

    @Nullable
    @Override final
    public RayTraceResult collisionRayTrace(IBlockState state, World world, BlockPos pos, Vec3d start, Vec3d end) {
        return collisionRayTrace(new LocationReadOnly(world, pos, state), start, end);
    }

    protected RayTraceResult collisionRayTrace(Location location, Vec3d start, Vec3d end) {
        return super.collisionRayTrace(location.getBlockState(), location.getWorld(), location.getPos(), start, end);
    }

    @Override final
    public void onExplosionDestroy(World world, BlockPos pos, Explosion explosion) {
        onExplosionDestroy(new Location(world, pos), explosion);
    }

    protected void onExplosionDestroy(Location location, Explosion explosion) { }

    @Override final
    public void onEntityWalk(World world, BlockPos pos, Entity entity) {
        onEntityWalk(new Location(world, pos), entity);
    }

    protected void onEntityWalk(Location location, Entity entity) { }

    @Override final
    public void onBlockClicked(World world, BlockPos pos, EntityPlayer player) {
        onBlockClicked(new Location(world, pos), player);
    }

    protected void onBlockClicked(Location location, EntityPlayer player) { }

    @Override final
    public Vec3d modifyAcceleration(World world, BlockPos pos, Entity entity, Vec3d acceleration) {
        return modifyAcceleration(new Location(world, pos), entity, acceleration);
    }

    public Vec3d modifyAcceleration(Location location, Entity entity, Vec3d acceleration) {
        return acceleration;
    }

    @Override
    public void onEntityCollision(World world, BlockPos pos, IBlockState state, Entity entity) {
        onEntityCollision(new Location(world, pos), entity);
    }

    protected void onEntityCollision(Location location, Entity entity) { }

    @Override final
    public void harvestBlock(World world, EntityPlayer player, BlockPos pos, IBlockState state, @Nullable TileEntity tile, ItemStack item) {
        harvestBlock(new Location(world, pos), state, player, tile, item);
    }

    protected void harvestBlock(Location location, IBlockState state, EntityPlayer player, @Nullable TileEntity tile, ItemStack item) {
        super.harvestBlock(location.getWorld(), player, location.getPos(), state, tile, item);
    }

    @Override final
    public void onFallenUpon(World world, BlockPos pos, Entity entity, float distance) {
        onFallenUpon(new Location(world, pos), entity, distance);
    }

    protected void onFallenUpon(Location location, Entity entity, float distance) {
        super.onFallenUpon(location.getWorld(), location.getPos(), entity, distance);
    }

    @Deprecated
    @Override final
    public ItemStack getItem(World world, BlockPos pos, IBlockState state) {
        return getItem(new LocationReadOnly(world, pos, state));
    }

    public ItemStack getItem(Location location) {
        return super.getItem(location.getWorld(), location.getPos(), location.getBlockState());
    }

    @Deprecated
    @Override final
    public void onBlockHarvested(World world, BlockPos pos, IBlockState state, EntityPlayer player) {
        onBlockHarvested(new Location(world, pos), state, player);
    }

    protected void onBlockHarvested(Location location, IBlockState state, EntityPlayer player) {}

    @Override final
    public Vec3d getOffset(IBlockState state, IBlockAccess blockAccessor, BlockPos pos) {
        return getOffset(new LocationReadOnly(blockAccessor, pos, state));
    }

    public Vec3d getOffset(Location location) {
        return super.getOffset(location.getBlockState(), location.getBlockAccessor(), location.getPos());
    }

    @Override final
    public float getSlipperiness(IBlockState state, IBlockAccess blockAccessor, BlockPos pos, @Nullable Entity entity) {
        return getSlipperiness(new LocationReadOnly(blockAccessor, pos, state), entity);
    }

    public float getSlipperiness(Location location, @Nullable Entity entity) {
        return slipperiness;
    }

    @Override final
    public boolean isLadder(IBlockState state, IBlockAccess blockAccessor, BlockPos pos, EntityLivingBase entity) {
        return isLadder(new LocationReadOnly(blockAccessor, pos, state), entity);
    }

    public boolean isLadder(Location location, EntityLivingBase entity) {
        return false;
    }

    @Override final
    public boolean isNormalCube(IBlockState state, IBlockAccess blockAccessor, BlockPos pos) {
        return super.isNormalCube(state, blockAccessor, pos);
    }

    @Override
    public boolean doesSideBlockRendering(IBlockState state, IBlockAccess blockAccessor, BlockPos pos, EnumFacing side) {
        return super.doesSideBlockRendering(state, blockAccessor, pos, side);
    }

    @Override
    public boolean isBurning(IBlockAccess blockAccessor, BlockPos pos) {
        return super.isBurning(blockAccessor, pos);
    }

    @Override
    public boolean isAir(IBlockState state, IBlockAccess blockAccessor, BlockPos pos) {
        return super.isAir(state, blockAccessor, pos);
    }

    @Override
    public boolean canHarvestBlock(IBlockAccess blockAccessor, BlockPos pos, EntityPlayer player) {
        return super.canHarvestBlock(blockAccessor, pos, player);
    }

    @Override
    public boolean removedByPlayer(IBlockState state, World world, BlockPos pos, EntityPlayer player, boolean drop) {
        return super.removedByPlayer(state, world, pos, player, drop);
    }

    @Override
    public boolean isFireSource(World world, BlockPos pos, EnumFacing side) {
        return super.isFireSource(world, pos, side);
    }

    @Deprecated
    @Override final
    public List<ItemStack> getDrops(IBlockAccess blockAccessor, BlockPos pos, IBlockState state, int fortune) {
        return super.getDrops(blockAccessor, pos, state, fortune);
    }

    @Deprecated
    @Override final
    public void getDrops(NonNullList<ItemStack> items, IBlockAccess blockAccessor, BlockPos pos, IBlockState state, int fortune) {
        addDrops(new LocationReadOnly(blockAccessor, pos, state), items, fortune);
    }

    public void addDrops(Location location, NonNullList<ItemStack> items, int fortune) {
        super.getDrops(items, location.getBlockAccessor(), location.getPos(), location.getBlockState(), fortune);
    }

    @Deprecated
    @Override final
    public boolean canSilkHarvest(World world, BlockPos pos, IBlockState state, EntityPlayer player) {
        return canSilkHarvest(new LocationReadOnly(world, pos, state), player);
    }

    public boolean canSilkHarvest(Location location, EntityPlayer player) {
        return super.canSilkHarvest(location.getWorld(), location.getPos(), location.getBlockState(), player);
    }

    @Override
    public boolean canCreatureSpawn(IBlockState state, IBlockAccess blockAccessor, BlockPos pos, EntityLiving.SpawnPlacementType spawnType) {
        return super.canCreatureSpawn(state, blockAccessor, pos, spawnType);
    }

    @Override
    public boolean isBed(IBlockState state, IBlockAccess blockAccessor, BlockPos pos, @Nullable Entity entity) {
        return super.isBed(state, blockAccessor, pos, entity);
    }

    @Nullable
    @Override
    public BlockPos getBedSpawnPosition(IBlockState p_getBedSpawnPosition_1_, IBlockAccess p_getBedSpawnPosition_2_, BlockPos p_getBedSpawnPosition_3_, @Nullable EntityPlayer p_getBedSpawnPosition_4_) {
        return super.getBedSpawnPosition(p_getBedSpawnPosition_1_, p_getBedSpawnPosition_2_, p_getBedSpawnPosition_3_, p_getBedSpawnPosition_4_);
    }

    @Override
    public void setBedOccupied(IBlockAccess p_setBedOccupied_1_, BlockPos p_setBedOccupied_2_, EntityPlayer p_setBedOccupied_3_, boolean p_setBedOccupied_4_) {
        super.setBedOccupied(p_setBedOccupied_1_, p_setBedOccupied_2_, p_setBedOccupied_3_, p_setBedOccupied_4_);
    }

    @Override
    public EnumFacing getBedDirection(IBlockState p_getBedDirection_1_, IBlockAccess p_getBedDirection_2_, BlockPos p_getBedDirection_3_) {
        return super.getBedDirection(p_getBedDirection_1_, p_getBedDirection_2_, p_getBedDirection_3_);
    }

    @Override
    public boolean isBedFoot(IBlockAccess p_isBedFoot_1_, BlockPos p_isBedFoot_2_) {
        return super.isBedFoot(p_isBedFoot_1_, p_isBedFoot_2_);
    }

    @Override
    public void beginLeavesDecay(IBlockState p_beginLeavesDecay_1_, World p_beginLeavesDecay_2_, BlockPos p_beginLeavesDecay_3_) {
        super.beginLeavesDecay(p_beginLeavesDecay_1_, p_beginLeavesDecay_2_, p_beginLeavesDecay_3_);
    }

    @Override
    public boolean canSustainLeaves(IBlockState p_canSustainLeaves_1_, IBlockAccess p_canSustainLeaves_2_, BlockPos p_canSustainLeaves_3_) {
        return super.canSustainLeaves(p_canSustainLeaves_1_, p_canSustainLeaves_2_, p_canSustainLeaves_3_);
    }

    @Override
    public boolean isLeaves(IBlockState p_isLeaves_1_, IBlockAccess p_isLeaves_2_, BlockPos p_isLeaves_3_) {
        return super.isLeaves(p_isLeaves_1_, p_isLeaves_2_, p_isLeaves_3_);
    }

    @Override
    public boolean canBeReplacedByLeaves(IBlockState p_canBeReplacedByLeaves_1_, IBlockAccess p_canBeReplacedByLeaves_2_, BlockPos p_canBeReplacedByLeaves_3_) {
        return super.canBeReplacedByLeaves(p_canBeReplacedByLeaves_1_, p_canBeReplacedByLeaves_2_, p_canBeReplacedByLeaves_3_);
    }

    @Override
    public boolean isWood(IBlockAccess p_isWood_1_, BlockPos p_isWood_2_) {
        return super.isWood(p_isWood_1_, p_isWood_2_);
    }

    @Override
    public boolean isReplaceableOreGen(IBlockState p_isReplaceableOreGen_1_, IBlockAccess p_isReplaceableOreGen_2_, BlockPos p_isReplaceableOreGen_3_, Predicate<IBlockState> p_isReplaceableOreGen_4_) {
        return super.isReplaceableOreGen(p_isReplaceableOreGen_1_, p_isReplaceableOreGen_2_, p_isReplaceableOreGen_3_, p_isReplaceableOreGen_4_);
    }

    @Override
    public float getExplosionResistance(World p_getExplosionResistance_1_, BlockPos p_getExplosionResistance_2_, @Nullable Entity p_getExplosionResistance_3_, Explosion p_getExplosionResistance_4_) {
        return super.getExplosionResistance(p_getExplosionResistance_1_, p_getExplosionResistance_2_, p_getExplosionResistance_3_, p_getExplosionResistance_4_);
    }

    @Override
    public void onBlockExploded(World p_onBlockExploded_1_, BlockPos p_onBlockExploded_2_, Explosion p_onBlockExploded_3_) {
        super.onBlockExploded(p_onBlockExploded_1_, p_onBlockExploded_2_, p_onBlockExploded_3_);
    }

    @Override
    public boolean canPlaceTorchOnTop(IBlockState p_canPlaceTorchOnTop_1_, IBlockAccess p_canPlaceTorchOnTop_2_, BlockPos p_canPlaceTorchOnTop_3_) {
        return super.canPlaceTorchOnTop(p_canPlaceTorchOnTop_1_, p_canPlaceTorchOnTop_2_, p_canPlaceTorchOnTop_3_);
    }

    @Override
    public boolean isFoliage(IBlockAccess blockAccessor, BlockPos pos) {
        return super.isFoliage(blockAccessor, pos);
    }

    @Override
    public boolean addLandingEffects(IBlockState state, WorldServer world, BlockPos pos, IBlockState p_addLandingEffects_4_, EntityLivingBase entity, int distance) {
        return super.addLandingEffects(state, world, pos, p_addLandingEffects_4_, entity, distance);
    }

    @Override
    public boolean addRunningEffects(IBlockState state, World p_addRunningEffects_2_, BlockPos p_addRunningEffects_3_, Entity p_addRunningEffects_4_) {
        return super.addRunningEffects(state, p_addRunningEffects_2_, p_addRunningEffects_3_, p_addRunningEffects_4_);
    }

    @Override
    @SideOnly(Side.CLIENT)
    public boolean addHitEffects(IBlockState state, World world, RayTraceResult target, ParticleManager particleManager) {
        return super.addHitEffects(state, world, target, particleManager);
    }

    @Override
    @SideOnly(Side.CLIENT)
    public boolean addDestroyEffects(World world, BlockPos pos, ParticleManager particleManager) {
        return super.addDestroyEffects(world, pos, particleManager);
    }

    @Override
    public boolean canSustainPlant(IBlockState state, IBlockAccess blockAccessor, BlockPos pos, EnumFacing side, IPlantable plant) {
        return super.canSustainPlant(state, blockAccessor, pos, side, plant);
    }

    @Override
    public void onPlantGrow(IBlockState state, World world, BlockPos p_onPlantGrow_3_, BlockPos p_onPlantGrow_4_) {
        super.onPlantGrow(state, world, p_onPlantGrow_3_, p_onPlantGrow_4_);
    }

    @Override
    public boolean isFertile(World world, BlockPos pos) {
        return super.isFertile(world, pos);
    }

    @Override
    public int getLightOpacity(IBlockState state, IBlockAccess blockAccessor, BlockPos pos) {
        return super.getLightOpacity(state, blockAccessor, pos);
    }

    @Override
    public boolean canEntityDestroy(IBlockState state, IBlockAccess blockAccessor, BlockPos pos, Entity entity) {
        return super.canEntityDestroy(state, blockAccessor, pos, entity);
    }

    @Override
    public boolean isBeaconBase(IBlockAccess blockAccessor, BlockPos p_isBeaconBase_2_, BlockPos p_isBeaconBase_3_) {
        return super.isBeaconBase(blockAccessor, p_isBeaconBase_2_, p_isBeaconBase_3_);
    }

    @Override
    public boolean rotateBlock(World world, BlockPos pos, EnumFacing rotation) {
        return super.rotateBlock(world, pos, rotation);
    }

    @Nullable
    @Override
    public EnumFacing[] getValidRotations(World world, BlockPos pos) {
        return super.getValidRotations(world, pos);
    }

    @Override
    public float getEnchantPowerBonus(World world, BlockPos pos) {
        return super.getEnchantPowerBonus(world, pos);
    }

    @Override
    public boolean recolorBlock(World p_recolorBlock_1_, BlockPos p_recolorBlock_2_, EnumFacing p_recolorBlock_3_, EnumDyeColor p_recolorBlock_4_) {
        return super.recolorBlock(p_recolorBlock_1_, p_recolorBlock_2_, p_recolorBlock_3_, p_recolorBlock_4_);
    }

    @Override
    public int getExpDrop(IBlockState p_getExpDrop_1_, IBlockAccess p_getExpDrop_2_, BlockPos p_getExpDrop_3_, int p_getExpDrop_4_) {
        return super.getExpDrop(p_getExpDrop_1_, p_getExpDrop_2_, p_getExpDrop_3_, p_getExpDrop_4_);
    }

    @Override
    public void onNeighborChange(IBlockAccess p_onNeighborChange_1_, BlockPos p_onNeighborChange_2_, BlockPos p_onNeighborChange_3_) {
        super.onNeighborChange(p_onNeighborChange_1_, p_onNeighborChange_2_, p_onNeighborChange_3_);
    }

    @Override
    public void observedNeighborChange(IBlockState p_observedNeighborChange_1_, World p_observedNeighborChange_2_, BlockPos p_observedNeighborChange_3_, Block p_observedNeighborChange_4_, BlockPos p_observedNeighborChange_5_) {
        super.observedNeighborChange(p_observedNeighborChange_1_, p_observedNeighborChange_2_, p_observedNeighborChange_3_, p_observedNeighborChange_4_, p_observedNeighborChange_5_);
    }

    @Override
    public boolean shouldCheckWeakPower(IBlockState p_shouldCheckWeakPower_1_, IBlockAccess p_shouldCheckWeakPower_2_, BlockPos p_shouldCheckWeakPower_3_, EnumFacing p_shouldCheckWeakPower_4_) {
        return super.shouldCheckWeakPower(p_shouldCheckWeakPower_1_, p_shouldCheckWeakPower_2_, p_shouldCheckWeakPower_3_, p_shouldCheckWeakPower_4_);
    }

    @Override
    public boolean getWeakChanges(IBlockAccess p_getWeakChanges_1_, BlockPos p_getWeakChanges_2_) {
        return super.getWeakChanges(p_getWeakChanges_1_, p_getWeakChanges_2_);
    }

    @Override
    public IBlockState getExtendedState(IBlockState p_getExtendedState_1_, IBlockAccess p_getExtendedState_2_, BlockPos p_getExtendedState_3_) {
        return super.getExtendedState(p_getExtendedState_1_, p_getExtendedState_2_, p_getExtendedState_3_);
    }

    @Nullable
    @Override
    public Boolean isEntityInsideMaterial(IBlockAccess p_isEntityInsideMaterial_1_, BlockPos p_isEntityInsideMaterial_2_, IBlockState p_isEntityInsideMaterial_3_, Entity p_isEntityInsideMaterial_4_, double p_isEntityInsideMaterial_5_, Material p_isEntityInsideMaterial_7_, boolean p_isEntityInsideMaterial_7_2) {
        return super.isEntityInsideMaterial(p_isEntityInsideMaterial_1_, p_isEntityInsideMaterial_2_, p_isEntityInsideMaterial_3_, p_isEntityInsideMaterial_4_, p_isEntityInsideMaterial_5_, p_isEntityInsideMaterial_7_, p_isEntityInsideMaterial_7_2);
    }

    @Nullable
    @Override
    public Boolean isAABBInsideMaterial(World p_isAABBInsideMaterial_1_, BlockPos p_isAABBInsideMaterial_2_, AxisAlignedBB p_isAABBInsideMaterial_3_, Material p_isAABBInsideMaterial_4_) {
        return super.isAABBInsideMaterial(p_isAABBInsideMaterial_1_, p_isAABBInsideMaterial_2_, p_isAABBInsideMaterial_3_, p_isAABBInsideMaterial_4_);
    }

    @Nullable
    @Override
    public Boolean isAABBInsideLiquid(World p_isAABBInsideLiquid_1_, BlockPos p_isAABBInsideLiquid_2_, AxisAlignedBB p_isAABBInsideLiquid_3_) {
        return super.isAABBInsideLiquid(p_isAABBInsideLiquid_1_, p_isAABBInsideLiquid_2_, p_isAABBInsideLiquid_3_);
    }

    @Override
    public float getBlockLiquidHeight(World p_getBlockLiquidHeight_1_, BlockPos p_getBlockLiquidHeight_2_, IBlockState p_getBlockLiquidHeight_3_, Material p_getBlockLiquidHeight_4_) {
        return super.getBlockLiquidHeight(p_getBlockLiquidHeight_1_, p_getBlockLiquidHeight_2_, p_getBlockLiquidHeight_3_, p_getBlockLiquidHeight_4_);
    }

    @Override
    public boolean canRenderInLayer(IBlockState p_canRenderInLayer_1_, BlockRenderLayer p_canRenderInLayer_2_) {
        return super.canRenderInLayer(p_canRenderInLayer_1_, p_canRenderInLayer_2_);
    }

    @Override
    public SoundType getSoundType(IBlockState p_getSoundType_1_, World p_getSoundType_2_, BlockPos p_getSoundType_3_, @Nullable Entity p_getSoundType_4_) {
        return super.getSoundType(p_getSoundType_1_, p_getSoundType_2_, p_getSoundType_3_, p_getSoundType_4_);
    }

    @Nullable
    @Override
    public float[] getBeaconColorMultiplier(IBlockState state, World world, BlockPos p_getBeaconColorMultiplier_3_, BlockPos p_getBeaconColorMultiplier_4_) {
        return super.getBeaconColorMultiplier(state, world, p_getBeaconColorMultiplier_3_, p_getBeaconColorMultiplier_4_);
    }

    @Override
    public Vec3d getFogColor(World world, BlockPos pos, IBlockState state, Entity entity, Vec3d color, float modifier) {
        return super.getFogColor(world, pos, state, entity, color, modifier);
    }

    @Override
    public IBlockState getStateAtViewpoint(IBlockState state, IBlockAccess blockAccessor, BlockPos pos, Vec3d viewpoint) {
        return super.getStateAtViewpoint(state, blockAccessor, pos, viewpoint);
    }

    @Override final
    public boolean canBeConnectedTo(IBlockAccess blockAccessor, BlockPos pos, EnumFacing side) {
        return canBeConnectedTo(new LocationReadOnly(blockAccessor, pos, null), side);
    }

    public boolean canBeConnectedTo(Location location, EnumFacing side) {
        return false;
    }

    @Nullable
    @Override final
    public PathNodeType getAiPathNodeType(IBlockState state, IBlockAccess blockAccessor, BlockPos pos) {
        return getAiPathNodeType(new LocationReadOnly(blockAccessor, pos, state));
    }

    public PathNodeType getAiPathNodeType(Location location) {
        return super.getAiPathNodeType(location.getBlockState(), location.getBlockAccessor(), location.getPos());
    }

    @Nullable
    @Deprecated
    @Override final
    public PathNodeType getAiPathNodeType(IBlockState state, IBlockAccess blockAccessor, BlockPos pos, @Nullable EntityLiving entity) {
        return getAiPathNodeType(new LocationReadOnly(blockAccessor, pos, state), entity);
    }

    @Nullable
    public PathNodeType getAiPathNodeType(Location location, @Nullable EntityLiving entity) {
        return super.getAiPathNodeType(location.getBlockState(), location.getBlockAccessor(), location.getPos(), entity);
    }

    @Override final
    public boolean doesSideBlockChestOpening(IBlockState state, IBlockAccess blockAccessor, BlockPos pos, EnumFacing side) {
        return doesSideBlockChestOpening(new LocationReadOnly(blockAccessor, pos, state), side);
    }

    protected boolean doesSideBlockChestOpening(Location location, EnumFacing side) {
        return super.doesSideBlockChestOpening(location.getBlockState(), location.getBlockAccessor(), location.getPos(), side);
    }

    protected Item asItem() {
        return Item.getItemFromBlock(this);
    }

    protected IProperty[] getProperties() {
        return new IProperty[0];
    }

    @Override
    protected final BlockStateContainer createBlockState() {
        return new BlockStateContainer(this, getProperties());
    }
}
