package ru.craftlogic.api.economy;

import ru.craftlogic.api.text.Text;
import ru.craftlogic.api.world.OfflinePlayer;

import java.util.UUID;

public interface EconomyManager {
    boolean isEnabled();
    float getBalance(UUID id);
    void setBalance(UUID id, float balance);

    default float getBalance(OfflinePlayer player) {
        return this.getBalance(player.getId());
    }
    default void setBalance(OfflinePlayer player, float balance) {
        this.setBalance(player.getId(), balance);
    }

    default void give(UUID id, float amount) {
        float balance = getBalance(id);
        setBalance(id, balance + amount);
    }
    default float take(UUID id, float amount) {
        float balance = getBalance(id);
        amount = Math.min(balance, amount);
        setBalance(id, balance - amount);
        return amount;
    }

    default void give(OfflinePlayer player, float amount) {
        give(player.getId(), amount);
    }
    default float take(OfflinePlayer player, float amount) {
        return take(player.getId(), amount);
    }

    default float roundUpToFormat(float amount) {
        return amount;
    }
    default Text<?, ?> format(float amount) {
        return Text.string(String.valueOf(amount));
    }
}
