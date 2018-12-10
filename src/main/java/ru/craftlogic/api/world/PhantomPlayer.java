package ru.craftlogic.api.world;

import com.mojang.authlib.GameProfile;
import net.minecraft.entity.player.EntityPlayerMP;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraftforge.common.util.FakePlayer;
import ru.craftlogic.api.server.AdvancedPlayerFileData;

public class PhantomPlayer extends Player {
    private final FakePlayer fake;
    private boolean loadedData;

    public PhantomPlayer(World world, GameProfile profile) {
        super(world.getServer(), profile);
        this.fake = new FakePlayer(world.unwrap(), profile);
    }

    public Player asOnline() {
        return this.server.getPlayerManager().getOnline(this.profile);
    }

    @Override
    public EntityPlayerMP getEntity() {
        if (isOnline()) {
            return asOnline().getEntity();
        }
        if (!this.loadedData) {
            this.loadData();
        }
        return this.fake;
    }

    private AdvancedPlayerFileData getFileData() {
        return this.server.getPlayerManager().getFileData();
    }

    @Override
    public boolean hasData(World world) {
        return isOnline() || getFileData().hasPlayerData(this.profile);
    }

    public boolean loadData() {
        if (this.loadedData) {
            return true;
        }
        NBTTagCompound data = getFileData().readPlayerData(this.fake);
        if (data != null) {
            this.loadedData = true;
            return true;
        }
        return false;
    }

    public boolean saveData() {
        if (this.loadedData) {
            getFileData().writePlayerData(this.fake);
        }
        return true;
    }
}
