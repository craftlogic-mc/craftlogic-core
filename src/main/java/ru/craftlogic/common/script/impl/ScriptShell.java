package ru.craftlogic.common.script.impl;

import ru.craftlogic.api.world.CommandSender;

public abstract class ScriptShell extends ScriptBase {
    private CommandSender getSender() {
        return ((CommandSender) getBinding().getVariable("me"));
    }

    @Override
    public void print(Object value) {
        getSender().sendMessage("[S>]: %s", value);
    }

    @Override
    public String toString() {
        return getClass().getName();
    }
}
