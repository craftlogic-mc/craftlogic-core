package ru.craftlogic.common.script.impl;

import groovy.lang.Script;
import net.minecraft.block.Block;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.SoundEvent;

public abstract class ScriptBase extends Script {
    protected ScriptContainer container;

    void setContainer(ScriptContainer container) {
        this.container = container;
    }

    @Override
    public void println() {
        this.print("\n");
    }

    @Override
    public void println(Object value) {
        this.print(value + "\n");
    }

    @Override
    public void printf(String format, Object value) {
        this.print(String.format(format, value));
    }

    @Override
    public void printf(String format, Object[] values) {
        this.print(String.format(format, values));
    }

    protected ItemManager getItem() {
        return new ItemManager();
    }

    protected BlockManager getBlock() {
        return new BlockManager();
    }

    protected SoundManager getSound() {
        return new SoundManager();
    }

    private static class ItemManager {
        public Item getAt(String id) {
            return Item.REGISTRY.getObject(new ResourceLocation(id));
        }

        public ItemStack getAt(Block block, int amount) {
            return new ItemStack(block, amount);
        }

        public ItemStack getAt(Item item, int amount) {
            return new ItemStack(item, amount);
        }

        public ItemStack getAt(String id, int amount) {
            return new ItemStack(getAt(id), amount);
        }

        public ItemStack getAt(Block block, int amount, int metadata) {
            return new ItemStack(block, amount, metadata);
        }

        public ItemStack getAt(Item item, int amount, int metadata) {
            return new ItemStack(item, amount, metadata);
        }

        public ItemStack getAt(String id, int amount, int metadata) {
            return new ItemStack(getAt(id), amount, metadata);
        }
    }

    private static class BlockManager {
        public Block getAt(String id) {
            return Block.REGISTRY.getObject(new ResourceLocation(id));
        }
    }

    private static class SoundManager {
        public SoundEvent getAt(String id) {
            return SoundEvent.REGISTRY.getObject(new ResourceLocation(id));
        }
    }
}
