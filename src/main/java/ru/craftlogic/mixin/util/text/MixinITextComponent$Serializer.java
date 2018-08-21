package ru.craftlogic.mixin.util.text;

import com.google.gson.*;
import net.minecraft.util.text.ITextComponent;
import net.minecraft.util.text.Style;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;
import ru.craftlogic.util.text.TextComponentDate;

import java.lang.reflect.Type;

@Mixin(ITextComponent.Serializer.class)
public class MixinITextComponent$Serializer {
    @Inject(method = "deserialize", at = @At("HEAD"), cancellable = true)
    public void onDeserialize(JsonElement element, Type type, JsonDeserializationContext context, CallbackInfoReturnable<ITextComponent> info) {
        if (element.isJsonObject()) {
            JsonObject obj = element.getAsJsonObject();
            if (obj.has("date")) {
                long date = obj.get("date").getAsLong();
                String format = obj.get("format").getAsString();
                ITextComponent component = new TextComponentDate(date, format);

                if (obj.has("extra")) {
                    JsonArray extra = obj.getAsJsonArray("extra");
                    if (extra.size() <= 0) {
                        throw new JsonParseException("Unexpected empty array of components");
                    }

                    for (JsonElement e : extra) {
                        component.appendSibling(this.deserialize(e, type, context));
                    }
                }

                component.setStyle(context.deserialize(element, Style.class));
                info.setReturnValue(component);
            }
        }
    }

    @Inject(method = "serialize", at = @At("HEAD"), cancellable = true)
    public void onSerialize(ITextComponent component, Type type, JsonSerializationContext context, CallbackInfoReturnable<JsonElement> info) {
        if (component instanceof TextComponentDate) {
            JsonObject obj = new JsonObject();
            if (!component.getStyle().isEmpty()) {
                this.serializeChatStyle(component.getStyle(), obj, context);
            }

            if (!component.getSiblings().isEmpty()) {
                JsonArray extra = new JsonArray();

                for (ITextComponent s : component.getSiblings()) {
                    extra.add(this.serialize(s, s.getClass(), context));
                }

                obj.add("extra", extra);
            }
            obj.addProperty("date", ((TextComponentDate) component).getDate());
            obj.addProperty("format", ((TextComponentDate) component).getFormat());

            info.setReturnValue(obj);
        }
    }

    @Shadow
    public ITextComponent deserialize(JsonElement element, Type type, JsonDeserializationContext context) {
        return null;
    }

    @Shadow
    public JsonElement serialize(ITextComponent component, Type type, JsonSerializationContext context) {
        return null;
    }

    @Shadow
    private void serializeChatStyle(Style style, JsonObject obj, JsonSerializationContext context) {}
}
