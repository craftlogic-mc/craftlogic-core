package ru.craftlogic.api.network;

import net.minecraft.server.MinecraftServer;

public interface AdvancedNetHandlerPlayServer {
    void resetPosition();
    MinecraftServer getServer();
}
