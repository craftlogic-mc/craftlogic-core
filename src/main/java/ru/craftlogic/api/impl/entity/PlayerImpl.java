package ru.craftlogic.api.impl.entity;

import com.mojang.authlib.GameProfile;
import net.minecraft.entity.player.EntityPlayerMP;
import ru.craftlogic.api.server.Server;
import ru.craftlogic.api.world.Player;

import java.lang.ref.WeakReference;

public class PlayerImpl extends Player {
    public WeakReference<EntityPlayerMP> entity;

    public PlayerImpl(Server server, GameProfile profile, EntityPlayerMP entity) {
        super(server, profile);
        this.entity = new WeakReference<>(entity);
    }

    @Override
    public EntityPlayerMP getEntity() {
        return this.entity.get();
    }
}
