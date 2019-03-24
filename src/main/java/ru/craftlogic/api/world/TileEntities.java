package ru.craftlogic.api.world;

import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.IBlockAccess;
import net.minecraft.world.World;
import net.minecraft.world.chunk.Chunk;

import java.util.function.Consumer;

public final class TileEntities {
    private TileEntities() {}

    public static <T> T getTileEntity(IBlockAccess world, BlockPos pos, Class<T> type) {
        TileEntity tile = null;
        if (world instanceof World) {
            Chunk chunk = ((World) world).getChunk(pos);
            if (!chunk.isEmpty()) {
                tile = chunk.getTileEntity(pos, ((World) world).isRemote ?
                        Chunk.EnumCreateEntityType.QUEUED : Chunk.EnumCreateEntityType.IMMEDIATE);
            }
        } else {
            tile = world.getTileEntity(pos);
        }
        return tile == null || !type.isAssignableFrom(tile.getClass()) ? null : (T)tile;
    }

    public static <T> void withTileEntity(IBlockAccess world, BlockPos pos, Class<T> type, Consumer<T> action) {
        T tile = getTileEntity(world, pos, type);
        if (tile != null) {
            action.accept(tile);
        }
    }
}
