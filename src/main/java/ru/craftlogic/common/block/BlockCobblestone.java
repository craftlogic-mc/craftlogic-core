package ru.craftlogic.common.block;

import net.minecraft.block.Block;
import net.minecraft.block.BlockFalling;
import net.minecraft.block.SoundType;
import net.minecraft.block.material.Material;
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
import ru.craftlogic.CraftConfig;
import ru.craftlogic.api.CraftItems;
import ru.craftlogic.api.block.Mossable;
import ru.craftlogic.api.world.Location;

import java.util.Random;

public class BlockCobblestone extends BlockFalling implements Mossable {
    private final boolean mossy;

    public BlockCobblestone(boolean mossy) {
        super(Material.ROCK);
        this.mossy = mossy;
        this.setHardness(mossy ? 5F : 4F);
        this.setResistance(10F);
        this.setSoundType(SoundType.STONE);
        this.setTranslationKey(mossy ? "stoneMoss" : "stonebrick");
        this.setCreativeTab(CreativeTabs.BUILDING_BLOCKS);
        this.setTickRandomly(true);
    }

    @Override
    public void updateTick(World world, BlockPos pos, IBlockState state, Random rand) {
        if (CraftConfig.tweaks.enableCobblestoneGravity && world.isAreaLoaded(pos, 32)) {
            super.updateTick(world, pos, state, rand);
        }
    }

    @Override
    public void randomDisplayTick(IBlockState state, World world, BlockPos pos, Random rand) {
        if (CraftConfig.tweaks.enableCobblestoneGravity) {
            super.randomDisplayTick(state, world, pos, rand);
        }
    }

    @Override
    public void randomTick(World world, BlockPos pos, IBlockState state, Random rand) {
        if (!world.isRemote) {
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
                            } else if (s.getMaterial() == Material.LAVA) {
                                heat += 2;
                            } else if (s.getMaterial() == Material.FIRE) {
                                ++heat;
                            }
                        }
                    }
                }
            }
            boolean loaded = world.isAreaLoaded(pos, 32);
            if (humidity > heat) {
                if (!this.mossy) {
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
            } else if (this.mossy && rand.nextInt(3) == 0 && CraftConfig.tweaks.enableMossDecay) {
                world.setBlockState(pos, Blocks.COBBLESTONE.getDefaultState(), loaded ? 3 : 2);
                world.playEvent(1009, pos, 0);
                world.playEvent(2000, pos, 0);
            }
        }
    }



    @SideOnly(Side.CLIENT)
    @Override
    public int getDustColor(IBlockState state) {
        return 0xFF5D5A59;
    }

    @Override
    public Item getItemDropped(IBlockState state, Random rand, int fortune) {
        if (CraftConfig.items.enableRocks) {
            return CraftItems.ROCK;
        } else {
            return super.getItemDropped(state, rand, fortune);
        }
    }

    @Override
    public int quantityDropped(Random random) {
        if (CraftConfig.items.enableRocks) {
            return 1 + random.nextInt(4);
        } else {
            return 1;
        }
    }

    @Override
    public void getDrops(NonNullList<ItemStack> drops, IBlockAccess blockAccessor, BlockPos pos, IBlockState state, int fortune) {
        Random rand = blockAccessor instanceof World ? ((World)blockAccessor).rand : RANDOM;
        super.getDrops(drops, blockAccessor, pos, state, fortune);
        if (this.mossy && rand.nextInt(4) == 0 && CraftConfig.items.enableMoss) {
            drops.add(new ItemStack(CraftItems.MOSS));
        }
    }

    @Override
    public boolean isMossy(Location location) {
        return this.mossy;
    }

    @Override
    public boolean growMoss(Location location) {
        if (!this.mossy && CraftConfig.tweaks.enableCobblestoneMossGrowth) {
            boolean loaded = location.isAreaLoaded(32);
            location.setBlock(Blocks.MOSSY_COBBLESTONE, loaded ? 3 : 2);
            return true;
        }
        return false;
    }
}
