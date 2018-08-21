package ru.craftlogic.network.message;

import net.minecraft.entity.Entity;
import net.minecraft.tileentity.TileEntity;
import ru.craftlogic.api.block.holders.ScreenHolder;
import ru.craftlogic.api.network.AdvancedBuffer;
import ru.craftlogic.api.network.AdvancedMessage;
import ru.craftlogic.api.world.Locatable;
import ru.craftlogic.api.world.Location;
import ru.craftlogic.util.ReflectiveUsage;

public class MessageShowScreen extends AdvancedMessage {
    private HolderType type;
    private Location location;
    private int windowId;
    private int extraData;
    private int entityId;

    @Deprecated
    @ReflectiveUsage
    public MessageShowScreen() {}

    public MessageShowScreen(ScreenHolder screenHolder, int windowId, int extraData) {
        this.windowId = windowId;
        this.extraData = extraData;
        if (screenHolder instanceof Entity) {
            this.entityId = ((Entity) screenHolder).getEntityId();
            this.type = HolderType.ENTITY;
        } else if (screenHolder instanceof TileEntity && screenHolder instanceof Locatable) {
            this.location = ((Locatable) screenHolder).getLocation();
            this.type = HolderType.TILE;
        }
    }

    public HolderType getType() {
        return type;
    }

    public Location getLocation() {
        return location;
    }

    public int getWindowId() {
        return windowId;
    }

    public int getEntityId() {
        return entityId;
    }

    public int getExtraData() {
        return extraData;
    }

    @Override
    protected void read(AdvancedBuffer buf) {
        this.type = buf.readEnumValue(HolderType.class);
        this.windowId = buf.readInt();
        switch (this.type) {
            case TILE:
                this.location = buf.readBlockLocation();
                break;
            case ENTITY:
                this.entityId = buf.readVarInt();
                break;
        }
        this.extraData = buf.readInt();
    }

    @Override
    protected void write(AdvancedBuffer buf) {
        buf.writeEnumValue(this.type);
        buf.writeInt(this.windowId);
        switch (this.type) {
            case TILE:
                buf.writeBlockLocation(this.location);
                break;
            case ENTITY:
                buf.writeVarInt(this.entityId);
                break;
        }
        buf.writeInt(this.extraData);
    }

    public enum HolderType {
        TILE,
        ENTITY
    }
}
