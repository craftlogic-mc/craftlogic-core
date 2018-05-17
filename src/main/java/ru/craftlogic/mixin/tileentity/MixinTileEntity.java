package ru.craftlogic.mixin.tileentity;

import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.registry.RegistryNamespaced;
import net.minecraft.world.World;
import net.minecraftforge.fml.common.FMLLog;
import org.apache.logging.log4j.Logger;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Overwrite;
import org.spongepowered.asm.mixin.Shadow;
import ru.craftlogic.CraftLogic;
import ru.craftlogic.api.tile.TileEntityBase;
import ru.craftlogic.api.util.TileEntityInfo;

@Mixin(TileEntity.class)
public class MixinTileEntity {
    @Shadow @Final
    private static Logger LOGGER;
    @Shadow @Final
    private static RegistryNamespaced<ResourceLocation, Class<? extends TileEntity>> REGISTRY;

    @Overwrite
    public static TileEntity create(World world, NBTTagCompound compound) {
        TileEntity tile = null;
        String s = compound.getString("id");
        Class type = null;

        try {
            type = REGISTRY.getObject(new ResourceLocation(s));
            if (type != null) {
                TileEntityInfo<?> i = CraftLogic.getTileEntityInfo(type);
                if (TileEntityBase.class.isAssignableFrom(type)) {
                    tile = i != null ? i.create(world) : null;
                } else {
                    tile = (TileEntity)type.newInstance();
                }
            }
        } catch (Throwable t) {
            LOGGER.error("Failed to create block entity {}", s, t);
            FMLLog.log.error("A TileEntity {}({}) has thrown an exception during loading, its state cannot be restored. Report this to the mod author", s, type == null ? null : type.getName(), t);
        }

        if (tile != null) {
            try {
                ((MixinTileEntity)(Object)tile).setWorldCreate(world);
                tile.readFromNBT(compound);
            } catch (Throwable t) {
                LOGGER.error("Failed to load data for block entity {}", s, t);
                FMLLog.log.error("A TileEntity {}({}) has thrown an exception during loading, its state cannot be restored. Report this to the mod author", s, type.getName(), t);
                tile = null;
            }
        } else {
            LOGGER.warn("Skipping BlockEntity with id {}", s);
        }

        return tile;
    }

    @Shadow
    protected void setWorldCreate(World world) {}
}
