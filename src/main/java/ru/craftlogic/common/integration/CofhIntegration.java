package ru.craftlogic.common.integration;

import net.minecraft.block.Block;
import net.minecraft.init.Blocks;
import ru.craftlogic.api.integration.ModIntegration;

import java.lang.reflect.Field;

public class CofhIntegration implements ModIntegration {
    @Override
    public String getModId() {
        return "cofhcore";
    }

    @Override
    public void preInit() {}

    @Override
    public void init() {}

    @Override
    public void postInit() {
        try {
            Class<?> bh = Class.forName("cofh.core.util.helpers.BlockHelper");
            Field rt = bh.getDeclaredField("rotateType");
            byte[] types = (byte[]) rt.get(null);
            types[Block.getIdFromBlock(Blocks.CHEST)] = 0;
            types[Block.getIdFromBlock(Blocks.TRAPPED_CHEST)] = 0;
        } catch (ReflectiveOperationException e) {}
    }
}
