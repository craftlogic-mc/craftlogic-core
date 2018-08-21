package ru.craftlogic.common.script;

import net.minecraftforge.event.*;
import net.minecraftforge.event.entity.EntityEvent;
import net.minecraftforge.event.entity.living.LivingEvent;
import net.minecraftforge.event.world.BlockEvent;
import net.minecraftforge.event.world.ExplosionEvent;
import net.minecraftforge.event.world.NoteBlockEvent;
import net.minecraftforge.fml.common.eventhandler.Event;
import ru.craftlogic.api.event.player.PlayerSneakEvent;

import java.util.HashMap;
import java.util.Map;

public class Events {
    private static final Map<String, Class<? extends Event>> REGISTRY = new HashMap<>();

    static void init() {
        REGISTRY.clear();
        register("entity:enter_chunk", EntityEvent.EnteringChunk.class);
        register("entity:can_update", EntityEvent.EnteringChunk.class);
        register("entity:construct", EntityEvent.EntityConstructing.class);
        register("living:update", LivingEvent.LivingUpdateEvent.class);
        register("living:jump", LivingEvent.LivingJumpEvent.class);
        register("chat", ServerChatEvent.class);
        register("registry", RegistryEvent.class);
        register("loot_table_load", LootTableLoadEvent.class);
        register("difficulty_change", DifficultyChangeEvent.class);
        register("command", CommandEvent.class);
        register("anvil_update", AnvilUpdateEvent.class);
        register("portal_spawn", BlockEvent.PortalSpawnEvent.class);
        register("crop_grow:pre", BlockEvent.CropGrowEvent.Pre.class);
        register("crop_grow:post", BlockEvent.CropGrowEvent.Post.class);
        register("create_fluid_source", BlockEvent.CreateFluidSourceEvent.class);
        register("neighbor_notify", BlockEvent.NeighborNotifyEvent.class);
        register("block_multi_place", BlockEvent.MultiPlaceEvent.class);
        register("block_place", BlockEvent.PlaceEvent.class);
        register("block_break", BlockEvent.BreakEvent.class);
        register("harvest_drops", BlockEvent.HarvestDropsEvent.class);
        register("explosion", ExplosionEvent.class);
        register("explosion:start", ExplosionEvent.Start.class);
        register("explosion:detonate", ExplosionEvent.Detonate.class);
        register("note_block:change", NoteBlockEvent.Change.class);
        register("note_block:play", NoteBlockEvent.Play.class);
        register("player:sneak", PlayerSneakEvent.class);
        //TODO
    }

    public static void register(String name, Class<? extends Event> eventType) {
        if (REGISTRY.containsKey(name)) {
            throw new IllegalStateException("Event type " + name + " is already registered!");
        }
        REGISTRY.put(name, eventType);
    }

    public static Class<? extends Event> get(String name) {
        return REGISTRY.get(name);
    }
}
