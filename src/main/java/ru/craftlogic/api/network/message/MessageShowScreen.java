package ru.craftlogic.api.network.message;

import net.minecraft.entity.Entity;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.tileentity.TileEntity;
import net.minecraftforge.fml.common.network.simpleimpl.MessageContext;
import ru.craftlogic.CraftLogic;
import ru.craftlogic.api.block.holders.ScreenHolder;
import ru.craftlogic.api.network.AdvancedBuffer;
import ru.craftlogic.api.network.AdvancedMessage;
import ru.craftlogic.api.network.AdvancedMessageHandler;
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

    enum HolderType {
        TILE,
        ENTITY
    }

    public static class Handler extends AdvancedMessageHandler<MessageShowScreen, AdvancedMessage> {
        @Override
        protected AdvancedMessage handle(MessageShowScreen message, MessageContext context) {
            EntityPlayer player = getPlayer(context);
            switch (message.type) {
                case TILE: {
                    syncTask(context, () -> {
                        ScreenHolder screenHolder = message.location.getTileEntity(ScreenHolder.class);
                        CraftLogic.showScreen(screenHolder, player, message.extraData);
                        player.openContainer.windowId = message.windowId;
                    });
                    break;
                }
                case ENTITY: {
                    Entity entity = player.world.getEntityByID(message.entityId);
                    if (entity instanceof ScreenHolder) {
                        syncTask(context, () -> {
                            CraftLogic.showScreen((ScreenHolder) entity, player, message.extraData);
                            player.openContainer.windowId = message.windowId;
                        });
                    }
                    break;
                }
            }
            return null;
        }
    }
}
