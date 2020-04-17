package ru.craftlogic.api.block.holders;

import net.minecraft.block.Block;
import net.minecraft.block.ITileEntityProvider;
import net.minecraft.block.state.IBlockState;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.ResourceLocation;
import net.minecraft.world.World;
import ru.craftlogic.api.CraftTileEntities;
import ru.craftlogic.api.block.BlockBase;
import ru.craftlogic.api.util.TileEntityInfo;

import javax.annotation.Nullable;
import java.util.ArrayList;
import java.util.List;

public interface TileEntityHolder<T extends TileEntity> extends ITileEntityProvider {
    @Nullable
    @Override
    default T createNewTileEntity(World world, int meta) {
        if (this instanceof Block) {
            IBlockState state = ((Block)this).getStateFromMeta(meta);
            if (this instanceof BlockBase) {
                return (T) ((BlockBase)this).createTileEntity(world, state);
            } else {
                TileEntityInfo<? extends T> type = this.getTileEntityInfo(state);
                return type == null ? null : type.create(world, state);
            }
        }
        return null;
    }

    default ResourceLocation getTileEntityName(IBlockState state) {
        return ((Block)this).getRegistryName();
    }

    TileEntityInfo<? extends T> getTileEntityInfo(IBlockState state);

    default void registerTileEntity() {
        Block block = (Block)this;
        List<Class<? extends TileEntity>> tiles = new ArrayList<>();
        for (IBlockState state : block.getBlockState().getValidStates()) {
            TileEntityInfo type = this.getTileEntityInfo(state);
            if (type != null && !tiles.contains(type.clazz)) {
                tiles.add(type.clazz);
                ResourceLocation name = getTileEntityName(state);
                if (CraftTileEntities.getTileEntityInfo(name) == null) {
                    CraftTileEntities.registerTileEntity(name.toString(), type);
                }
            }
        }
    }
}
