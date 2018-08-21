package ru.craftlogic.network.message;

import com.google.gson.Gson;
import com.google.gson.JsonObject;
import ru.craftlogic.api.network.AdvancedBuffer;
import ru.craftlogic.api.network.AdvancedMessage;

public class MessageShowScriptScreen extends AdvancedMessage {
    private String id;
    private JsonObject info;
    private String script;
    private String args;

    public MessageShowScriptScreen() {}

    public MessageShowScriptScreen(String id, JsonObject info, String script, String args) {
        this.id = id;
        this.info = info;
        this.script = script;
        this.args = args;
    }

    public String getId() {
        return id;
    }

    public JsonObject getInfo() {
        return info;
    }

    public String getScript() {
        return script;
    }

    public String getArgs() {
        return args;
    }

    @Override
    protected void read(AdvancedBuffer buf) {
        this.id = buf.readString(Short.MAX_VALUE);
        this.info = new Gson().fromJson(buf.readString(Short.MAX_VALUE), JsonObject.class);
        this.script = buf.readString(Short.MAX_VALUE);
        this.args = buf.readString(Short.MAX_VALUE);
    }

    @Override
    protected void write(AdvancedBuffer buf) {
        buf.writeString(this.id);
        buf.writeString(new Gson().toJson(this.info));
        buf.writeString(this.script);
        buf.writeString(this.args);
    }
}
