package ru.craftlogic.common.script.impl;

import com.google.common.collect.ImmutableSet;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import net.minecraft.util.JsonUtils;
import net.minecraft.util.text.ITextComponent;
import net.minecraft.util.text.TextComponentString;
import net.minecraft.util.text.TextFormatting;

import java.util.HashSet;
import java.util.Set;

public class ScriptContainer implements Runnable {
    protected final String id;
    protected final String name;
    protected final Set<String> authors = new HashSet<>();
    protected final ScriptBase script;

    public ScriptContainer(String id, JsonObject info, ScriptBase script) {
        this.id = id;
        this.name = JsonUtils.getString(info, "name", id);
        if (info.has("authors")) {
            for (JsonElement e : info.get("authors").getAsJsonArray()) {
                this.authors.add(e.getAsString());
            }
        }
        this.script = script;
        script.setContainer(this);
    }

    public String getId() {
        return id;
    }

    public String getName() {
        return name;
    }

    public Set<String> getAuthors() {
        return ImmutableSet.copyOf(this.authors);
    }

    public ITextComponent getPrefix() {
        ITextComponent prefix;
        if (!this.id.equalsIgnoreCase(this.name)) {
            prefix = new TextComponentString("[" + this.name + " (" + this.id + ".gs)]");
        } else {
            prefix = new TextComponentString("[" + this.name + "]");
        }
        prefix.getStyle().setColor(TextFormatting.RED);
        return prefix;
    }

    @Override
    public void run() {
        this.script.run();
    }
}
