package ru.craftlogic.common.block;

import net.minecraft.block.BlockTorch;
import net.minecraft.block.SoundType;
import net.minecraft.block.properties.IProperty;
import net.minecraft.block.properties.PropertyBool;
import net.minecraft.block.state.BlockStateContainer;
import net.minecraft.block.state.IBlockState;
import net.minecraft.client.renderer.block.model.ModelResourceLocation;
import net.minecraft.creativetab.CreativeTabs;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.init.Items;
import net.minecraft.init.SoundEvents;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.util.*;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.IBlockAccess;
import net.minecraft.world.World;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;
import ru.craftlogic.api.model.ModelAutoReg;
import ru.craftlogic.api.model.ModelManager;

import java.util.HashMap;
import java.util.Map;
import java.util.Random;

public class BlockBurningTorch extends BlockTorch implements ModelAutoReg {
    public static final PropertyBool LIT = PropertyBool.create("lit");

    public BlockBurningTorch() {
        this.setHardness(0.0F);
        this.setLightLevel(0.9375F);
        this.setSoundType(SoundType.WOOD);
        this.setUnlocalizedName("torch");
        this.setTickRandomly(true);
        this.setDefaultState(this.blockState.getBaseState()
            .withProperty(FACING, EnumFacing.UP)
            .withProperty(LIT, false)
        );
    }

    @Override
    public boolean onBlockActivated(World world, BlockPos pos, IBlockState state, EntityPlayer player, EnumHand hand, EnumFacing side, float tx, float ty, float tz) {
        if (!world.isRemote) {
            ItemStack heldItem = player.getHeldItem(hand);
            boolean lit = state.getValue(LIT);
            if (heldItem.isEmpty() && lit) {
                world.setBlockState(pos, state.withProperty(LIT, false));
                world.playSound(null, pos, SoundEvents.BLOCK_FIRE_EXTINGUISH, SoundCategory.PLAYERS, 1F, world.rand.nextFloat() * 0.3F + 0.7F);
            } else if (heldItem.getItem() == Items.FLINT_AND_STEEL && !lit) {
                heldItem.damageItem(1, player);
                world.playSound(null, pos, SoundEvents.ITEM_FLINTANDSTEEL_USE, SoundCategory.PLAYERS, 1F, world.rand.nextFloat() * 0.3F + 0.7F);
                if (world.rand.nextInt(3) == 0) {
                    world.setBlockState(pos, state.withProperty(LIT, true));
                }
            }
        }
        return true;
    }

    @Override
    public IBlockState getStateForPlacement(World world, BlockPos pos, EnumFacing side, float tx, float ty, float tz, int meta, EntityLivingBase placer) {
        return super.getStateForPlacement(world, pos, side, tx, ty, tz, meta, placer).withProperty(LIT, meta == 1);
    }

    @Override
    public int damageDropped(IBlockState state) {
        return state.getValue(LIT) ? 1 : 0;
    }

    @Override
    public IBlockState getStateFromMeta(int meta) {
        IBlockState state = this.getDefaultState().withProperty(LIT, (meta & 8) > 0);
        switch(meta & 7) {
            case 1:
                state = state.withProperty(FACING, EnumFacing.EAST);
                break;
            case 2:
                state = state.withProperty(FACING, EnumFacing.WEST);
                break;
            case 3:
                state = state.withProperty(FACING, EnumFacing.SOUTH);
                break;
            case 4:
                state = state.withProperty(FACING, EnumFacing.NORTH);
                break;
            case 5:
            default:
                state = state.withProperty(FACING, EnumFacing.UP);
        }

        return state;
    }

    @Override
    public int getMetaFromState(IBlockState state) {
        int meta = 0;
        switch(state.getValue(FACING)) {
            case EAST:
                meta = 1;
                break;
            case WEST:
                meta = 2;
                break;
            case SOUTH:
                meta = 3;
                break;
            case NORTH:
                meta = 4;
                break;
            case DOWN:
            case UP:
            default:
                meta = 5;
        }
        if (state.getValue(LIT)) {
            meta |= 8;
        }

        return meta;
    }

    @Override
    public int getLightValue(IBlockState state, IBlockAccess blockAccessor, BlockPos pos) {
        return state.getValue(LIT) ? super.getLightValue(state, blockAccessor, pos) : 0;
    }

    @Override
    public void updateTick(World world, BlockPos pos, IBlockState state, Random rand) {
        if (state.getValue(LIT) && (rand.nextInt(20) == 0 || world.isRainingAt(pos.up()) )) {
            state.cycleProperty(LIT);
        }
    }

    @Override
    @SideOnly(Side.CLIENT)
    public void randomDisplayTick(IBlockState state, World world, BlockPos pos, Random rand) {
        if (state.getValue(LIT)) {
            super.randomDisplayTick(state, world, pos, rand);
        }
    }

    @Override
    protected BlockStateContainer createBlockState() {
        return new BlockStateContainer(this, FACING, LIT);
    }

    @Override
    @SideOnly(Side.CLIENT)
    public void registerModel(ModelManager modelManager) {
        modelManager.registerStateMapper(this, (state, mapper) -> {
            Map<IProperty<?>, Comparable<?>> properties = new HashMap<>(state.getProperties());
            boolean lit = (Boolean) properties.remove(LIT);
            String props = mapper.getPropertyString(properties);
            return new ModelResourceLocation((lit ? "torch" : "unlit_torch"), props);
        });
        Item item = Item.getItemFromBlock(this);
        modelManager.registerItemVariants(item, "minecraft:torch", "minecraft:unlit_torch");
        modelManager.registerItemModel(item, 0, "unlit_torch");
        modelManager.registerItemModel(item, 1, "torch");
    }

    @Override
    public void getSubBlocks(CreativeTabs tab, NonNullList<ItemStack> blocks) {
        blocks.add(new ItemStack(this, 1, 0));
        blocks.add(new ItemStack(this, 1, 1));
    }
}
