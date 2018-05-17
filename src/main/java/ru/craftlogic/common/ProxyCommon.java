package ru.craftlogic.common;

import ru.craftlogic.common.entity.EntityThrownItem;

import static ru.craftlogic.CraftLogic.registerEntity;

public class ProxyCommon {
    public void preInit() {}

    public void init() {}

    public void postInit() {
        registerEntity(EntityThrownItem.class, "thrown_item", 64, 10, true);
    }
}
