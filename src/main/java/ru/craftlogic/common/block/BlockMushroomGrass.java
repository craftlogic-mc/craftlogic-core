package ru.craftlogic.common.block;

import net.minecraft.block.BlockDirt;
import net.minecraft.block.SoundType;
import net.minecraft.block.material.Material;
import net.minecraft.block.properties.PropertyBool;
import net.minecraft.block.properties.PropertyEnum;
import net.minecraft.block.state.BlockStateContainer;
import net.minecraft.block.state.IBlockState;
import net.minecraft.client.renderer.block.model.ModelResourceLocation;
import net.minecraft.creativetab.CreativeTabs;
import net.minecraft.entity.Entity;
import net.minecraft.init.Blocks;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.ColorizerGrass;
import net.minecraft.world.World;
import net.minecraft.world.biome.BiomeColorHelper;
import net.minecraftforge.fluids.Fluid;
import net.minecraftforge.fluids.FluidRegistry;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;
import ru.craftlogic.api.block.BlockBase;
import ru.craftlogic.api.block.Colored;
import ru.craftlogic.api.model.ModelManager;
import ru.craftlogic.api.model.ModelRegistrar;
import ru.craftlogic.api.util.Nameable;
import ru.craftlogic.api.world.Location;

import javax.annotation.Nullable;
import java.util.Random;

public class BlockMushroomGrass extends BlockBase implements ModelRegistrar, Colored {
    private static final PropertyBool SNOWY = PropertyBool.create("snowy");
    private static final PropertyBool COVERED = PropertyBool.create("covered");
    private static final PropertyEnum<MushroomType> TYPE = PropertyEnum.create("type", MushroomType.class);

    public BlockMushroomGrass() {
        super(Material.GRASS, "mushroom_grass", 3.5F, CreativeTabs.BUILDING_BLOCKS);
        this.setSoundType(SoundType.PLANT);
        this.setDefaultState(this.getBlockState().getBaseState().withProperty(SNOWY, false));
        this.setTickRandomly(true);
    }

    @Override
    public SoundType getSoundType(IBlockState state, World world, BlockPos pos, @Nullable Entity entity) {
        return super.getSoundType(state, world, pos, entity);
    }

    @Override
    protected IBlockState getActualState(Location location) {
        Location upper = location.offset(EnumFacing.UP);
        return location.getBlockState().withProperty(SNOWY, upper.isSameBlock(Blocks.SNOW) || upper.isSameBlock(Blocks.SNOW_LAYER));
    }

    @Override
    protected void updateTick(Location location, Random rand) {
        if (!location.isWorldRemote() && location.isAreaLoaded(3)) {
            Location upper = location.offset(EnumFacing.UP);

            if (location.getBlockProperty(COVERED) && upper.getLightFromNeighbors() < 4 && upper.getBlockLightOpacity() > 2) {
                location.setBlockProperty(COVERED, false);
            }
        }
    }

    @Override
    public void fillWithRain(Location location, Fluid fluid) {
        if (!location.isWorldRemote() && location.isAreaLoaded(3)) {
            if (fluid == FluidRegistry.WATER) {
                Random rand = new Random();
                MushroomType type = location.getBlockProperty(TYPE);

                for(int i = 0; i < 4; ++i) {
                    Location l = location.randomize(rand, 3.0, 5.0, 3.0);
                    if (l.getY() >= 0 && l.getY() < 256 && !l.isBlockLoaded()) {
                        return;
                    }

                    Location u = l.offset(EnumFacing.UP);

                    if ((l.getBlock() == Blocks.DIRT && l.getBlockProperty(BlockDirt.VARIANT) == BlockDirt.DirtType.DIRT
                            || l.getBlock() == Blocks.GRASS) && u.getLightFromNeighbors() >= 4 && u.getBlockLightOpacity() <= 2 && u.isAir()) {
                        u.setBlockState(type.getState());
                    }
                }
            }
        }
    }

    @Override
    public IBlockState getStateFromMeta(int meta) {
        return this.getDefaultState()
                .withProperty(TYPE, MushroomType.values()[meta & 7])
                .withProperty(COVERED, (meta & 8) > 0);
    }

    @Override
    public int getMetaFromState(IBlockState state) {
        return state.getValue(TYPE).ordinal() + (state.getValue(COVERED) ? 8 : 0);
    }

    @Override
    public int getBlockColor(@Nullable Location location, IBlockState state, int tint) {
        return location != null ? BiomeColorHelper.getGrassColorAtPos(location.getWorld(), location.getPos())
                : ColorizerGrass.getGrassColor(0.5, 1.0);
    }

    @Override
    public BlockStateContainer getBlockState() {
        return new BlockStateContainer(this, SNOWY, COVERED, TYPE);
    }

    @Override
    @SideOnly(Side.CLIENT)
    public void registerModel(ModelManager modelManager) {
        super.registerModel(modelManager);
        modelManager.registerStateMapper(this, (state, mapper) -> {
            String props = "snowy=" + state.getValue(SNOWY);
            return state.getValue(COVERED) ? new ModelResourceLocation("grass", props) : new ModelResourceLocation("dirt");
        });
    }

    public enum MushroomType implements Nameable {
        BROWN, RED;

        public IBlockState getState() {
            switch (this) {
                default:
                case BROWN:
                    return Blocks.BROWN_MUSHROOM.getDefaultState();
                case RED:
                    return Blocks.RED_MUSHROOM.getDefaultState();
            }
        }
    }
}
