package ru.craftlogic.client.screen;

import com.mojang.authlib.GameProfile;
import net.minecraft.client.entity.EntityOtherPlayerMP;
import net.minecraft.client.gui.inventory.GuiInventory;
import net.minecraft.client.network.NetHandlerPlayClient;
import net.minecraft.client.network.NetworkPlayerInfo;
import net.minecraft.entity.EntityLivingBase;
import ru.craftlogic.api.screen.Screen;
import ru.craftlogic.api.world.Location;

import javax.annotation.Nullable;

public class ScreenPlayerInfo extends Screen {
    private final GameProfile profile;
    private final long firstPlayed, lastPlayed, timePlayed;
    private final boolean allowEdit;
    private final EntityLivingBase entity;
    private final Location lastLocation, bedLocation;
    private float oldMouseX, oldMouseY;

    public ScreenPlayerInfo(GameProfile profile, long firstPlayed, long lastPlayed, long timePlayed,
                            boolean allowEdit, Location lastLocation, @Nullable Location bedLocation) {
        this.profile = profile;
        this.firstPlayed = firstPlayed;
        this.lastPlayed = lastPlayed;
        this.timePlayed = timePlayed;
        this.allowEdit = allowEdit;
        this.entity = new EntityOtherPlayerMP(getClient().world, profile);
        this.lastLocation = lastLocation;
        this.bedLocation = bedLocation;
    }

    @Nullable
    protected NetworkPlayerInfo getPlayerNetworkInfo(GameProfile profile) {
        NetHandlerPlayClient connection = getClient().getConnection();
        return connection != null ? connection.getPlayerInfo(profile.getId()) : null;
    }

    protected boolean isPlayerOnline(GameProfile profile) {
        return getPlayerNetworkInfo(profile) != null;
    }

    @Override
    protected void init() {

    }

    @Override
    public void drawBackground(int mouseX, int mouseY, float deltaTime) {
        bindTexture(BLANK_TEXTURE);
        int centerX = width / 2;
        int centerY = height / 2;
        int x = centerX - 176 / 2;
        int y = centerY - 166 / 2;
        drawTexturedRect(x, y, 176.0, 166.0);
        GuiInventory.drawEntityOnScreen(x + 30, y + 60, 40, (float)(mouseX + 10) - oldMouseX, (float)(mouseY + 10 - 9) - oldMouseY, entity);
    }

    @Override
    public void drawForeground(int mouseX, int mouseY, float deltaTime) {
        int centerX = width / 2;
        int centerY = height / 2;
        int x = centerX - 176 / 2;
        int y = centerY - 166 / 2;
        oldMouseX = (float)mouseX;
        oldMouseY = (float)mouseY;
    }
}
