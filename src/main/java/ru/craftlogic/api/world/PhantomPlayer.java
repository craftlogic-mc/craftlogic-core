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
        fake = new FakePlayer(world.unwrap(), profile);
    }

    public Player asOnline() {
        return server.getPlayerManager().getOnline(profile);
    }

    @Override
    public EntityPlayerMP getEntity() {
        if (isOnline()) {
            return asOnline().getEntity();
        }
        if (!loadedData) {
            loadData();
        }
        return fake;
    }

    private AdvancedPlayerFileData getFileData() {
        return server.getPlayerManager().getFileData();
    }

    @Override
    public boolean hasData(World world) {
        return isOnline() || getFileData().hasPlayerData(profile);
    }

    public boolean loadData() {
        if (loadedData) {
            return true;
        }
        NBTTagCompound data = getFileData().readPlayerData(fake);
        if (data != null) {
            loadedData = true;
            return true;
        }
        return false;
    }

    public boolean saveData() {
        if (loadedData) {
            getFileData().writePlayerData(fake);
        }
        return true;
    }
}
