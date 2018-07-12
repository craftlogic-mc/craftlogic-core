package ru.craftlogic.common.block;

import net.minecraft.block.Block;
import net.minecraft.block.BlockFalling;
import net.minecraft.block.BlockStoneBrick.EnumType;
import net.minecraft.block.SoundType;
import net.minecraft.block.material.Material;
import net.minecraft.block.state.BlockStateContainer;
import net.minecraft.block.state.IBlockState;
import net.minecraft.creativetab.CreativeTabs;
import net.minecraft.init.Blocks;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.util.NonNullList;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.IBlockAccess;
import net.minecraft.world.World;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;
import ru.craftlogic.common.CraftItems;

import java.util.Random;

import static net.minecraft.block.BlockStoneBrick.VARIANT;

public class BlockStoneBrick extends BlockFalling {
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
        if (state.getValue(VARIANT) == EnumType.CRACKED) {
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
        if (state.getValue(VARIANT) == EnumType.CRACKED) {
            super.updateTick(world, pos, state, rand);
        }
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
                world.playEvent(2001, pos, Block.getStateId(state));
                world.setBlockState(pos, state.withProperty(VARIANT, EnumType.CRACKED));
            } else if (humidity > heat) {
                if (state.getValue(VARIANT) == EnumType.DEFAULT) {
                    if (rand.nextInt(30) == 0) {
                        world.setBlockState(pos, state.withProperty(VARIANT, EnumType.MOSSY));
                    }
                } else {
                    for (int x = -1; x <= 1; x++) {
                        for (int y = -1; y <= 1; y++) {
                            for (int z = -1; z <= 1; z++) {
                                if (x != 0 && y != 0 && z != 0) {
                                    BlockPos offsetPos = pos.add(x, y, z);
                                    IBlockState s = world.getBlockState(offsetPos);
                                    if (rand.nextInt(4) == 0) {
                                        if (s.getBlock() == Blocks.COBBLESTONE) {
                                            world.setBlockState(offsetPos, Blocks.MOSSY_COBBLESTONE.getDefaultState());
                                        } else if (s.getBlock() == Blocks.STONEBRICK && s.getValue(VARIANT) == EnumType.DEFAULT) {
                                            world.setBlockState(offsetPos, s.withProperty(VARIANT, EnumType.MOSSY));
                                        }
                                    }
                                }
                            }
                        }
                    }
                }
            } else if (humidity < heat) {
                if (state.getValue(VARIANT) == EnumType.MOSSY && rand.nextInt(2) == 0) {
                    world.playEvent(1009, pos, 0);
                    world.playEvent(2000, pos, 5);
                    world.setBlockState(pos, Blocks.STONEBRICK.getDefaultState());
                }
            }
        }
    }

    @Override
    public Item getItemDropped(IBlockState state, Random rand, int fortune) {
        return CraftItems.STONE_BRICK;
    }

    @Override
    public int quantityDropped(Random rand) {
        return rand.nextInt(5) == 0 ? 3 : 4;
    }

    @Override
    public int quantityDropped(IBlockState state, int fortune, Random rand) {
        if (state.getValue(VARIANT) == EnumType.CRACKED) {
            return rand.nextInt(4);
        } else {
            return super.quantityDropped(state, fortune, rand);
        }
    }

    @Override
    public void getDrops(NonNullList<ItemStack> drops, IBlockAccess blockAccessor, BlockPos pos, IBlockState state, int fortune) {
        Random rand = blockAccessor instanceof World ? ((World)blockAccessor).rand : RANDOM;
        super.getDrops(drops, blockAccessor, pos, state, fortune);
        if (state.getValue(VARIANT) == EnumType.MOSSY && rand.nextInt(4) == 0) {
            drops.add(new ItemStack(CraftItems.MOSS));
        }
    }
}
