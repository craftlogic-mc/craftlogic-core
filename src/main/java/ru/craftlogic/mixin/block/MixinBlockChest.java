package ru.craftlogic.mixin.block;

import net.minecraft.block.Block;
import net.minecraft.block.BlockChest;
import net.minecraft.block.BlockContainer;
import net.minecraft.block.material.Material;
import net.minecraft.block.properties.PropertyDirection;
import net.minecraft.block.state.BlockStateContainer;
import net.minecraft.block.state.IBlockState;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.inventory.InventoryLargeChest;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.stats.StatList;
import net.minecraft.tileentity.TileEntityChest;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.EnumHand;
import net.minecraft.util.math.AxisAlignedBB;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.MathHelper;
import net.minecraft.world.IBlockAccess;
import net.minecraft.world.ILockableContainer;
import net.minecraft.world.World;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Overwrite;
import org.spongepowered.asm.mixin.Shadow;
import ru.craftlogic.api.world.TileEntities;
import ru.craftlogic.common.block.ChestPart;

import javax.annotation.Nullable;

import static ru.craftlogic.common.block.ChestProperties.PART;

@Mixin(BlockChest.class)
public abstract class MixinBlockChest extends BlockContainer {
    @Shadow @Final
    public static PropertyDirection FACING;
    @Shadow @Final
    protected static AxisAlignedBB NORTH_CHEST_AABB, SOUTH_CHEST_AABB, WEST_CHEST_AABB, EAST_CHEST_AABB, NOT_CONNECTED_AABB;
    @Shadow @Final
    public BlockChest.Type chestType;

    protected MixinBlockChest() {
        super(Material.WOOD);
    }

    /**
     * @author Radviger
     * @reason Separable chests
     */
    @Overwrite
    public boolean onBlockActivated(World world, BlockPos pos, IBlockState state, EntityPlayer player, EnumHand hand, EnumFacing side, float hitX, float hitY, float hitZ) {
        ItemStack heldItem = player.getHeldItem(hand);
        if (heldItem.getItem() != Item.getItemFromBlock(this) || !side.getAxis().isHorizontal() || side == state.getValue(FACING)) {
            if (!world.isRemote) {
                ILockableContainer container = this.getLockableContainer(world, pos);
                if (container != null) {
                    player.displayGUIChest(container);
                    switch (this.chestType) {
                        case BASIC:
                            player.addStat(StatList.CHEST_OPENED);
                            break;
                        case TRAP:
                            player.addStat(StatList.TRAPPED_CHEST_TRIGGERED);
                            break;
                    }
                }
            }
            return true;
        }
        return false;
    }

    @Shadow
    public abstract ILockableContainer getLockableContainer(World world, BlockPos pos);

    /**
     * @author Radviger
     * @reason Separable chests
     */
    @Overwrite
    public AxisAlignedBB getBoundingBox(IBlockState state, IBlockAccess blockAccessor, BlockPos pos) {
        ChestPart part = state.getValue(PART);
        if (part != ChestPart.SINGLE) {
            switch (part.rotate(state.getValue(FACING))) {
                case NORTH:
                    return NORTH_CHEST_AABB;
                case SOUTH:
                    return SOUTH_CHEST_AABB;
                case WEST:
                    return WEST_CHEST_AABB;
                case EAST:
                    return EAST_CHEST_AABB;
            }
        }
        return NOT_CONNECTED_AABB;
    }

    /**
     * @author Radviger
     * @reason Separable chests
     */
    @Overwrite
    private boolean isDoubleChest(World world, BlockPos pos) {
        return world.getBlockState(pos).getValue(PART) != ChestPart.SINGLE;
    }

    /**
     * @author Radviger
     * @reason Separable chests
     */
    @Overwrite
    public void onBlockAdded(World world, BlockPos pos, IBlockState state) {
        if (state.getValue(PART) != ChestPart.SINGLE) {
            this.checkForSurroundingChests(world, pos, state);

            for (EnumFacing side : EnumFacing.Plane.HORIZONTAL) {
                BlockPos offsetPos = pos.offset(side);
                IBlockState offsetState = world.getBlockState(offsetPos);
                if (offsetState.getBlock() == this) {
                    this.checkForSurroundingChests(world, offsetPos, offsetState);
                }
            }
        }
    }

    /**
     * @author Radviger
     * @reason Separable chests
     */
    @Overwrite
    public void onBlockPlacedBy(World world, BlockPos pos, IBlockState state, EntityLivingBase placer, ItemStack item) {
        EnumFacing facing = EnumFacing.getHorizontal(MathHelper.floor((double)(placer.rotationYaw * 4F / 360F) + 0.5D) & 3).getOpposite();
        state = state.withProperty(FACING, facing);
        if (!placer.isSneaking()) {
            BlockPos northPos = pos.north();
            IBlockState northState = world.getBlockState(northPos);
            BlockPos southPos = pos.south();
            IBlockState southState = world.getBlockState(southPos);
            BlockPos westPos = pos.west();
            IBlockState westState = world.getBlockState(westPos);
            BlockPos eastPos = pos.east();
            IBlockState eastState = world.getBlockState(eastPos);
            boolean chestAtNorth = this == northState.getBlock() && northState.getValue(PART) == ChestPart.SINGLE;
            boolean chestAtSouth = this == southState.getBlock() && southState.getValue(PART) == ChestPart.SINGLE;
            boolean chestAtWest = this == westState.getBlock() && westState.getValue(PART) == ChestPart.SINGLE;
            boolean chestAtEast = this == eastState.getBlock() && eastState.getValue(PART) == ChestPart.SINGLE;
            if (!chestAtNorth && !chestAtSouth && !chestAtWest && !chestAtEast) {
                world.setBlockState(pos, state, 3);
            } else if (facing.getAxis() == EnumFacing.Axis.X && (chestAtNorth || chestAtSouth)) {
                ChestPart part = facing == EnumFacing.EAST ? ChestPart.RIGHT : ChestPart.LEFT;
                if (chestAtNorth) {
                    part = part.opposite();
                }
                world.setBlockState(chestAtNorth ? northPos : southPos, state.withProperty(PART, part.opposite()), 3);
                world.setBlockState(pos, state.withProperty(PART, part), 3);
            } else if (facing.getAxis() == EnumFacing.Axis.Z && (chestAtWest || chestAtEast)) {
                ChestPart part = facing == EnumFacing.SOUTH ? ChestPart.LEFT : ChestPart.RIGHT;
                if (chestAtWest) {
                    part = part.opposite();
                }
                world.setBlockState(chestAtWest ? westPos : eastPos, state.withProperty(PART, part.opposite()), 3);
                world.setBlockState(pos, state.withProperty(PART, part), 3);
            }
        } else {
            world.setBlockState(pos, state, 3);
        }

        if (item.hasDisplayName()) {
            TileEntityChest chest = TileEntities.getTileEntity(world, pos, TileEntityChest.class);
            if (chest != null) {
                chest.setCustomName(item.getDisplayName());
            }
        }
    }

    /**
     * @author Radviger
     * @reason Separable chests
     */
    @Overwrite
    public IBlockState checkForSurroundingChests(World world, BlockPos pos, IBlockState state) {
        if (!world.isRemote) {
            IBlockState northState = world.getBlockState(pos.north());
            IBlockState southState = world.getBlockState(pos.south());
            IBlockState westState = world.getBlockState(pos.west());
            IBlockState eastState = world.getBlockState(pos.east());
            EnumFacing facing = state.getValue(FACING);
            if (northState.getBlock() != this && southState.getBlock() != this) {
                boolean northBlockFull = northState.isFullBlock();
                boolean southBlockFull = southState.isFullBlock();
                if (westState.getBlock() == this || eastState.getBlock() == this) {
                    BlockPos offsetPos = westState.getBlock() == this ? pos.west() : pos.east();
                    IBlockState iblockstate7 = world.getBlockState(offsetPos.north());
                    IBlockState iblockstate6 = world.getBlockState(offsetPos.south());
                    facing = EnumFacing.SOUTH;
                    EnumFacing enumfacing2 = westState.getBlock() == this ? westState.getValue(FACING) : eastState.getValue(FACING);

                    if (enumfacing2 == EnumFacing.NORTH) {
                        facing = EnumFacing.NORTH;
                    }

                    if ((northBlockFull || iblockstate7.isFullBlock()) && !southBlockFull && !iblockstate6.isFullBlock()) {
                        facing = EnumFacing.SOUTH;
                    }

                    if ((southBlockFull || iblockstate6.isFullBlock()) && !northBlockFull && !iblockstate7.isFullBlock()) {
                        facing = EnumFacing.NORTH;
                    }
                }
            } else {
                BlockPos blockpos = northState.getBlock() == this ? pos.north() : pos.south();
                IBlockState iblockstate4 = world.getBlockState(blockpos.west());
                IBlockState iblockstate5 = world.getBlockState(blockpos.east());
                facing = EnumFacing.EAST;
                EnumFacing enumfacing1 = northState.getBlock() == this ? northState.getValue(FACING) : southState.getValue(FACING);

                if (enumfacing1 == EnumFacing.WEST) {
                    facing = EnumFacing.WEST;
                }

                if ((westState.isFullBlock() || iblockstate4.isFullBlock()) && !eastState.isFullBlock() && !iblockstate5.isFullBlock()) {
                    facing = EnumFacing.EAST;
                }

                if ((eastState.isFullBlock() || iblockstate5.isFullBlock()) && !westState.isFullBlock() && !iblockstate4.isFullBlock()) {
                    facing = EnumFacing.WEST;
                }
            }

            state = state.withProperty(FACING, facing);
            world.setBlockState(pos, state, 3);
        }
        return state;
    }

    /**
     * @author Radviger
     * @reason Separable chests
     */
    @Overwrite
    public IBlockState correctFacing(World world, BlockPos pos, IBlockState state) {
        EnumFacing enumfacing = null;

        for (EnumFacing side : EnumFacing.Plane.HORIZONTAL) {
            IBlockState offsetState = world.getBlockState(pos.offset(side));
            if (offsetState.getBlock() == this) {
                return state;
            }

            if (offsetState.isFullBlock()) {
                if (enumfacing != null) {
                    enumfacing = null;
                    break;
                }

                enumfacing = side;
            }
        }

        if (enumfacing != null) {
            return state.withProperty(FACING, enumfacing.getOpposite());
        } else {
            EnumFacing enumfacing2 = state.getValue(FACING);
            if (world.getBlockState(pos.offset(enumfacing2)).isFullBlock()) {
                enumfacing2 = enumfacing2.getOpposite();
            }

            if (world.getBlockState(pos.offset(enumfacing2)).isFullBlock()) {
                enumfacing2 = enumfacing2.rotateY();
            }

            if (world.getBlockState(pos.offset(enumfacing2)).isFullBlock()) {
                enumfacing2 = enumfacing2.getOpposite();
            }

            return state.withProperty(FACING, enumfacing2);
        }
    }

    /**
     * @author Radviger
     * @reason Separable chests
     */
    @Overwrite
    public boolean canPlaceBlockAt(World world, BlockPos pos) {
        return true;
    }

    /**
     * @author Radviger
     * @reason Separable chests
     */
    @Overwrite
    public void neighborChanged(IBlockState state, World world, BlockPos pos, Block neighborBlock, BlockPos neighborPos) {
        super.neighborChanged(state, world, pos, neighborBlock, neighborPos);
        TileEntityChest chest = TileEntities.getTileEntity(world, pos, TileEntityChest.class);
        if (chest != null) {
            chest.updateContainingBlockInfo();
        }
        ChestPart part = state.getValue(PART);
        if (part != ChestPart.SINGLE) {
            BlockPos offsetPos = pos.offset(part.rotate(state.getValue(FACING)));
            IBlockState offsetState = world.getBlockState(offsetPos);
            if (offsetState.getBlock() != this || offsetState.getValue(PART).opposite() != part) {
                world.setBlockState(pos, state.withProperty(PART, ChestPart.SINGLE));
            }
        }
    }

    /**
     * @author Radviger
     * @reason Separable chests
     */
    @Overwrite
    @Nullable
    public ILockableContainer getContainer(World world, BlockPos pos, boolean ignoreIfBlocked) {
        ILockableContainer chest = TileEntities.getTileEntity(world, pos, ILockableContainer.class);
        if (chest != null) {
            if (ignoreIfBlocked || !this.isBlocked(world, pos)) {
                IBlockState state = world.getBlockState(pos);
                ChestPart part = state.getValue(PART);
                if (part == ChestPart.SINGLE) {
                    return chest;
                } else {
                    BlockPos offsetPos = pos.offset(part.rotate(state.getValue(FACING)));
                    IBlockState offsetState = world.getBlockState(offsetPos);
                    ILockableContainer offsetChest = TileEntities.getTileEntity(world, offsetPos, ILockableContainer.class);
                    if (offsetChest != null) {
                        if (offsetState.getValue(PART) == part.opposite()) {
                            switch (part) {
                                case LEFT:
                                    return new InventoryLargeChest("container.chestDouble", chest, offsetChest);
                                case RIGHT:
                                    return new InventoryLargeChest("container.chestDouble", offsetChest, chest);
                            }
                        }
                    }
                }
            }
        }
        return null;
    }

    /**
     * @author Radviger
     * @reason Separable chests
     */
    @Overwrite
    public IBlockState getStateFromMeta(int meta) {
        ChestPart part = meta >= 8 ? ChestPart.RIGHT : (meta >= 4 ? ChestPart.LEFT : ChestPart.SINGLE);
        EnumFacing side = EnumFacing.getHorizontal(meta % 4);
        return this.getDefaultState().withProperty(FACING, side).withProperty(PART, part);
    }

    /**
     * @author Radviger
     * @reason Separable chests
     */
    @Overwrite
    public int getMetaFromState(IBlockState state) {
        return state.getValue(FACING).getHorizontalIndex() + state.getValue(PART).ordinal() * 4;
    }

    /**
     * @author Radviger
     * @reason Separable chests
     */
    @Overwrite
    protected BlockStateContainer createBlockState() {
        return new BlockStateContainer(this, FACING, PART);
    }

    @Shadow
    protected abstract boolean isBlocked(World world, BlockPos pos);

}
