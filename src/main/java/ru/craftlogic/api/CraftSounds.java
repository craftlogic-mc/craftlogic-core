package ru.craftlogic.api;

import net.minecraft.client.Minecraft;
import net.minecraft.client.audio.SoundHandler;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.SoundCategory;
import net.minecraft.util.SoundEvent;
import net.minecraftforge.fml.common.registry.GameRegistry;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;
import ru.craftlogic.api.sound.SoundSource;
import ru.craftlogic.client.sound.Sound;

import javax.annotation.Nonnull;

import static ru.craftlogic.api.CraftAPI.MOD_ID;
import static ru.craftlogic.api.CraftAPI.wrapWithActiveModId;

public class CraftSounds {
    public static SoundEvent WOODPECKER_CHIRP, WOODPECKER_PECK;
    public static SoundEvent FURNACE_VENT_OPEN, FURNACE_VENT_CLOSE, FURNACE_HOT_LOOP;
    public static SoundEvent BALANCE_ADD, BALANCE_SUBTRACT;
    public static SoundEvent COUNTDOWN_TICK;
    public static SoundEvent CART_PLACE;
    public static SoundEvent CART_LOADING;
    public static SoundEvent HEAL, HINT, WARNING, LEVEL_UP, SPELL, SLOW, HIDE, KICK, BAN, TOME, OPENING_FAILED, SQUASH, ERROR;
    public static SoundEvent CHICK, CHIRP, COAST_BIRD, CRITTER, FLIT, SQUEAK;

    static void init(Side side) {
        WOODPECKER_CHIRP = registerSound("entity.woodpecker.chirp");
        WOODPECKER_PECK = registerSound("entity.woodpecker.peck");
        FURNACE_VENT_OPEN = registerSound("furnace.vent.open");
        FURNACE_VENT_CLOSE = registerSound("furnace.vent.close");
        FURNACE_HOT_LOOP = registerSound("furnace.hot.loop");
        BALANCE_ADD = registerSound("balance.add");
        BALANCE_SUBTRACT = registerSound("balance.subtract");
        COUNTDOWN_TICK = registerSound("countdown.tick");
        CART_PLACE = registerSound("cart.place");
        CART_LOADING = registerSound("cart.loading");
        HEAL = registerSound("heal");
        HINT = registerSound("hint");
        WARNING = registerSound("warning");
        LEVEL_UP = registerSound("level_up");
        SPELL = registerSound("spell");
        SLOW = registerSound("slow");
        HIDE = registerSound("hide");
        KICK = registerSound("kick");
        BAN = registerSound("ban");
        TOME = registerSound("tome");
        OPENING_FAILED = registerSound("opening_failed");
        SQUASH = registerSound("squash");
        ERROR = registerSound("error");
        CHICK = registerSound("wild.chick");
        CHIRP = registerSound("wild.chirp");
        CRITTER = registerSound("wild.critter");
        FLIT = registerSound("wild.flit");
        SQUEAK = registerSound("wild.squeak");
    }

    public static SoundEvent registerSound(@Nonnull String name) {
        ResourceLocation id = wrapWithActiveModId(name, MOD_ID);
        SoundEvent soundEvent = new SoundEvent(id).setRegistryName(id);
        GameRegistry.findRegistry(SoundEvent.class).register(soundEvent);
        return soundEvent;
    }

    @SideOnly(Side.CLIENT)
    public static void playSound(SoundSource source, SoundEvent sound) {
        playSound(source, sound, SoundCategory.BLOCKS);
    }

    @SideOnly(Side.CLIENT)
    public static void playSound(SoundSource source, SoundEvent sound, SoundCategory category) {
        SoundHandler soundHandler = getSoundHandler();
        soundHandler.playSound(new Sound(source, sound, category));
    }

    @SideOnly(Side.CLIENT)
    private static SoundHandler getSoundHandler() {
        return Minecraft.getMinecraft().getSoundHandler();
    }
}
