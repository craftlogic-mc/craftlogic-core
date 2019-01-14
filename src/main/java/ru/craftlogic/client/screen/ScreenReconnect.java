package ru.craftlogic.client.screen;

import net.minecraft.client.gui.GuiMainMenu;
import net.minecraft.client.gui.GuiMultiplayer;
import net.minecraft.client.multiplayer.ServerData;
import net.minecraft.util.text.ITextComponent;
import net.minecraft.util.text.TextComponentTranslation;
import net.minecraftforge.fml.client.FMLClientHandler;
import ru.craftlogic.api.CraftMessages;
import ru.craftlogic.api.block.Updatable;
import ru.craftlogic.api.screen.Screen;
import ru.craftlogic.api.screen.element.ElementButton;
import ru.craftlogic.api.text.Text;

public class ScreenReconnect extends Screen implements Updatable {
    private final String address;
    private final long endTime;
    private final ITextComponent reason;

    public ScreenReconnect(String address, int reconnect, ITextComponent reason) {
        this.address = address;
        this.endTime = System.currentTimeMillis() + reconnect * 1000L;
        this.reason = reason;
    }

    @Override
    protected void init() {
        int x = this.width / 2;
        int y = this.height / 2;
        this.addElement(new ElementButton(this, x - 100, y + 20, new TextComponentTranslation("gui.toTitle")).withHandler((mouseX, mouseY, button) -> {
            this.getClient().displayGuiScreen(new GuiMultiplayer(new GuiMainMenu()));
        }));
    }

    @Override
    public void drawBackground(int mouseX, int mouseY, float deltaTime) {
        this.drawDefaultBackground();
    }

    @Override
    public void drawForeground(int mouseX, int mouseY, float deltaTime) {
        int x = this.width / 2;
        int y = this.height / 2;
        long currentTime = System.currentTimeMillis();
        if (currentTime < this.endTime) {
            drawCenteredText(this.reason, x, y - 25, 0xFFFFFFFF);
            Text<?, ?> timeout = CraftMessages.parseDuration(this.endTime - currentTime);
            ITextComponent text = new TextComponentTranslation("gui.reconnect.in", timeout.build());
            drawCenteredText(text, x, y - 10, 0xFFFFFFFF);
        }
    }


    @Override
    public void update() {
        if (System.currentTimeMillis() >= this.endTime) {
            FMLClientHandler.instance().connectToServer(new GuiMultiplayer(new GuiMainMenu()), new ServerData("Unknown", this.address, false));
        }
    }
}
