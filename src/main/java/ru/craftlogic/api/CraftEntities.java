package ru.craftlogic.api;

import net.minecraft.client.renderer.entity.Render;
import net.minecraft.client.renderer.entity.RenderManager;
import net.minecraft.entity.Entity;
import net.minecraft.util.ResourceLocation;
import net.minecraftforge.fml.client.registry.RenderingRegistry;
import net.minecraftforge.fml.common.registry.EntityRegistry;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;
import ru.craftlogic.client.render.entity.RenderThrownItem;
import ru.craftlogic.common.entity.EntityThrownItem;

import java.util.function.Function;

import static ru.craftlogic.api.CraftAPI.MOD_ID;
import static ru.craftlogic.api.CraftAPI.wrapWithActiveModId;


public class CraftEntities {
    private static int nextEntityId;

    static void init(Side side) {
        registerEntity(EntityThrownItem.class, "thrown_item", 64, 10, true);
        if (side == Side.CLIENT) {
            initClient();
        }
    }

    @SideOnly(Side.CLIENT)
    private static void initClient() {
        registerEntityRenderer(EntityThrownItem.class, RenderThrownItem::new);
    }

    public static <E extends Entity> void registerEntity(Class<E> type, String name, int trackingRange, int updateFrequency, boolean sendsVelocityUpdates) {
        ResourceLocation id = wrapWithActiveModId(name, MOD_ID);
        EntityRegistry.registerModEntity(id, type, id.getPath(), nextEntityId++, id.getNamespace(), trackingRange, updateFrequency, sendsVelocityUpdates);
    }

    public static <E extends Entity> void registerEntity(Class<E> type, String name, int trackingRange, int updateFrequency, boolean sendsVelocityUpdates, int eggPrimary, int eggSecondary) {
        ResourceLocation id = wrapWithActiveModId(name, MOD_ID);
        EntityRegistry.registerModEntity(id, type, id.getPath(), nextEntityId++, id.getNamespace(), trackingRange, updateFrequency, sendsVelocityUpdates, eggPrimary, eggSecondary);
    }

    @SideOnly(Side.CLIENT)
    public static <E extends Entity> void registerEntityRenderer(Class<E> type, Function<RenderManager, Render<E>> renderer) {
        RenderingRegistry.registerEntityRenderingHandler(type, renderer::apply);
    }
}
