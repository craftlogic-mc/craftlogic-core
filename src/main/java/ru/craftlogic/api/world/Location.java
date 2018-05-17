package ru.craftlogic.api.world;

import com.google.gson.JsonObject;
import net.minecraft.block.Block;
import net.minecraft.block.material.Material;
import net.minecraft.block.properties.IProperty;
import net.minecraft.block.state.IBlockState;
import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.init.Blocks;
import net.minecraft.item.ItemStack;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.EnumParticleTypes;
import net.minecraft.util.SoundCategory;
import net.minecraft.util.SoundEvent;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Vec3d;
import net.minecraft.util.math.Vec3i;
import net.minecraft.world.Explosion;
import net.minecraft.world.World;
import net.minecraft.world.WorldServer;
import net.minecraft.world.biome.Biome;
import net.minecraftforge.common.IPlantable;
import ru.craftlogic.api.math.Bounding;
import ru.craftlogic.api.math.RadiusBounding;

import java.util.Random;
import java.util.function.Consumer;

public class Location extends ChunkLocation {
    private final double x, y, z;
    private float yaw, pitch;

    public Location(World world, Vec3d vec) {
        this(world, vec.x, vec.y, vec.z);
    }

    public Location(World world, double x, double y, double z, float yaw, float pitch) {
        this(world.provider.getDimension(), x, y, z, yaw, pitch);
    }

    public Location(int dimension, double x, double y, double z, float yaw, float pitch) {
        super(dimension, (int)x >> 4, (int)z >> 4);
        this.x = x;
        this.y = y;
        this.z = z;
        this.yaw = yaw;
        this.pitch = pitch;
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
        this(dimension,
            pos.getX() + (pos.getX() < 0 ? -0.5 : 0.5),
            pos.getY() + (pos.getY() < 0 ? -0.5 : 0.5),
            pos.getZ() + (pos.getZ() < 0 ? -0.5 : 0.5)
        );
    }

    public Location(Entity entity) {
        this(entity.getEntityWorld(), entity.posX, entity.posY, entity.posZ, entity.rotationYaw, entity.rotationPitch);
    }

    protected Location(BlockPos pos) {
        super(pos.getX() >> 4, pos.getZ() >> 4);
        this.x = pos.getX();
        this.y = pos.getY();
        this.z = pos.getZ();
    }

    public static Location deserialize(World world, JsonObject json) {
        return new Location(world,
            json.get("x").getAsDouble(),
            json.get("y").getAsDouble(),
            json.get("z").getAsDouble(),
            json.get("yaw").getAsFloat(),
            json.get("pitch").getAsFloat()
        );
    }

    public JsonObject serialize() {
        JsonObject json = new JsonObject();
        json.addProperty("x", this.x);
        json.addProperty("y", this.y);
        json.addProperty("z", this.z);
        json.addProperty("yaw", this.yaw);
        json.addProperty("pitch", this.pitch);
        return json;
    }

    public int getBlockX() {
        return (int)this.x;
    }

    public int getBlockY() {
        return (int)this.y;
    }

    public int getBlockZ() {
        return (int)this.z;
    }

    public double getX() {
        return this.x;
    }

    public double getY() {
        return this.y;
    }

    public double getZ() {
        return this.z;
    }

    public float getYaw() {
        return this.yaw;
    }

    public float getPitch() {
        return this.pitch;
    }

    public BlockPos getPos() {
        return new BlockPos(this.getBlockX(), this.getBlockY(), this.getBlockZ());
    }

    public boolean isSameBlock(Block block) {
        return this.getBlock() == block;
    }

    public boolean isSameBlockMaterial(Material material) {
        return this.getBlockMaterial() == material;
    }

    public boolean setBlock(Block block) {
        return this.setBlockState(block.getDefaultState());
    }

    public boolean setBlockState(IBlockState state) {
        return this.getWorld().setBlockState(this.getPos(), state);
    }

    public boolean setBlockState(IBlockState state, int flag) {
        return this.getWorld().setBlockState(this.getPos(), state, flag);
    }

    public <B> B getBlock(Class<B> type) {
        Block block = this.getBlock();
        return block == Blocks.AIR || !type.isAssignableFrom(block.getClass()) ? null : (B)block;
    }

    public Block getBlock() {
        return this.getBlockState().getBlock();
    }

    public Material getBlockMaterial() {
        return this.getBlockState().getMaterial();
    }

    public IBlockState getActualBlockState() {
        return this.getBlockState().getActualState(this.getBlockAccessor(), this.getPos());
    }

    public IBlockState getBlockState() {
        return this.getBlockAccessor().getBlockState(this.getPos());
    }

    public <T extends Comparable<T>> T getBlockProperty(IProperty<T> property) {
        return this.getBlockState().getValue(property);
    }

    public <T extends Comparable<T>> void setBlockProperty(IProperty<T> property, T value) {
        this.setBlockState(this.getBlockState().withProperty(property, value));
    }

    public <T extends Comparable<T>> void setBlockProperty(IProperty<T> property, T value, int flag) {
        this.setBlockState(this.getBlockState().withProperty(property, value), flag);
    }

    public <T extends Comparable<T>> void cycleBlockProperty(IProperty<T> property) {
        this.setBlockState(this.getBlockState().cycleProperty(property));
    }

    public <T extends Comparable<T>> void cycleBlockProperty(IProperty<T> property, int flag) {
        this.setBlockState(this.getBlockState().cycleProperty(property), flag);
    }

    public Biome getBiome() {
        return this.getBlockAccessor().getBiome(this.getPos());
    }

    public float getBiomeTemperature() {
        return this.getBiome().getTemperature(this.getPos());
    }

    public float getBiomeRainfall() {
        return this.getBiome().getRainfall();
    }

    public TileEntity getTileEntity() {
        return this.getBlockAccessor().getTileEntity(this.getPos());
    }

    public <T> T getTileEntity(Class<T> type) {
        return TileEntities.getTileEntity(this.getBlockAccessor(), this.getPos(), type);
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

    public boolean setBlockToAir() {
        return this.setBlockToAir(false);
    }

    public boolean setBlockToAir(boolean drop) {
        if (drop) {
            getBlock().dropBlockAsItem(getWorld(), getPos(), getBlockState(), 0);
        }
        return this.getWorld().setBlockToAir(this.getPos());
    }

    public void spawnParticle(EnumParticleTypes particle, double ox, double oy, double oz, double mx, double my, double mz, int... data) {
        World world = this.getWorld();
        if (world instanceof WorldServer) {
            ((WorldServer)world).spawnParticle(particle, ox, oy, oz, 1, mx, my, mz,  1.0, data);
        } else {
            world.spawnParticle(particle, this.x + ox, this.y + oy, this.z + oz, mx, my, mz, data);
        }
    }

    public void playSound(SoundEvent sound, SoundCategory category, float volume, float pitch) {
        this.getWorld().playSound(null, this.x, this.y, this.z, sound, category, volume, pitch);
    }

    public void playEvent(int id, int data) {
        getWorld().playEvent(id, this.getPos(), data);
    }

    public Explosion explode(Entity exploder, float power, boolean igniteBlocks, boolean damageBlocks) {
        return this.getWorld().newExplosion(exploder, this.x, this.y, this.z, power, igniteBlocks, damageBlocks);
    }

    public boolean canBlockBePlaced(Block block) {
        return block.canPlaceBlockAt(this.getWorld(), this.getPos());
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

    public Location offset(EnumFacing side) {
        return this.offset(side, 1);
    }

    public Location offset(EnumFacing side, double amount) {
        return this.add(
            side.getFrontOffsetX() * amount,
            side.getFrontOffsetY() * amount,
            side.getFrontOffsetZ() * amount
        );
    }

    public Location randomize(Random rand, double range) {
        double offset = range / 2.0;
        return this.add(
            rand.nextDouble() * range - offset,
            rand.nextDouble() * range - offset,
            rand.nextDouble() * range - offset
        );
    }

    private Location add(double x, double y, double z) {
        return new Location(this.getDimension(), this.x + x, this.y + y, this.z + z);
    }

    public boolean isBlockLoaded() {
        return this.getWorld().isBlockLoaded(this.getPos());
    }

    public boolean isSideSolid(EnumFacing facing) {
        return this.isSideSolid(facing, false);
    }

    public boolean isSideSolid(EnumFacing facing, boolean def) {
        return this.getBlockAccessor().isSideSolid(this.getPos(), facing, def);
    }

    public void spawnItem(ItemStack item) {
        Block.spawnAsEntity(this.getWorld(), this.getPos(), item);
    }

    public boolean isWorldRemote() {
        return getWorld().isRemote;
    }

    public boolean isRaining() {
        return getWorld().isRainingAt(getPos());
    }

    public boolean canSustainPlant(EnumFacing side, IPlantable plant) {
        return getBlock().canSustainPlant(getBlockState(), getBlockAccessor(), getPos(), side, plant);
    }

    public void removeTileEntity() {
        this.getWorld().removeTileEntity(this.getPos());
    }

    public EnumFacing getDirectionFromEntityLiving(EntityLivingBase entity) {
        return EnumFacing.getDirectionFromEntityLiving(this.getPos(), entity);
    }

    public boolean isAreaLoaded(int radius) {
        return getWorld().isAreaLoaded(getPos(), radius);
    }

    public int getLightFromNeighbors() {
        return getWorld().getLightFromNeighbors(getPos());
    }

    public int getLight() {
        return getWorld().getLight(getPos());
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof Location)) {
            if (!(o instanceof ChunkLocation)) {
                return false;
            } else {
                ChunkLocation loc = (ChunkLocation)o;
                return loc.getDimension() == getDimension() && loc.getChunkX() == getChunkX() && loc.getChunkZ() == getChunkZ();
            }
        }

        Location location = (Location) o;

        return location.x == x
                && location.y == y
                && location.z == z
                && location.yaw == yaw
                && location.pitch == pitch
                && location.getDimension() == getDimension();
    }

    @Override
    public int hashCode() {
        int result = this.getDimension();
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
