package ru.craftlogic.mixin.block;

import net.minecraft.block.Block;
import net.minecraft.block.BlockDirt;
import net.minecraft.block.BlockDirt.DirtType;
import net.minecraft.block.BlockGrass;
import net.minecraft.block.material.Material;
import net.minecraft.block.state.BlockStateContainer;
import net.minecraft.block.state.IBlockState;
import net.minecraft.client.renderer.block.statemap.StateMap;
import net.minecraft.init.Blocks;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Overwrite;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import ru.craftlogic.api.block.holders.TileEntityHolder;
import ru.craftlogic.api.model.ModelAutoReg;
import ru.craftlogic.api.model.ModelManager;
import ru.craftlogic.api.plants.PlantSoil;
import ru.craftlogic.api.util.TileEntityInfo;
import ru.craftlogic.api.world.Location;
import ru.craftlogic.api.world.TileEntities;
import ru.craftlogic.common.tileentity.TileEntityGrass;

import javax.annotation.Nullable;
import java.util.Random;

import static net.minecraft.block.BlockGrass.SNOWY;
import static ru.craftlogic.common.block.GrassProperties.HAS_PLANT;

@Mixin(BlockGrass.class)
public class MixinBlockGrass extends Block implements TileEntityHolder<TileEntityGrass>, ModelAutoReg {
    public MixinBlockGrass(Material material) {
        super(material);
    }

    @Inject(method = "<init>", at = @At("RETURN"))
    public void constructor(CallbackInfo info) {
        this.setDefaultState(this.blockState.getBaseState()
            .withProperty(SNOWY, false)
            .withProperty(HAS_PLANT, false)
        );
    }

    @Override
    public void fillWithRain(World world, BlockPos pos) {
        if (!world.isRemote) {
            PlantSoil soil = TileEntities.getTileEntity(world, pos, PlantSoil.class);
            if (soil != null) {
                soil.gainWater(world.isThundering() ? 2 : 1, false);
            }
        }
    }

    /*@Override
    public boolean onBlockActivated(World world, BlockPos pos, IBlockState state, EntityPlayer player, EnumHand hand, EnumFacing side, float dx, float dy, float dz) {
        if (!world.isRemote) {
            world.setBlockState(pos, state.withProperty(HAS_PLANT, true));
            PlantSoil soil = TileEntities.getTileEntity(world, pos, PlantSoil.class);
            if (soil != null && player.getHeldItem(hand).isEmpty()) {
                Plant plant = soil.getPlant();
                if (plant != null) {
                    player.sendMessage(new TextComponentString("Soil water: " + soil.getWater()));
                    player.sendMessage(new TextComponentString("Soil nutrients: " + soil.getNutrients()));
                    player.sendMessage(new TextComponentString("Plant: " + plant));
                } else {
                    soil.setPlant(CraftPlants.RED_MUSHROOM);
                    player.sendMessage(new TextComponentString("Added mushroom seed"));
                }
            }
        }
        return true;
    }*/

    /**
     * @author Radviger
     * @reason In-grass plants
     */
    @Overwrite
    public void updateTick(World world, BlockPos pos, IBlockState state, Random random) {
        if (!world.isRemote) {
            Location location = new Location(world, pos);
            if (location.isAreaLoaded(3)) {
                Location up = location.offset(EnumFacing.UP);

                if (up.getLightFromNeighbors() < 4 && up.getBlockLightOpacity() > 2) {
                    location.setBlock(Blocks.DIRT);
                } else {
                    if (state.getValue(HAS_PLANT)) {
                        TileEntityGrass tile = location.getTileEntity(TileEntityGrass.class);
                        if (tile != null) {
                            tile.randomTick(random);
                        }
                    }
                    if (up.getLightFromNeighbors() >= 9) {
                        for (int i = 0; i < 4; ++i) {
                            Location r = location.randomize(random, 3);
                            Location rup = r.offset(EnumFacing.UP);

                            if (r.isHeightValid() && r.isBlockLoaded()) {
                                if (this.isDirt(r) && rup.getLightFromNeighbors() >= 4 && rup.getBlockLightOpacity() <= 2) {
                                    r.setBlock(Blocks.GRASS);
                                }
                            }
                        }
                    }
                }
            }
        }
    }

    private boolean isDirt(Location location) {
        return location.isSameBlock(Blocks.DIRT) && location.getBlockProperty(BlockDirt.VARIANT) == DirtType.DIRT;
    }

    @Override
    public IBlockState getStateFromMeta(int meta) {
        return this.getDefaultState().withProperty(HAS_PLANT, meta > 0);
    }

    @Override
    public int getMetaFromState(IBlockState state) {
        return state.getValue(HAS_PLANT) ? 1 : 0;
    }

    /**
     * @author Radviger
     * @reason In-grass plants
     */
    @Overwrite
    protected BlockStateContainer createBlockState() {
        return new BlockStateContainer(this, SNOWY, HAS_PLANT);
    }

    @Override
    public boolean hasTileEntity(IBlockState state) {
        return this.getTileEntityInfo(state) != null;
    }

    @Nullable
    @Override
    public TileEntity createTileEntity(World world, IBlockState state) {
        return this.getTileEntityInfo(state).create(world, state);
    }

    @Override
    public TileEntityInfo<TileEntityGrass> getTileEntityInfo(IBlockState state) {
        if (state.getValue(HAS_PLANT))
            return new TileEntityInfo<>(TileEntityGrass.class, state, TileEntityGrass::new);
        return null;
    }

    @Override
    @SideOnly(Side.CLIENT)
    public void registerModel(ModelManager modelManager) {
        modelManager.registerStateMapper(this, new StateMap.Builder().ignore(HAS_PLANT).build());
    }
}
