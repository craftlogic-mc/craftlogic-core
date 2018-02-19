package ru.craftlogic.api.world;

import com.google.gson.JsonObject;
import net.minecraft.block.Block;
import net.minecraft.block.properties.IProperty;
import net.minecraft.block.state.IBlockState;
import net.minecraft.entity.Entity;
import net.minecraft.init.Blocks;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.SoundCategory;
import net.minecraft.util.SoundEvent;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Vec3i;
import net.minecraft.world.Explosion;
import net.minecraft.world.World;
import net.minecraft.world.biome.Biome;
import ru.craftlogic.api.math.Bounding;
import ru.craftlogic.api.math.RadiusBounding;

import java.util.function.Consumer;

public final class Location extends BlockPos implements ChunkLocation {
    private final int dimension;
    private final double x, y, z;
    private final float yaw, pitch;

    public Location(World world, double x, double y, double z, float yaw, float pitch) {
        this(world.provider.getDimension(), x, y, z, yaw, pitch);
    }

    public Location(int dimension, double x, double y, double z, float yaw, float pitch) {
        super(x, y, z);
        this.x = x;
        this.y = y;
        this.z = z;
        this.yaw = yaw;
        this.pitch = pitch;
        this.dimension = dimension;
    }

    public Location(World world, double x, double y, double z) {
        this(world.provider.getDimension(), x, y, z);
    }

    public Location(int dimension, double x, double y, double z) {
        this(dimension, x, y, z, 0F, 0F);
    }

    public Location(World world, Vec3i pos) {
        this(world.provider.getDimension(), pos);
    }

    public Location(int dimension, Vec3i pos) {
        this(dimension, pos.getX(), pos.getY(), pos.getZ());
    }

    public Location(Entity entity) {
        this(entity.getEntityWorld(), entity.posX, entity.posY, entity.posZ, entity.rotationYaw, entity.rotationPitch);
    }

    public static Location fromJson(World world, JsonObject json) {
        return new Location(world,
            json.get("x").getAsDouble(),
            json.get("y").getAsDouble(),
            json.get("z").getAsDouble(),
            json.get("yaw").getAsFloat(),
            json.get("pitch").getAsFloat()
        );
    }

    public JsonObject toJson() {
        JsonObject json = new JsonObject();
        json.addProperty("x", this.x);
        json.addProperty("y", this.y);
        json.addProperty("z", this.z);
        json.addProperty("yaw", this.yaw);
        json.addProperty("pitch", this.pitch);
        return json;
    }

    public double getDX() {
        return this.x;
    }

    public double getDY() {
        return this.y;
    }

    public double getDZ() {
        return this.z;
    }

    @Override
    public int getDimension() {
        return this.dimension;
    }

    public boolean setBlockState(IBlockState state) {
        return this.getWorld().setBlockState(this, state);
    }

    public <B> B getBlock(Class<B> type) {
        Block block = this.getBlock();
        return block == Blocks.AIR || !type.isAssignableFrom(block.getClass()) ? null : (B)block;
    }

    public Block getBlock() {
        return this.getBlockState().getBlock();
    }

    public IBlockState getBlockState() {
        return this.getWorld().getBlockState(this);
    }

    public <T extends Comparable<T>> T getBlockProperty(IProperty<T> property) {
        return this.getBlockState().getValue(property);
    }

    public <T extends Comparable<T>> void setBlockProperty(IProperty<T> property, T value) {
        this.setBlockState(this.getBlockState().withProperty(property, value));
    }

    public <T extends Comparable<T>> void cycleBlockProperty(IProperty<T> property) {
        this.setBlockState(this.getBlockState().cycleProperty(property));
    }

    public Biome getBiome() {
        return this.getWorld().getBiome(this);
    }

    public TileEntity getTileEntity() {
        return this.getWorld().getTileEntity(this);
    }

    public <T> T getTileEntity(Class<T> type) {
        return TileEntities.getTileEntity(this.getWorld(), this, type);
    }

    public void withTileEntity(Consumer<TileEntity> action) {
        TileEntity tile = this.getTileEntity();
        if (tile != null) {
            action.accept(tile);
        }

    }

    public <T> void withTileEntity(Class<T> type, Consumer<T> action) {
        T tile = this.getTileEntity(type);
        if (tile != null) {
            action.accept(tile);
        }
    }

    public boolean setToAir() {
        return this.getWorld().setBlockToAir(this);
    }

    public void playSound(SoundEvent sound, SoundCategory category, float volume, float pitch) {
        double x = this.getX() + 0.5;
        double y = this.getY() + 0.5;
        double z = this.getZ() + 0.5;
        this.getWorld().playSound(null, x, y, z, sound, category, volume, pitch);
    }

    public Explosion explode(Entity exploder, float power, boolean igniteBlocks) {
        double x = this.getX() + 0.5;
        double y = this.getY() + 0.5;
        double z = this.getZ() + 0.5;
        return this.getWorld().createExplosion(exploder, x, y, z, power, igniteBlocks);
    }

    public float getYaw() {
        return this.yaw;
    }

    public float getPitch() {
        return this.pitch;
    }

    public Bounding toBounding(int radius) {
        return new RadiusBounding(this.x, this.y, this.z, radius) {
            @Override
            public double getStartY() {
                return Math.max(this.getStartY(), 0);
            }

            @Override
            public double getEndY() {
                return Math.min(this.getEndY(), Location.this.getWorld().getHeight());
            }
        };
    }

    public Bounding toFullHeightBounding(int radius) {
        return new RadiusBounding(this.x, this.y, this.z, radius) {
            @Override
            public double getStartY() {
                return 0;
            }

            @Override
            public double getEndY() {
                return Location.this.getWorld().getHeight();
            }
        };
    }

    public boolean isBlockLoaded() {
        return this.getWorld().isBlockLoaded(this);
    }

    @Override
    public int getChunkX() {
        return getX() >> 4;
    }

    @Override
    public int getChunkZ() {
        return getZ() >> 4;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof Location)) {
            if (!(o instanceof BlockPos)) {
                if (!(o instanceof ChunkLocation)) {
                    return false;
                } else {
                    ChunkLocation loc = (ChunkLocation)o;
                    return loc.getDimension() == getDimension() && loc.getChunkX() == getChunkX() && loc.getChunkZ() == getChunkZ();
                }
            } else {
                BlockPos pos = (BlockPos)o;
                return pos.getX() == getX() && pos.getY() == getY() && pos.getZ() == getZ();
            }
        }

        Location location = (Location) o;

        return location.x == x
                && location.y == y
                && location.z == z
                && location.yaw == yaw
                && location.pitch == pitch
                && location.dimension == dimension;
    }

    @Override
    public int hashCode() {
        int result = this.dimension;
        long temp = Double.doubleToLongBits(x);
        result = 31 * result + (int) (temp ^ (temp >>> 32));
        temp = Double.doubleToLongBits(y);
        result = 31 * result + (int) (temp ^ (temp >>> 32));
        temp = Double.doubleToLongBits(z);
        result = 31 * result + (int) (temp ^ (temp >>> 32));
        result = 31 * result + (yaw != 0 ? Float.floatToIntBits(yaw) : 0);
        result = 31 * result + (pitch != 0 ? Float.floatToIntBits(pitch) : 0);
        return result;
    }
}
