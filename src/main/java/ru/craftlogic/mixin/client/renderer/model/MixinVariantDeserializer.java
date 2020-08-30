package ru.craftlogic.mixin.client.renderer.model;

import com.google.gson.JsonDeserializationContext;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParseException;
import net.minecraft.client.renderer.block.model.ModelRotation;
import net.minecraft.client.renderer.block.model.Variant;
import net.minecraft.util.JsonUtils;
import net.minecraft.util.ResourceLocation;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Overwrite;
import org.spongepowered.asm.mixin.Shadow;

import java.lang.reflect.Type;

@Mixin(Variant.Deserializer.class)
public abstract class MixinVariantDeserializer {

    @Shadow protected abstract String getStringModel(JsonObject obj);
    @Shadow protected abstract ModelRotation parseModelRotation(JsonObject obj);
    @Shadow protected abstract boolean parseUvLock(JsonObject obj);
    @Shadow protected abstract int parseWeight(JsonObject obj);

    /**
     * @author Radviger
     * @reason Item models for blocks
     */
    @Overwrite
    public Variant deserialize(JsonElement e, Type ty, JsonDeserializationContext ctx) throws JsonParseException {
        JsonObject obj = e.getAsJsonObject();
        String model = getStringModel(obj);
        ModelRotation rotation = parseModelRotation(obj);
        boolean lockUV = parseUvLock(obj);
        boolean item = JsonUtils.getBoolean(obj, "item", false);
        int weight = parseWeight(obj);
        return new Variant(getResourceLocationBlock(model, item), rotation, lockUV, weight);
    }

    private ResourceLocation getResourceLocationBlock(String model, boolean item) {
        ResourceLocation location = new ResourceLocation(model);
        location = new ResourceLocation(location.getNamespace(), (item ? "item/" : "block/") + location.getPath());
        return location;
    }
}
