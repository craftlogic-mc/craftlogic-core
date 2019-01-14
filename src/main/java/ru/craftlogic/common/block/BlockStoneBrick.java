package ru.craftlogic.common.block;

import net.minecraft.block.Block;
import net.minecraft.block.BlockFalling;
import net.minecraft.block.BlockStoneBrick.EnumType;
import net.minecraft.block.SoundType;
import net.minecraft.block.material.Material;
import net.minecraft.block.state.BlockStateContainer;
import net.minecraft.block.state.IBlockState;
import net.minecraft.creativetab.CreativeTabs;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.init.Blocks;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.EnumHand;
import net.minecraft.util.NonNullList;
import net.minecraft.util.SoundCategory;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.RayTraceResult;
import net.minecraft.world.IBlockAccess;
import net.minecraft.world.World;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;
import ru.craftlogic.CraftConfig;
import ru.craftlogic.api.CraftItems;
import ru.craftlogic.api.CraftSounds;
import ru.craftlogic.api.block.Mossable;
import ru.craftlogic.api.world.Location;

import java.util.Random;

import static net.minecraft.block.BlockStoneBrick.VARIANT;

public class BlockStoneBrick extends BlockFalling implements Mossable {
    public BlockStoneBrick() {
        super(Material.ROCK);
        this.setTickRandomly(true);
        this.setCreativeTab(CreativeTabs.BUILDING_BLOCKS);
        this.setHardness(1.5F);
        this.setResistance(10F);
        this.setSoundType(SoundType.STONE);
        this.setUnlocalizedName("stonebricksmooth");
        this.setDefaultState(this.blockState.getBaseState().withProperty(VARIANT, EnumType.DEFAULT));
    }

    @Override
    @SideOnly(Side.CLIENT)
    public void randomDisplayTick(IBlockState state, World world, BlockPos pos, Random rand) {
        if (state.getValue(VARIANT) == EnumType.CRACKED && CraftConfig.tweaks.enableCrackedStoneBrickGravity) {
            super.randomDisplayTick(state, world, pos, rand);
        }
    }

    @Override
    public void getSubBlocks(CreativeTabs tab, NonNullList<ItemStack> blocks) {
        for (EnumType type : EnumType.values()) {
            blocks.add(new ItemStack(this, 1, type.getMetadata()));
        }
    }

    @Override
    public IBlockState getStateFromMeta(int meta) {
        return this.getDefaultState().withProperty(VARIANT, EnumType.byMetadata(meta));
    }

    @Override
    public int getMetaFromState(IBlockState state) {
        return state.getValue(VARIANT).getMetadata();
    }

    @Override
    protected BlockStateContainer createBlockState() {
        return new BlockStateContainer(this, VARIANT);
    }

    @Override
    public void updateTick(World world, BlockPos pos, IBlockState state, Random rand) {
        if (state.getValue(VARIANT) == EnumType.CRACKED && CraftConfig.tweaks.enableCrackedStoneBrickGravity) {
            super.updateTick(world, pos, state, rand);
        }
    }

    @Override
    public boolean onBlockActivated(World world, BlockPos pos, IBlockState state, EntityPlayer player, EnumHand hand, EnumFacing side, float hitX, float hitY, float hitZ) {
        if (state.getValue(VARIANT) == EnumType.MOSSY && CraftConfig.items.enableMoss) {
            if (!world.isRemote) {
                if (world.rand.nextInt(4) == 0) {
                    player.addItemStackToInventory(new ItemStack(CraftItems.MOSS));
                }
                world.playSound(null, pos, CraftSounds.SQUASH, SoundCategory.PLAYERS, 1F, 0.8F + 0.2F * world.rand.nextFloat());
                world.setBlockState(pos, state.withProperty(VARIANT, EnumType.DEFAULT));
            }
            return true;
        }
        return false;
    }

    @Override
    public void randomTick(World world, BlockPos pos, IBlockState state, Random rand) {
        if (!world.isRemote) {
            int cold = 0;
            int humidity = 0;
            int heat = 0;
            for (int x = -1; x <= 1; x++) {
                for (int y = -1; y <= 1; y++) {
                    for (int z = -1; z <= 1; z++) {
                        if (x != 0 && y != 0 && z != 0) {
                            BlockPos offsetPos = pos.add(x, y, z);
                            IBlockState s = world.getBlockState(offsetPos);
                            if (s.getMaterial() == Material.WATER) {
                                ++humidity;
                                ++cold;
                            } else if (s.getMaterial() == Material.ICE) {
                                if (s.getBlock() == Blocks.FROSTED_ICE || s.getBlock() == Blocks.PACKED_ICE) {
                                    cold += 2;
                                } else {
                                    ++cold;
                                }
                            } else if (s.getMaterial() == Material.LAVA) {
                                heat += 2;
                            } else if (s.getMaterial() == Material.FIRE) {
                                ++heat;
                            }
                        }
                    }
                }
            }
            if (state.getValue(VARIANT) == EnumType.DEFAULT && cold == heat && heat >= 2 && rand.nextInt(3) == 0) {
                if (CraftConfig.tweaks.enableStoneBrickCracking) {
                    world.playEvent(2001, pos, Block.getStateId(state));
                    world.setBlockState(pos, state.withProperty(VARIANT, EnumType.CRACKED));
                }
            } else if (humidity > heat) {
                if (state.getValue(VARIANT) == EnumType.DEFAULT) {
                    if (rand.nextInt(30) == 0) {
                        this.growMoss(new Location(world, pos));
                    }
                } else if (CraftConfig.tweaks.enableMossSpreading) {
                    for (int x = -1; x <= 1; x++) {
                        for (int y = -1; y <= 1; y++) {
                            for (int z = -1; z <= 1; z++) {
                                if (x != 0 && y != 0 && z != 0) {
                                    BlockPos offsetPos = pos.add(x, y, z);
                                    if (rand.nextInt(4) == 0) {
                                        IBlockState s = world.getBlockState(offsetPos);
                                        Block block = s.getBlock();
                                        if (block instanceof Mossable) {
                                            Mossable mossable = (Mossable) block;
                                            Location l = new Location(world, offsetPos);
                                            if (!mossable.isMossy(l)) {
                                                mossable.growMoss(l);
                                            }
                                        }
                                    }
                                }
                            }
                        }
                    }
                }
            } else if (humidity < heat && state.getValue(VARIANT) == EnumType.MOSSY && rand.nextInt(2) == 0
                    && CraftConfig.tweaks.enableMossDecay) {

                world.playEvent(1009, pos, 0);
                world.playEvent(2000, pos, 5);
                world.setBlockState(pos, Blocks.STONEBRICK.getDefaultState());
            }
        }
    }

    @Override
    public Item getItemDropped(IBlockState state, Random rand, int fortune) {
        if (CraftConfig.items.enableStoneBricks) {
            return CraftItems.STONE_BRICK;
        } else {
            return super.getItemDropped(state, rand, fortune);
        }
    }

    @Override
    public int quantityDropped(Random rand) {
        if (CraftConfig.items.enableStoneBricks) {
            return rand.nextInt(5) == 0 ? 3 : 4;
        } else {
            return 1;
        }
    }

    @Override
    public int quantityDropped(IBlockState state, int fortune, Random rand) {
        if (state.getValue(VARIANT) == EnumType.CRACKED && CraftConfig.items.enableStoneBricks) {
            return rand.nextInt(4);
        } else {
            return super.quantityDropped(state, fortune, rand);
        }
    }

    @Override
    public void getDrops(NonNullList<ItemStack> drops, IBlockAccess blockAccessor, BlockPos pos, IBlockState state, int fortune) {
        Random rand = blockAccessor instanceof World ? ((World)blockAccessor).rand : RANDOM;
        super.getDrops(drops, blockAccessor, pos, state, fortune);
        if (state.getValue(VARIANT) == EnumType.MOSSY && rand.nextInt(4) == 0 && CraftConfig.items.enableMoss) {
            drops.add(new ItemStack(CraftItems.MOSS));
        }
    }

    @Override
    public boolean isMossy(Location location) {
        return location.getBlockProperty(VARIANT) == EnumType.MOSSY;
    }

    @Override
    public boolean growMoss(Location location) {
        if (location.getBlockProperty(VARIANT) == EnumType.DEFAULT && CraftConfig.tweaks.enableStoneBrickMossGrowth) {
            boolean loaded = location.isAreaLoaded(32);
            location.setBlockProperty(VARIANT, EnumType.MOSSY, loaded ? 3 : 2);
            return true;
        }
        return false;
    }

    @Override
    public ItemStack getPickBlock(IBlockState state, RayTraceResult target, World world, BlockPos pos, EntityPlayer player) {
        return new ItemStack(Blocks.STONEBRICK, 1, this.getMetaFromState(state));
    }
}
