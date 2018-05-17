package ru.craftlogic.common.entity;

import net.minecraft.util.ResourceLocation;
import ru.craftlogic.CraftLogic;
import ru.craftlogic.api.util.Nameable;

public enum ChickenVariant implements Nameable {
    ROOSTER("textures/entity/chicken/rooster.png"),
    BLACK("textures/entity/chicken/black_chicken.png"),
    WHITE("textures/entity/chicken/white_chicken.png"),
    GRAY("textures/entity/chicken/gray_chicken.png"),
    DARK_GRAY("textures/entity/chicken/darkgray_chicken.png"),
    BROWN("textures/entity/chicken/brown_chicken.png");

    private final ResourceLocation texture;

    ChickenVariant(String texture) {
        this.texture = texture.contains(":") ?
                new ResourceLocation(texture) : new ResourceLocation(CraftLogic.MODID, texture);
    }

    public ResourceLocation getTexture() {
        return texture;
    }
}
