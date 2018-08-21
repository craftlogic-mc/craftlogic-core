package ru.craftlogic.api;

import net.minecraft.client.renderer.tileentity.TileEntitySpecialRenderer;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.ResourceLocation;
import net.minecraftforge.fml.client.registry.ClientRegistry;
import net.minecraftforge.fml.common.registry.GameRegistry;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;
import ru.craftlogic.api.util.TileEntityInfo;
import ru.craftlogic.client.render.tileentity.RenderBarrel;
import ru.craftlogic.common.tileentity.TileEntityBarrel;

import javax.annotation.Nonnull;
import java.util.HashMap;
import java.util.Map;
import java.util.function.Supplier;

import static ru.craftlogic.api.CraftAPI.MOD_ID;
import static ru.craftlogic.api.CraftAPI.wrapWithActiveModId;

public class CraftTileEntities {
    static Map<ResourceLocation, TileEntityInfo<?>> TILE_REGISTRY = new HashMap<>();

    static void init(Side side) {
        if (side == Side.CLIENT) {
            initClient();
        }
    }

    @SideOnly(Side.CLIENT)
    private static void initClient() {
        registerTileEntityRenderer(TileEntityBarrel.class, RenderBarrel::new);
    }

    public static void registerTileEntity(@Nonnull String name, TileEntityInfo<?> type) {
        ResourceLocation id = wrapWithActiveModId(name, MOD_ID);
        GameRegistry.registerTileEntity(type.clazz, id);
        TILE_REGISTRY.put(id, type);
    }

    public static <T extends TileEntity> TileEntityInfo<T> getTileEntityInfo(ResourceLocation name) {
        return (TileEntityInfo<T>) TILE_REGISTRY.get(name);
    }

    public static <T extends TileEntity> TileEntityInfo<T> getTileEntityInfo(Class<T> clazz) {
        for (TileEntityInfo<?> type : TILE_REGISTRY.values()) {
            if (type.clazz == clazz) {
                return (TileEntityInfo<T>) type;
            }
        }
        return null;
    }

    @SideOnly(Side.CLIENT)
    public static <E extends TileEntity> void registerTileEntityRenderer(Class<E> type, Supplier<TileEntitySpecialRenderer<E>> renderer) {
        ClientRegistry.bindTileEntitySpecialRenderer(type, renderer.get());
    }
}
