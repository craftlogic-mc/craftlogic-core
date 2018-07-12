package ru.craftlogic.common.entity;

import net.minecraft.util.ResourceLocation;
import ru.craftlogic.CraftLogic;
import ru.craftlogic.api.util.Nameable;

public enum ChickenVariant implements Nameable {
    ROOSTER("rooster"),
    BLACK("black_chicken"),
    WHITE("white_chicken"),
    GRAY("gray_chicken"),
    DARK_GRAY("darkgray_chicken"),
    BROWN("brown_chicken");

    private final ResourceLocation texture;

    ChickenVariant(String texture) {
        ResourceLocation tx = texture.contains(":") ? new ResourceLocation(texture) : new ResourceLocation(CraftLogic.MODID, texture);
        this.texture = new ResourceLocation(tx.getResourceDomain(), "textures/entity/chicken/" + tx.getResourcePath() + ".png");
    }

    public ResourceLocation getTexture() {
        return texture;
    }
}
