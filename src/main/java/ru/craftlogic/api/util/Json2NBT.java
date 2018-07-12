package ru.craftlogic.api.util;

import com.google.gson.*;
import net.minecraft.nbt.*;

import java.util.Map;

public class Json2NBT {
    private static final Gson GSON = new Gson();

    public static NBTBase jsonToNbt(String str) {
        return jsonToNbt(GSON.fromJson(str, JsonElement.class));
    }

    public static NBTBase jsonToNbt(JsonElement element) {
        if (element.isJsonArray()) {
            NBTTagList list = new NBTTagList();
            JsonArray array = element.getAsJsonArray();
            for (JsonElement e : array) {
                list.appendTag(jsonToNbt(e));
            }
            return list;
        } else if (element.isJsonObject()) {
            NBTTagCompound compound = new NBTTagCompound();
            JsonObject object = element.getAsJsonObject();
            for (Map.Entry<String, JsonElement> entry : object.entrySet()) {
                compound.setTag(entry.getKey(), jsonToNbt(entry.getValue()));
            }
            return compound;
        } else if (element.isJsonPrimitive()) {
            JsonPrimitive primitive = element.getAsJsonPrimitive();
            if (primitive.isBoolean()) {
                return new NBTTagByte((byte) (primitive.getAsBoolean() ? 1 : 0));
            } else if (primitive.isString()) {
                return new NBTTagString(primitive.getAsString());
            } else if (primitive.isNumber()) {
                Number number = primitive.getAsNumber();
                if (number instanceof Float) {
                    return new NBTTagFloat(number.floatValue());
                } else if (number instanceof Integer) {
                    return new NBTTagInt(number.intValue());
                } else if (number instanceof Byte) {
                    return new NBTTagByte(number.byteValue());
                } else if (number instanceof Short) {
                    return new NBTTagShort(number.shortValue());
                } else if (number instanceof Long) {
                    return new NBTTagLong(number.longValue());
                } else {
                    return new NBTTagDouble(number.doubleValue());
                }
            }
        }
        throw new IllegalArgumentException("Unsupported JSON type!");
    }

    public static JsonElement nbtToJson(NBTBase nbt) {
        if (nbt instanceof NBTTagList) {
            JsonArray array = new JsonArray();
            NBTTagList list = (NBTTagList) nbt.copy();
            for (int i = list.tagCount() - 1; i >= 0; i--) {
                array.add(nbtToJson(list.removeTag(i)));
            }
            return array;
        } else if (nbt instanceof NBTTagCompound) {
            JsonObject object = new JsonObject();
            NBTTagCompound compound = (NBTTagCompound) nbt.copy();
            for (String key : compound.getKeySet()) {
                object.add(key, nbtToJson(compound.getTag(key)));
            }
            return object;
        } else if (nbt instanceof NBTTagByteArray) {
            JsonArray array = new JsonArray();
            for (byte b : ((NBTTagByteArray) nbt).getByteArray()) {
                array.add(new JsonPrimitive(b));
            }
        } else if (nbt instanceof NBTTagIntArray) {
            JsonArray array = new JsonArray();
            for (int i : ((NBTTagIntArray) nbt).getIntArray()) {
                array.add(new JsonPrimitive(i));
            }
        } else if (nbt instanceof NBTTagString) {
            return new JsonPrimitive(((NBTTagString) nbt).getString());
        } else if (nbt instanceof NBTTagInt) {
            return new JsonPrimitive(((NBTTagInt) nbt).getInt());
        } else if (nbt instanceof NBTTagLong) {
            return new JsonPrimitive(((NBTTagLong) nbt).getLong());
        } else if (nbt instanceof NBTTagShort) {
            return new JsonPrimitive(((NBTTagShort) nbt).getShort());
        } else if (nbt instanceof NBTTagByte) {
            return new JsonPrimitive(((NBTTagByte) nbt).getByte());
        } else if (nbt instanceof NBTTagFloat) {
            return new JsonPrimitive(((NBTTagFloat) nbt).getFloat());
        } else if (nbt instanceof NBTTagDouble) {
            return new JsonPrimitive(((NBTTagDouble) nbt).getDouble());
        }
        throw new IllegalArgumentException("Unsupported NBT type!");
    }
}
