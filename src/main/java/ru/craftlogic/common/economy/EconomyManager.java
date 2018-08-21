package ru.craftlogic.common.economy;

import com.google.gson.JsonObject;
import com.mojang.authlib.GameProfile;
import it.unimi.dsi.fastutil.objects.Object2FloatMap.Entry;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.util.JsonUtils;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;
import net.minecraftforge.fml.common.gameevent.PlayerEvent;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import ru.craftlogic.api.CraftNetwork;
import ru.craftlogic.api.Server;
import ru.craftlogic.api.text.Text;
import ru.craftlogic.api.util.ConfigurableManager;
import ru.craftlogic.api.world.OfflinePlayer;
import ru.craftlogic.api.world.Player;
import ru.craftlogic.common.command.CommandManager;
import ru.craftlogic.network.message.MessageBalance;

import java.io.IOException;
import java.nio.file.Path;
import java.util.List;
import java.util.UUID;

public class EconomyManager extends ConfigurableManager {
    private static final Logger LOGGER = LogManager.getLogger("EconomyManager");

    private final AccountManager accountManager;
    private boolean enabled;
    private final String format = "%.2f";
    private String currency;

    public EconomyManager(Server server, Path settingsDirectory) {
        super(server, settingsDirectory.resolve("economy.json"), LOGGER);
        this.accountManager = new AccountManager(this, settingsDirectory.resolve("economy/accounts.json"), LOGGER);
    }

    public boolean isEnabled() {
        return enabled;
    }

    @Override
    public void registerCommands(CommandManager commandManager) {
        commandManager.registerCommandContainer(EconomyCommands.class);
    }

    @Override
    protected void load(JsonObject config) {
        this.enabled = JsonUtils.getBoolean(config, "enabled", false);
        this.currency = JsonUtils.getString(config, "currency");

        try {
            this.accountManager.load();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    @Override
    protected void save(JsonObject config) {
        config.addProperty("enabled", this.enabled);
        config.addProperty("currency", this.currency);

        try {
            this.accountManager.save(true);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public List<Entry<UUID>> getTop(int size) {
        return this.accountManager.getTop(size);
    }

    public float getBalance(OfflinePlayer player) {
        return this.getBalance(player.getId());
    }

    public float getBalance(UUID id) {
        return this.accountManager.getBalance(id);
    }

    public void setBalance(OfflinePlayer player, float balance) {
        this.setBalance(player.getId(), balance);
    }

    public void setBalance(UUID id, float balance) {
        this.accountManager.setBalance(id, balance);
        if (this.accountManager.isDirty()) {
            Player player = this.server.getPlayer(new GameProfile(id, null));
            if (player != null) {
                player.sendPacket(new MessageBalance(balance, this.currency, this.format));
            }
        }
    }

    public String getCurrency() {
        return this.currency;
    }

    public Text<?, ?> format(float amount) {
        return Text.string(String.format(this.format, amount)).appendText(this.currency);
    }

    public float roundUpToFormat(float amount) {
        String n = String.format(this.format, amount);
        return Float.parseFloat(n);
    }

    @SubscribeEvent
    public void onPlayerJoin(PlayerEvent.PlayerLoggedInEvent event) {
        EntityPlayer player = event.player;
        if (this.enabled && !player.world.isRemote) {
            float balance = this.getBalance(player.getGameProfile().getId());
            CraftNetwork.sendTo(player, new MessageBalance(balance, this.currency, this.format));
        }
    }
}
