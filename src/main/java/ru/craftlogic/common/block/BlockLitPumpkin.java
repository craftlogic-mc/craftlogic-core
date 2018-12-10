package ru.craftlogic.common.block;

import net.minecraft.block.BlockPumpkin;
import net.minecraft.block.SoundType;
import net.minecraft.block.properties.IProperty;
import net.minecraft.block.properties.PropertyBool;
import net.minecraft.block.state.BlockStateContainer;
import net.minecraft.block.state.IBlockState;
import net.minecraft.client.renderer.block.model.ModelResourceLocation;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.init.Items;
import net.minecraft.init.SoundEvents;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.EnumHand;
import net.minecraft.util.EnumParticleTypes;
import net.minecraft.util.SoundCategory;
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

public class BlockLitPumpkin extends BlockPumpkin implements ModelAutoReg {
    public static final PropertyBool LIT = BlockBurningTorch.LIT;

    public BlockLitPumpkin() {
        this.setHardness(1F);
        this.setSoundType(SoundType.WOOD);
        this.setUnlocalizedName("litpumpkin");
        this.setDefaultState(
            this.blockState.getBaseState()
                .withProperty(LIT, false)
                .withProperty(FACING, EnumFacing.NORTH)
        );
    }

    @SideOnly(Side.CLIENT)
    public void randomDisplayTick(IBlockState state, World world, BlockPos pos, Random rand) {
        if (state.getValue(LIT)) {
            EnumFacing facing = state.getValue(FACING);
            double dx = (double)pos.getX() + 0.5D;
            double dy = (double)pos.getY() + rand.nextDouble() * 6.0D / 16.0D;
            double dz = (double)pos.getZ() + 0.5D;
            double ox = 0.52D;
            double oz = rand.nextDouble() * 0.6D - 0.3D;
            switch(facing) {
                case NORTH:
                    world.spawnParticle(EnumParticleTypes.SMOKE_NORMAL, dx + oz, dy, dz - ox, 0, 0, 0);
                    world.spawnParticle(EnumParticleTypes.FLAME, dx + oz, dy, dz - ox, 0, 0, 0);
                    break;
                case SOUTH:
                    world.spawnParticle(EnumParticleTypes.SMOKE_NORMAL, dx + oz, dy, dz + ox, 0, 0, 0);
                    world.spawnParticle(EnumParticleTypes.FLAME, dx + oz, dy, dz + ox, 0, 0, 0);
                    break;
                case WEST:
                    world.spawnParticle(EnumParticleTypes.SMOKE_NORMAL, dx - ox, dy, dz + oz, 0, 0, 0);
                    world.spawnParticle(EnumParticleTypes.FLAME, dx - ox, dy, dz + oz, 0, 0, 0);
                    break;
                case EAST:
                    world.spawnParticle(EnumParticleTypes.SMOKE_NORMAL, dx + ox, dy, dz + oz, 0, 0, 0);
                    world.spawnParticle(EnumParticleTypes.FLAME, dx + ox, dy, dz + oz, 0, 0, 0);
            }

        }
    }

    @Override
    public boolean onBlockActivated(World world, BlockPos pos, IBlockState state, EntityPlayer player, EnumHand hand, EnumFacing side, float tx, float ty, float tz) {
        if (!world.isRemote && side == state.getValue(FACING)) {
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
    public int getLightValue(IBlockState state, IBlockAccess blockAccessor, BlockPos pos) {
        return state.getValue(LIT) ? 15 : 0;
    }

    @Override
    public IBlockState getStateFromMeta(int meta) {
        return this.getDefaultState()
                .withProperty(FACING, EnumFacing.getHorizontal(meta & 7))
                .withProperty(LIT, (meta & 8) > 0);
    }

    @Override
    public int getMetaFromState(IBlockState state) {
        int meta = state.getValue(FACING).getHorizontalIndex();
        if (state.getValue(LIT)) meta |= 8;
        return meta;
    }

    @Override
    protected BlockStateContainer createBlockState() {
        return new BlockStateContainer(this, LIT, FACING);
    }

    @Override
    @SideOnly(Side.CLIENT)
    public void registerModel(ModelManager modelManager) {
        modelManager.registerStateMapper(this, (state, mapper) -> {
            Map<IProperty<?>, Comparable<?>> properties = new HashMap<>(state.getProperties());
            boolean lit = (Boolean) properties.remove(LIT);
            String props = mapper.getPropertyString(properties);
            return new ModelResourceLocation((lit ? "lit_pumpkin" : "unlit_pumpkin"), props);
        });
        Item item = Item.getItemFromBlock(this);
        modelManager.registerItemVariants(item, "minecraft:unlit_pumpkin");
        modelManager.registerItemModel(item, 0, "unlit_pumpkin");
    }
}
