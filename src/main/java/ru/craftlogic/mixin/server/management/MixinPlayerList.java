package ru.craftlogic.mixin.server.management;

import com.mojang.authlib.GameProfile;
import io.netty.buffer.Unpooled;
import net.minecraft.entity.Entity;
import net.minecraft.entity.player.EntityPlayerMP;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.network.NetHandlerPlayServer;
import net.minecraft.network.NetworkManager;
import net.minecraft.network.PacketBuffer;
import net.minecraft.network.play.server.*;
import net.minecraft.potion.PotionEffect;
import net.minecraft.scoreboard.ServerScoreboard;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.management.*;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.text.ITextComponent;
import net.minecraft.util.text.TextComponentTranslation;
import net.minecraft.util.text.TextFormatting;
import net.minecraft.world.World;
import net.minecraft.world.WorldServer;
import net.minecraft.world.chunk.storage.AnvilChunkLoader;
import net.minecraft.world.storage.IPlayerFileData;
import net.minecraft.world.storage.WorldInfo;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.fml.common.FMLCommonHandler;
import org.apache.logging.log4j.Logger;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Overwrite;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Redirect;
import ru.craftlogic.api.entity.Player;
import ru.craftlogic.api.event.player.PlayerLeftMessageEvent;
import ru.craftlogic.api.server.AdvancedPlayerList;

import javax.annotation.Nullable;
import java.io.File;
import java.util.UUID;

@Mixin(PlayerList.class)
public class MixinPlayerList implements AdvancedPlayerList {
    @Shadow @Final
    private static Logger LOGGER;

    @Shadow @Final
    private MinecraftServer mcServer;

    @Shadow
    private IPlayerFileData playerDataManager;

    @Redirect(method = "<init>", at = @At(value = "NEW", args = "class=net/minecraft/server/management/UserListBans"))
    public UserListBans createBans(File file) {
        File dir = new File("./settings/");

        if (!dir.exists()) {
            dir.mkdir();
        }
        return new UserListBans(new File(dir, file.getName()));
    }

    @Redirect(method = "<init>", at = @At(value = "NEW", args = "class=net/minecraft/server/management/UserListIPBans"))
    public UserListIPBans createIPBans(File file) {
        File dir = new File("./settings/");
        if (!dir.exists()) {
            dir.mkdir();
        }
        return new UserListIPBans(new File(dir, file.getName()));
    }

    @Redirect(method = "<init>", at = @At(value = "NEW", args = "class=net/minecraft/server/management/UserListOps"))
    public UserListOps createOPs(File file) {
        File dir = new File("./settings/");
        if (!dir.exists()) {
            dir.mkdir();
        }
        return new UserListOps(new File(dir, file.getName()));
    }

    @Redirect(method = "<init>", at = @At(value = "NEW", args = "class=net/minecraft/server/management/UserListWhitelist"))
    public UserListWhitelist createWhiteList(File file) {
        File dir = new File("./settings/");
        if (!dir.exists()) {
            dir.mkdir();
        }
        return new UserListWhitelist(new File(dir, file.getName()));
    }

    /**
     * @author Radviger
     * @reason Ability to edit/disable player join messages
     */
    @Overwrite(remap = false)
    public void initializeConnectionToPlayer(NetworkManager netManager, EntityPlayerMP player, NetHandlerPlayServer netHandler) {
        GameProfile profile = player.getGameProfile();
        PlayerProfileCache profileCache = this.mcServer.getPlayerProfileCache();
        GameProfile cachedProfile = profileCache.getProfileByUUID(profile.getId());
        String username = cachedProfile == null ? profile.getName() : cachedProfile.getName();
        profileCache.addEntry(profile);
        NBTTagCompound playerData = this.readPlayerDataFromFile(player);
        if (playerData == null || ((Player)player).getFirstPlayed() == 0) {
            ((Player)player).setFirstPlayed(System.currentTimeMillis());
        }
        player.setWorld(this.mcServer.getWorld(player.dimension));
        World playerWorld = this.mcServer.getWorld(player.dimension);
        if (playerWorld == null) {
            player.dimension = 0;
            playerWorld = this.mcServer.getWorld(0);
            BlockPos spawnPoint = playerWorld.provider.getRandomizedSpawnPoint();
            player.setPosition((double)spawnPoint.getX(), (double)spawnPoint.getY(), (double)spawnPoint.getZ());
        }

        player.setWorld(playerWorld);
        player.interactionManager.setWorld((WorldServer)player.world);
        String s1 = "local";
        if (netManager.getRemoteAddress() != null) {
            s1 = netManager.getRemoteAddress().toString();
        }

        LOGGER.info("{}[{}] logged in with entity id {} at ({}, {}, {})", player.getName(), s1, player.getEntityId(), player.posX, player.posY, player.posZ);
        WorldServer world = this.mcServer.getWorld(player.dimension);
        WorldInfo worldInfo = world.getWorldInfo();
        this.setPlayerGameTypeBasedOnOther(player, null, world);
        player.connection = netHandler;
        netHandler.sendPacket(new SPacketJoinGame(player.getEntityId(), player.interactionManager.getGameType(), worldInfo.isHardcoreModeEnabled(), world.provider.getDimension(), world.getDifficulty(), this.getMaxPlayers(), worldInfo.getTerrainType(), world.getGameRules().getBoolean("reducedDebugInfo")));
        netHandler.sendPacket(new SPacketCustomPayload("MC|Brand", (new PacketBuffer(Unpooled.buffer())).writeString(this.getServerInstance().getServerModName())));
        netHandler.sendPacket(new SPacketServerDifficulty(worldInfo.getDifficulty(), worldInfo.isDifficultyLocked()));
        netHandler.sendPacket(new SPacketPlayerAbilities(player.capabilities));
        netHandler.sendPacket(new SPacketHeldItemChange(player.inventory.currentItem));
        this.updatePermissionLevel(player);
        player.getStatFile().markAllDirty();
        player.getRecipeBook().init(player);
        this.sendScoreboard((ServerScoreboard)world.getScoreboard(), player);
        this.mcServer.refreshStatusNextTick();

        TextComponentTranslation message;
        if (player.getName().equalsIgnoreCase(username)) {
            message = new TextComponentTranslation("multiplayer.player.joined", player.getDisplayName());
        } else {
            message = new TextComponentTranslation("multiplayer.player.joined.renamed", player.getDisplayName(), username);
        }
        message.getStyle().setColor(TextFormatting.YELLOW);

        PlayerLeftMessageEvent event = new PlayerLeftMessageEvent(player, message);

        if (!MinecraftForge.EVENT_BUS.post(event)) {
            this.sendMessage(message);
        }

        this.playerLoggedIn(player);
        netHandler.setPlayerLocation(player.posX, player.posY, player.posZ, player.rotationYaw, player.rotationPitch);
        this.updateTimeAndWeatherForPlayer(player, world);
        if (!this.mcServer.getResourcePackUrl().isEmpty()) {
            player.loadResourcePack(this.mcServer.getResourcePackUrl(), this.mcServer.getResourcePackHash());
        }

        for (PotionEffect potioneffect : player.getActivePotionEffects()) {
            netHandler.sendPacket(new SPacketEntityEffect(player.getEntityId(), potioneffect));
        }

        if (playerData != null && playerData.hasKey("RootVehicle", 10)) {
            NBTTagCompound rootVehicle = playerData.getCompoundTag("RootVehicle");
            Entity entity1 = AnvilChunkLoader.readWorldEntity(rootVehicle.getCompoundTag("Entity"), world, true);
            if (entity1 != null) {
                UUID uuid = rootVehicle.getUniqueId("Attach");
                if (entity1.getUniqueID().equals(uuid)) {
                    player.startRiding(entity1, true);
                } else {
                    for (Entity entity2 : entity1.getRecursivePassengers()) {
                        if (entity2.getUniqueID().equals(uuid)) {
                            player.startRiding(entity2, true);
                            break;
                        }
                    }
                }

                if (!player.isRiding()) {
                    LOGGER.warn("Couldn't reattach entity to player");
                    world.removeEntityDangerously(entity1);

                    for (Entity entity2 : entity1.getRecursivePassengers()) {
                        world.removeEntityDangerously(entity2);
                    }
                }
            }
        }

        player.addSelfToInternalCraftingInventory();
        FMLCommonHandler.instance().firePlayerLoggedIn(player);
    }

    @Override
    public IPlayerFileData getDataManager() {
        return this.playerDataManager;
    }

    @Shadow
    public void sendMessage(ITextComponent message) { }

    @Shadow
    public void playerLoggedIn(EntityPlayerMP player) { }

    @Shadow
    private void setPlayerGameTypeBasedOnOther(EntityPlayerMP oldPlayer, EntityPlayerMP newPlayer, World world) { }

    @Shadow
    @Nullable
    public NBTTagCompound readPlayerDataFromFile(EntityPlayerMP player) { return null; }

    @Shadow
    public void updateTimeAndWeatherForPlayer(EntityPlayerMP player, WorldServer world) { }

    @Shadow
    public void updatePermissionLevel(EntityPlayerMP player) { }

    @Shadow
    protected void sendScoreboard(ServerScoreboard scoreboard, EntityPlayerMP player) { }

    @Shadow
    public MinecraftServer getServerInstance() {
        return this.mcServer;
    }

    @Shadow
    public int getMaxPlayers() { return 0; }
}
