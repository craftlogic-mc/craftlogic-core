package ru.craftlogic.api.entity;

import net.minecraft.util.ResourceLocation;
import ru.craftlogic.api.CraftAPI;
import ru.craftlogic.api.util.Nameable;

import static ru.craftlogic.api.CraftAPI.wrapWithModId;

public interface Chicken extends Bird, Tameable {
    ChickenVariant getVariant();

    enum ChickenVariant implements Nameable {
        ROOSTER("rooster"),
        BLACK("black_chicken"),
        WHITE("white_chicken"),
        GRAY("gray_chicken"),
        DARK_GRAY("dark_gray_chicken"),
        BROWN("brown_chicken");

        private final ResourceLocation texture;

        ChickenVariant(String texture) {
            ResourceLocation tx = wrapWithModId(texture, CraftAPI.MOD_ID);
            this.texture = new ResourceLocation(tx.getResourceDomain(), "textures/entity/chicken/" + tx.getResourcePath() + ".png");
        }

        public ResourceLocation getTexture() {
            return texture;
        }
    }
}
