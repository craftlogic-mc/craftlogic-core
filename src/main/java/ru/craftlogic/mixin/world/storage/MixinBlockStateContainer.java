package ru.craftlogic.mixin.world.storage;

import com.google.common.base.MoreObjects;
import net.minecraft.block.Block;
import net.minecraft.block.state.IBlockState;
import net.minecraft.init.Blocks;
import net.minecraft.network.PacketBuffer;
import net.minecraft.world.chunk.BlockStateContainer;
import net.minecraft.world.chunk.NibbleArray;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Overwrite;
import org.spongepowered.asm.mixin.Shadow;

import javax.annotation.Nullable;

@Mixin(BlockStateContainer.class)
public abstract class MixinBlockStateContainer {
    @Shadow
    private static int getIndex(int x, int y, int z) {
        return 0;
    }

    private static final IBlockState AIR_BLOCK_STATE = Blocks.AIR.getDefaultState();
    private final int[] storage = new int[4096];

    /**
     * @author Radviger
     * @reason Chunk IO performance optimisation
     */
    @Overwrite
    public void set(int x, int y, int z, IBlockState state) {
        set(getIndex(x, y, z), state);
    }

    /**
     * @author Radviger
     * @reason Chunk IO performance optimisation
     */
    @Overwrite
    protected void set(int index, IBlockState state) {
        storage[index] = Block.BLOCK_STATE_IDS.get(state);
    }

    /**
     * @author Radviger
     * @reason Chunk IO performance optimisation
     */
    @Overwrite
    public IBlockState get(int x, int y, int z) {
        return get(getIndex(x, y, z));
    }

    /**
     * @author Radviger
     * @reason Chunk IO performance optimisation
     */
    @Overwrite
    protected IBlockState get(int index) {
        IBlockState state = Block.BLOCK_STATE_IDS.getByValue(storage[index]);
        return MoreObjects.firstNonNull(state, AIR_BLOCK_STATE);
    }

    /**
     * @author Radviger
     * @reason Chunk IO performance optimisation
     */
    @Overwrite
    @SideOnly(Side.CLIENT)
    public void read(PacketBuffer buf) {
        for (int i = 0; i < 4096; i++) {
            storage[i] = buf.readVarInt();
        }
    }

    /**
     * @author Radviger
     * @reason Chunk IO performance optimisation
     */
    @Overwrite
    public void write(PacketBuffer buf) {
        for (int i = 0; i < 4096; i++) {
            buf.writeVarInt(storage[i]);
        }
    }

    /**
     * @author Radviger
     * @reason Chunk IO performance optimisation
     */
    @Overwrite
    @Nullable
    public NibbleArray getDataForNBT(byte[] blockIds, NibbleArray data) {
        NibbleArray result = null;

        for(int index = 0; index < 4096; ++index) {
            int id = storage[index];
            int x = index & 15;
            int y = index >> 8 & 15;
            int z = index >> 4 & 15;
            if ((id >> 12 & 15) != 0) {
                if (result == null) {
                    result = new NibbleArray();
                }

                result.set(x, y, z, id >> 12 & 15);
            }

            blockIds[index] = (byte)(id >> 4 & 255);
            data.set(x, y, z, id & 15);
        }

        return result;
    }

    /**
     * @author Radviger
     * @reason Chunk IO performance optimisation
     */
    @Overwrite
    public void setDataFromNBT(byte[] blockIds, NibbleArray data, @Nullable NibbleArray blockIdExtension) {
        for(int index = 0; index < 4096; ++index) {
            int x = index & 15;
            int y = index >> 8 & 15;
            int z = index >> 4 & 15;
            int meta = blockIdExtension == null ? 0 : blockIdExtension.get(x, y, z);
            int id = meta << 12 | (blockIds[index] & 255) << 4 | data.get(x, y, z);
            storage[index] = id;
        }

    }

    /**
     * @author Radviger
     * @reason Chunk IO performance optimisation
     */
    @Overwrite
    public int getSerializedSize() {
        return 4 * 4096;
    }
}
