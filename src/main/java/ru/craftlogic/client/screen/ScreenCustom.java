package ru.craftlogic.client.screen;

import com.google.gson.JsonObject;
import groovy.lang.Binding;
import groovy.lang.Closure;
import groovy.lang.GroovyShell;
import net.minecraft.client.Minecraft;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.util.ResourceLocation;
import org.codehaus.groovy.control.CompilerConfiguration;
import ru.craftlogic.api.block.Updatable;
import ru.craftlogic.api.screen.Element;
import ru.craftlogic.api.screen.Elements;
import ru.craftlogic.api.screen.Screen;
import ru.craftlogic.common.script.ScriptManager;
import ru.craftlogic.common.script.impl.ScriptContainerScreen;
import ru.craftlogic.common.script.impl.ScriptScreen;

import java.util.Map;

import static ru.craftlogic.api.CraftAPI.MOD_ID;

public final class ScreenCustom extends Screen implements Updatable {
    private ScriptContainerScreen scriptContainer;

    public ScreenCustom(String id, JsonObject info, String raw, String args) {
        CompilerConfiguration config = new CompilerConfiguration();
        config.setScriptBaseClass(ScriptScreen.class.getName());
        Binding binding = new Binding();
        Minecraft client = Minecraft.getMinecraft();
        binding.setProperty("$client", client);
        binding.setProperty("$viewer", client.player);
        binding.setProperty("$args", args);
        GroovyShell compiler = ScriptManager.makeShell(binding, config);
        ScriptScreen script = (ScriptScreen) compiler.parse(raw, id + ".gs");
        this.scriptContainer = new ScriptContainerScreen(id, info, script);
        this.scriptContainer.run();
    }

    public <E extends Element> E addElement(Map<String, Object> args, String type) {
        ResourceLocation id = type.contains(":") ? new ResourceLocation(type) : new ResourceLocation(MOD_ID, type);
        return this.addElement(args, id);
    }

    public <E extends Element> E addElement(Map<String, Object> args, ResourceLocation type) {
        E element = Elements.get(type, this, args);
        this.addElement(element);
        return element;
    }

    @Override
    public void onGuiClosed() {
        callDelegate(this.scriptContainer.closed);
    }

    @Override
    protected void init() {
        callDelegate(this.scriptContainer.init);
    }

    @Override
    public void update() {
        callDelegate(this.scriptContainer.tick);
    }

    @Override
    public void drawBackground(int mouseX, int mouseY, float partialTicks) {
        callDelegate(this.scriptContainer.drawBackground, mouseX, mouseY, partialTicks);
    }

    @Override
    public void drawForeground(int mouseX, int mouseY, float partialTicks) {
        callDelegate(this.scriptContainer.drawForeground, mouseX, mouseY, partialTicks);
    }

    private <R> R callDelegate(Closure<R> target, Object... args) {
        if (target != null) {
            target.setDelegate(this);
            R result = target.call(args);
            target.setDelegate(null);
            return result;
        }
        return null;
    }

    public NBTTagCompound handlePayload(String channel, NBTTagCompound data) {
        return this.scriptContainer.handlePayload(channel, data);
    }
}
