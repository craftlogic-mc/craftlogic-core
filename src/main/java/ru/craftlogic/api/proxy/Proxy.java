package ru.craftlogic.api.proxy;

import net.minecraftforge.fml.common.FMLCommonHandler;

import java.util.function.Supplier;

public abstract class Proxy {
    public static <P extends Proxy> P factory(Supplier<? extends P> client, Supplier<? extends P> server) {
        if (FMLCommonHandler.instance().getSide().isClient()) {
            return client.get();
        } else {
            return server.get();
        }
    }
}
