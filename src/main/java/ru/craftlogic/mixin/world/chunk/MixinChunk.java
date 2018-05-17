package ru.craftlogic.mixin.world.chunk;

import net.minecraft.block.Block;
import net.minecraft.block.state.IBlockState;
import net.minecraft.init.Blocks;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.EnumSkyBlock;
import net.minecraft.world.World;
import net.minecraft.world.chunk.Chunk;
import net.minecraft.world.chunk.Chunk.EnumCreateEntityType;
import net.minecraft.world.chunk.storage.ExtendedBlockStorage;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Overwrite;
import org.spongepowered.asm.mixin.Shadow;

import javax.annotation.Nullable;
import java.util.Map;
import java.util.concurrent.ConcurrentLinkedQueue;

import static net.minecraft.world.chunk.Chunk.NULL_BLOCK_STORAGE;

@Mixin(Chunk.class)
public class MixinChunk  {
    @Shadow @Final
    private int[] precipitationHeightMap, heightMap;
    @Shadow @Final
    private ExtendedBlockStorage[] storageArrays;
    @Shadow @Final
    private World world;
    @Shadow @Final
    private ConcurrentLinkedQueue<BlockPos> tileEntityPosQueue;
    @Shadow @Final
    private Map<BlockPos, TileEntity> tileEntities;
    @Shadow
    private boolean dirty;

    @Nullable
    @Overwrite
    public IBlockState setBlockState(BlockPos pos, IBlockState newState) {
        int localX = pos.getX() & 15;
        int y = pos.getY();
        int localZ = pos.getZ() & 15;
        int idx = localZ << 4 | localX;
        if (y >= this.precipitationHeightMap[idx] - 1) {
            this.precipitationHeightMap[idx] = -999;
        }

        int height = this.heightMap[idx];
        IBlockState oldState = this.getBlockState(pos);
        if (oldState == newState) {
            return null;
        } else {
            Block newBlock = newState.getBlock();
            Block oldBlock = oldState.getBlock();
            int oldLightOpacity = oldState.getLightOpacity(this.world, pos);
            ExtendedBlockStorage blockStorage = this.storageArrays[y >> 4];
            boolean flag = false;
            if (blockStorage == NULL_BLOCK_STORAGE) {
                if (newBlock == Blocks.AIR) {
                    return null;
                }

                blockStorage = new ExtendedBlockStorage(y >> 4 << 4, this.world.provider.hasSkyLight());
                this.storageArrays[y >> 4] = blockStorage;
                flag = y >= height;
            }

            blockStorage.set(localX, y & 15, localZ, newState);
            if (!this.world.isRemote) {
                if (oldBlock != newBlock) {
                    oldBlock.breakBlock(this.world, pos, oldState);
                }

                TileEntity tile = this.getTileEntity(pos, EnumCreateEntityType.CHECK);
                if (tile != null && tile.shouldRefresh(this.world, pos, oldState, newState)) {
                    this.world.removeTileEntity(pos);
                }
            } else if (oldBlock.hasTileEntity(oldState)) {
                TileEntity tile = this.getTileEntity(pos, EnumCreateEntityType.CHECK);
                if (tile != null && tile.shouldRefresh(this.world, pos, oldState, newState)) {
                    this.world.removeTileEntity(pos);
                }
            }

            if (blockStorage.get(localX, y & 15, localZ).getBlock() != newBlock) {
                return null;
            } else {
                if (flag) {
                    this.generateSkylightMap();
                } else {
                    int newLightOpacity = newState.getLightOpacity(this.world, pos);
                    if (newLightOpacity > 0) {
                        if (y >= height) {
                            this.relightBlock(localX, y + 1, localZ);
                        }
                    } else if (y == height - 1) {
                        this.relightBlock(localX, y, localZ);
                    }

                    if (newLightOpacity != oldLightOpacity && (newLightOpacity < oldLightOpacity || this.getLightFor(EnumSkyBlock.SKY, pos) > 0 || this.getLightFor(EnumSkyBlock.BLOCK, pos) > 0)) {
                        this.propagateSkylightOcclusion(localX, localZ);
                    }
                }

                if (!this.world.isRemote && oldBlock != newBlock && (!this.world.captureBlockSnapshots || newBlock.hasTileEntity(newState))) {
                    newBlock.onBlockAdded(this.world, pos, newState);
                }

                if (newBlock.hasTileEntity(newState)) {
                    TileEntity tile = this.getTileEntity(pos, EnumCreateEntityType.CHECK);
                    if (tile == null) {
                        tile = newBlock.createTileEntity(this.world, newState);
                        this.world.setTileEntity(pos, tile);
                    }

                    if (tile != null) {
                        tile.updateContainingBlockInfo();
                    }
                }

                this.dirty = true;
                return oldState;
            }
        }
    }

    @Nullable
    @Overwrite
    public TileEntity getTileEntity(BlockPos pos, EnumCreateEntityType mode) {
        TileEntity tile = this.tileEntities.get(pos);
        if (tile != null && tile.isInvalid()) {
            this.tileEntities.remove(pos);
            tile = null;
        }

        if (tile == null) {
            if (mode == EnumCreateEntityType.IMMEDIATE) {
                this.world.setTileEntity(pos, tile);
            } else if (mode == EnumCreateEntityType.QUEUED) {
                this.tileEntityPosQueue.add(pos.toImmutable());
            }
        }

        return tile;
    }

    @Shadow
    public int getLightFor(EnumSkyBlock type, BlockPos pos) { return 0; }

    @Shadow
    public IBlockState getBlockState(BlockPos pos) { return null; }

    @Nullable
    @Shadow
    private TileEntity createNewTileEntity(BlockPos pos) { return null; }

    @Shadow
    public void generateSkylightMap() { }

    @Shadow
    private void propagateSkylightOcclusion(int x, int z) { }

    @Shadow
    private void relightBlock(int x, int y, int z) { }
}
