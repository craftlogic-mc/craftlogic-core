package ru.craftlogic.api.world;

import com.google.gson.JsonObject;
import net.minecraft.block.Block;
import net.minecraft.block.material.Material;
import net.minecraft.block.properties.IProperty;
import net.minecraft.block.state.BlockFaceShape;
import net.minecraft.block.state.IBlockState;
import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.init.Blocks;
import net.minecraft.inventory.InventoryHelper;
import net.minecraft.item.ItemStack;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.EnumParticleTypes;
import net.minecraft.util.SoundCategory;
import net.minecraft.util.SoundEvent;
import net.minecraft.util.math.*;
import net.minecraft.world.Explosion;
import net.minecraft.world.World;
import net.minecraft.world.WorldServer;
import net.minecraft.world.biome.Biome;
import net.minecraftforge.common.IPlantable;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;
import ru.craftlogic.api.math.Bounding;
import ru.craftlogic.api.math.CenteredBounding;

import java.lang.ref.WeakReference;
import java.util.Random;
import java.util.function.Consumer;

public class Location extends ChunkLocation {
    private final double x, y, z;
    private float yaw, pitch;
    private WeakReference<World> world;

    public Location(Location location) {
        this(
            location.getDimensionId(),
            location.getX(), location.getY(), location.getZ(),
            location.getYaw(), location.getPitch()
        );
    }

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
        this.world = new WeakReference<>(world);
    }

    public Location(int dimension, double x, double y, double z) {
        this(dimension, x, y, z, 0F, 0F);
    }

    public Location(World world, Vec3i pos) {
        this(world.provider.getDimension(), pos);
        this.world = new WeakReference<>(world);
    }

    public Location(int dimension, Vec3i pos) {
        this(dimension,
            pos.getX() + 0.5,
            pos.getY() + 0.5,
            pos.getZ() + 0.5
        );
    }

    public Location(Entity entity) {
        this(entity.getEntityWorld(), entity.posX, entity.posY, entity.posZ, entity.rotationYaw, entity.rotationPitch);
    }

    protected Location(BlockPos pos) {
        super(pos.getX() >> 4, pos.getZ() >> 4);
        this.x = pos.getX() + 0.5;
        this.y = pos.getY() + 0.5;
        this.z = pos.getZ() + 0.5;
    }

    public static Location deserialize(World world, JsonObject json) {
        return deserialize(world.provider.getDimension(), json);
    }

    public static Location deserialize(int dimension, JsonObject json) {
        return new Location(dimension,
            json.get("x").getAsDouble(),
            json.get("y").getAsDouble(),
            json.get("z").getAsDouble(),
            json.get("yaw").getAsFloat(),
            json.get("pitch").getAsFloat()
        );
    }

    public JsonObject serialize() {
        JsonObject json = new JsonObject();
        json.addProperty("x", getX());
        json.addProperty("y", getY());
        json.addProperty("z", getZ());
        json.addProperty("yaw", getYaw());
        json.addProperty("pitch", getPitch());
        return json;
    }

    public int getBlockX() {
        return MathHelper.floor(getX());
    }

    public int getBlockY() {
        return MathHelper.floor(getY());
    }

    public int getBlockZ() {
        return MathHelper.floor(getZ());
    }

    public double getX() {
        return x;
    }

    public double getY() {
        return y;
    }

    public double getZ() {
        return z;
    }

    public float getYaw() {
        return yaw;
    }

    public float getPitch() {
        return pitch;
    }

    public BlockPos getPos() {
        return new BlockPos(getBlockX(), getBlockY(), getBlockZ());
    }

    public boolean isSameBlock(Block block) {
        return getBlock() == block;
    }

    public boolean isSameBlockMaterial(Material material) {
        return getBlockMaterial() == material;
    }

    public boolean setBlock(Block block) {
        return setBlockState(block.getDefaultState());
    }

    public boolean setBlock(Block block, int flag) {
        return setBlockState(block.getDefaultState(), flag);
    }

    public boolean setBlockState(IBlockState state) {
        return getWorld().setBlockState(getPos(), state);
    }

    public boolean setBlockState(IBlockState state, int flag) {
        return getWorld().setBlockState(getPos(), state, flag);
    }

    public <B> B getBlock(Class<B> type) {
        Block block = getBlock();
        return block == Blocks.AIR || !type.isAssignableFrom(block.getClass()) ? null : (B)block;
    }

    public Block getBlock() {
        return getBlockState().getBlock();
    }

    public Material getBlockMaterial() {
        return getBlockState().getMaterial();
    }

    public IBlockState getActualBlockState() {
        return getBlockState().getActualState(getBlockAccessor(), getPos());
    }

    public IBlockState getBlockState() {
        return getBlockAccessor().getBlockState(getPos());
    }

    @Override
    public World getWorld() {
        return world != null ? world.get() : super.getWorld();
    }

    public <T extends Comparable<T>> T getBlockProperty(IProperty<T> property) {
        return getBlockState().getValue(property);
    }

    public <T extends Comparable<T>> void setBlockProperty(IProperty<T> property, T value) {
        setBlockState(getBlockState().withProperty(property, value));
    }

    public <T extends Comparable<T>> void setBlockProperty(IProperty<T> property, T value, int flag) {
        setBlockState(getBlockState().withProperty(property, value), flag);
    }

    public <T extends Comparable<T>> T cycleBlockProperty(IProperty<T> property) {
        IBlockState newState = getBlockState().cycleProperty(property);
        setBlockState(newState);
        return newState.getValue(property);
    }

    public <T extends Comparable<T>> T cycleBlockProperty(IProperty<T> property, int flag) {
        IBlockState newState = getBlockState().cycleProperty(property);
        setBlockState(newState, flag);
        return newState.getValue(property);
    }

    public AxisAlignedBB getBlockBounding() {
        return getBlockState().getBoundingBox(getWorld(), getPos());
    }

    public BlockFaceShape getBlockFaceShape(EnumFacing side) {
        return getBlockState().getBlockFaceShape(getBlockAccessor(), getPos(), side);
    }

    public Biome getBiome() {
        return getChunk().getBiome(getPos(), getWorld().getBiomeProvider());
    }

    public float getBiomeTemperature() {
        return getBiome().getTemperature(getPos());
    }

    public float getBiomeRainfall() {
        return getBiome().getRainfall();
    }

    public TileEntity getTileEntity() {
        return getBlockAccessor().getTileEntity(getPos());
    }

    public <T> T getTileEntity(Class<T> type) {
        return TileEntities.getTileEntity(getBlockAccessor(), getPos(), type);
    }

    public void withTileEntity(Consumer<TileEntity> action) {
        TileEntity tile = getTileEntity();
        if (tile != null) {
            action.accept(tile);
        }
    }

    public <T> void withTileEntity(Class<T> type, Consumer<T> action) {
        T tile = getTileEntity(type);
        if (tile != null) {
            action.accept(tile);
        }
    }

    public boolean setBlockToAir() {
        return setBlockToAir(false);
    }

    public boolean setBlockToAir(boolean drop) {
        if (drop) {
            getBlock().dropBlockAsItem(getWorld(), getPos(), getBlockState(), 0);
        }
        return getWorld().setBlockToAir(getPos());
    }

    public void spawnParticle(EnumParticleTypes particle, double ox, double oy, double oz, double mx, double my, double mz, int... data) {
        World world = getWorld();
        if (world instanceof WorldServer) {
            ((WorldServer)world).spawnParticle(particle, getX() + ox, getY() + oy, getZ() + oz, 1, mx, my, mz, 1.0, data);
        } else {
            world.spawnParticle(particle, getX() + ox, getY() + oy, getZ() + oz, mx, my, mz, data);
        }
    }

    public void playSound(SoundEvent sound, SoundCategory category, float volume, float pitch) {
        World world = getWorld();
        if (world.isRemote) {
            world.playSound(getX(), getY(), getZ(), sound, category, volume, pitch, false);
        } else {
            world.playSound(null, getX(), getY(), getZ(), sound, category, volume, pitch);
        }
    }

    public void playEvent(int id, int data) {
        getWorld().playEvent(id, getPos(), data);
    }

    public Explosion explode(Entity exploder, float power, boolean igniteBlocks, boolean damageBlocks) {
        return getWorld().newExplosion(exploder, getX(), getY(), getZ(), power, igniteBlocks, damageBlocks);
    }

    public boolean canBlockBePlaced(Block block) {
        return getBlock().isReplaceable(getWorld(), getPos()) && block.canPlaceBlockAt(getWorld(), getPos());
    }

    public boolean setBlockIfPossible(Block block) {
        if (canBlockBePlaced(block)) {
            setBlock(block);
            return true;
        } else {
            return false;
        }
    }

    public Bounding toBounding(int width, int height, int depth) {
        return new CenteredBounding(getX(), getY(), getZ(), width, height, depth) {
            @Override
            public double getStartY() {
                return Math.max(getStartY(), 0);
            }

            @Override
            public double getEndY() {
                return Math.min(getEndY(), getWorld().getHeight());
            }
        };
    }

    public Bounding toFullHeightBounding(int width, int depth) {
        return new CenteredBounding(getX(), getY(), getZ(), width, 0, depth) {
            @Override
            public double getStartY() {
                return 0;
            }

            @Override
            public double getEndY() {
                return getWorld().getHeight();
            }
        };
    }

    @Override
    public Location offset(EnumFacing side) {
        return offset(side, 1);
    }

    @Override
    public Location offset(EnumFacing side, int amount) {
        return add(
            side.getXOffset() * amount,
            side.getYOffset() * amount,
            side.getZOffset() * amount
        );
    }

    public Location offset(EnumFacing side, double amount) {
        return add(
            side.getXOffset() * amount,
            side.getYOffset() * amount,
            side.getZOffset() * amount
        );
    }

    public Location randomize(Random rand, double range) {
        return randomize(rand, range, range, range);
    }

    public Location randomize(Random rand, double rangeX, double rangeY, double rangeZ) {
        return add(
            rand.nextDouble() * rangeX - (rangeX / 2.0),
            rand.nextDouble() * rangeY - (rangeY / 2.0),
            rand.nextDouble() * rangeZ - (rangeZ / 2.0)
        );
    }

    protected Location add(double x, double y, double z) {
        return new Location(getDimensionId(), getX() + x, getY() + y, getZ() + z);
    }

    public boolean isBlockLoaded() {
        return isWorldLoaded() && getWorld().isBlockLoaded(getPos());
    }

    public boolean isWorldLoaded() {
        return getWorld() != null;
    }

    public boolean isSideSolid(EnumFacing facing) {
        return isSideSolid(facing, false);
    }

    public boolean isSideSolid(EnumFacing facing, boolean def) {
        return getBlockAccessor().isSideSolid(getPos(), facing, def);
    }

    public boolean spawnEntity(Entity entity) {
        return getWorld().spawnEntity(entity);
    }

    public void spawnItem(ItemStack item) {
        InventoryHelper.spawnItemStack(getWorld(), getX(), getY(), getZ(), item);
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
        getWorld().removeTileEntity(getPos());
    }

    public EnumFacing getDirectionFromEntityLiving(EntityLivingBase entity) {
        return EnumFacing.getDirectionFromEntityLiving(getPos(), entity);
    }

    public boolean isAreaLoaded(int radius) {
        return getWorld().isAreaLoaded(getPos(), radius);
    }

    public int getLightFromNeighbors() {
        return getWorld().getLightFromNeighbors(getPos());
    }

    public int getBlockLightOpacity() {
        return getBlock().getLightOpacity(getBlockState(), getWorld(), getPos());
    }

    public boolean canBlockBeConnectedTo(EnumFacing side) {
        return getBlock().canBeConnectedTo(getBlockAccessor(), getPos(), side);
    }

    public int getLight() {
        return getWorld().getLight(getPos());
    }

    @Override
    public boolean isWithinWorldBorder() {
        return getWorld().getWorldBorder().contains(getPos());
    }

    public boolean canBlockSeeSky() {
        return getWorld().canBlockSeeSky(getPos());
    }

    public boolean canBlockFreeze(boolean def) {
        return getWorld().canBlockFreeze(getPos(), def);
    }

    public boolean isHeightValid() {
        int y = getPos().getY();
        return y >= 0 && y < 256;
    }

    public boolean isAir() {
        return getBlockMaterial() == Material.AIR;
    }

    @SideOnly(Side.CLIENT)
    public int getFoliageColor() {
        return getBiome().getFoliageColorAtPos(getPos());
    }

    public double distance(Location other) {
        double sq = distanceSq(other);
        return Math.sqrt(sq);
    }

    public double distanceSq(Location other) {
        return getDimensionId() != other.getDimensionId() ? Double.POSITIVE_INFINITY : getPos().distanceSq(other.getPos());
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof Location)) {
            if (!(o instanceof ChunkLocation)) {
                return false;
            } else {
                ChunkLocation loc = (ChunkLocation)o;
                return loc.getDimensionId() == getDimensionId() && loc.getChunkX() == getChunkX() && loc.getChunkZ() == getChunkZ();
            }
        }

        Location location = (Location) o;

        return location.x == x
            && location.y == y
            && location.z == z
            && location.yaw == yaw
            && location.pitch == pitch
            && location.getDimensionId() == getDimensionId();
    }

    @Override
    public int hashCode() {
        int result = getDimensionId();
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
