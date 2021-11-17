package ru.craftlogic.mixin.server.management;

import com.mojang.authlib.GameProfile;
import io.netty.buffer.Unpooled;
import net.minecraft.entity.Entity;
import net.minecraft.entity.player.EntityPlayer;
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
import org.spongepowered.asm.mixin.injection.Slice;
import ru.craftlogic.api.entity.AdvancedPlayer;
import ru.craftlogic.api.event.player.PlayerLeftMessageEvent;
import ru.craftlogic.api.server.AdvancedPlayerFileData;
import ru.craftlogic.api.server.AdvancedPlayerList;

import javax.annotation.Nullable;
import java.io.File;
import java.util.UUID;

@Mixin(PlayerList.class)
public abstract class MixinPlayerList implements AdvancedPlayerList {
    @Shadow
    @Final
    private static Logger LOGGER;

    @Shadow
    @Final
    private MinecraftServer server;

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
        PlayerProfileCache profileCache = this.server.getPlayerProfileCache();
        GameProfile cachedProfile = profileCache.getProfileByUUID(profile.getId());
        String username = cachedProfile == null ? profile.getName() : cachedProfile.getName();
        profileCache.addEntry(profile);
        NBTTagCompound playerData = this.readPlayerDataFromFile(player);
        if (playerData == null) {
            AdvancedPlayer ap = (AdvancedPlayer) player;
            if (ap.getFirstPlayed() == 0) {
                ap.setFirstPlayed(System.currentTimeMillis());
            }
        }
        player.setWorld(this.server.getWorld(player.dimension));
        World playerWorld = this.server.getWorld(player.dimension);
        if (playerWorld == null) {
            player.dimension = 0;
            playerWorld = this.server.getWorld(0);
            BlockPos spawnPoint = playerWorld.provider.getRandomizedSpawnPoint();
            player.setPositionAndUpdate((double) spawnPoint.getX(), (double) spawnPoint.getY(), (double) spawnPoint.getZ());
        }

        player.setWorld(playerWorld);
        player.interactionManager.setWorld((WorldServer) player.world);
        String s1 = "local";
        if (netManager.getRemoteAddress() != null) {
            s1 = netManager.getRemoteAddress().toString();
        }

        LOGGER.info("{}[{}] logged in with entity id {} at ({}, {}, {})", player.getName(), s1, player.getEntityId(), player.posX, player.posY, player.posZ);
        WorldServer world = this.server.getWorld(player.dimension);
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
        this.sendScoreboard((ServerScoreboard) world.getScoreboard(), player);
        this.server.refreshStatusNextTick();

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
        if (!this.server.getResourcePackUrl().isEmpty()) {
            player.loadResourcePack(this.server.getResourcePackUrl(), this.server.getResourcePackHash());
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
    public AdvancedPlayerFileData getDataManager() {
        return (AdvancedPlayerFileData) this.playerDataManager;
    }

    @Shadow
    public abstract void sendMessage(ITextComponent message);

    @Shadow
    public abstract void playerLoggedIn(EntityPlayerMP player);

    @Shadow
    protected abstract void setPlayerGameTypeBasedOnOther(EntityPlayerMP oldPlayer, EntityPlayerMP newPlayer, World world);

    @Shadow
    @Nullable
    public abstract NBTTagCompound readPlayerDataFromFile(EntityPlayerMP player);

    @Shadow
    public abstract void updateTimeAndWeatherForPlayer(EntityPlayerMP player, WorldServer world);

    @Shadow
    public abstract void updatePermissionLevel(EntityPlayerMP player);

    @Shadow
    protected abstract void sendScoreboard(ServerScoreboard scoreboard, EntityPlayerMP player);

    @Shadow
    public abstract MinecraftServer getServerInstance();

    @Shadow
    public abstract int getMaxPlayers();

    // Fixes MC-92916
    @Redirect(method = "transferEntityToWorld(Lnet/minecraft/entity/Entity;ILnet/minecraft/world/WorldServer;Lnet/minecraft/world/WorldServer;Lnet/minecraftforge/common/util/ITeleporter;)V",
        at = @At(value = "INVOKE", target = "Lnet/minecraft/world/WorldServer;updateEntityWithOptionalForce(Lnet/minecraft/entity/Entity;Z)V"),
        slice = @Slice(from = @At(value = "CONSTANT", args = "stringValue=moving"), to = @At(value = "CONSTANT", args = "stringValue=placing")))
    public void doPrepareLeaveDimension(WorldServer world, Entity entity, boolean forceUpdate) {
        if (entity instanceof EntityPlayer) {
            entity.lastTickPosX = entity.posX;
            entity.lastTickPosY = entity.posY;
            entity.lastTickPosZ = entity.posZ;
            entity.prevRotationYaw = entity.rotationYaw;
            entity.prevRotationPitch = entity.rotationPitch;
        } else {
            world.updateEntityWithOptionalForce(entity, forceUpdate);
        }
    }

    // Fixes MC-92916
    // This is needed for Forge
    @Redirect(method = "transferEntityToWorld(Lnet/minecraft/entity/Entity;ILnet/minecraft/world/WorldServer;Lnet/minecraft/world/WorldServer;Lnet/minecraftforge/common/util/ITeleporter;)V",
        at = @At(value = "INVOKE", target = "Lnet/minecraft/world/WorldServer;updateEntityWithOptionalForce(Lnet/minecraft/entity/Entity;Z)V", ordinal = 0),
        slice = @Slice(from = @At(value = "CONSTANT", args = "stringValue=placing"),
            to = @At(value = "INVOKE", target = "Lnet/minecraftforge/common/util/ITeleporter;placeEntity(Lnet/minecraft/world/World;Lnet/minecraft/entity/Entity;F)V", remap = false)))
    public void doPrepareLeaveDimensionForge(WorldServer world, Entity entity, boolean forceUpdate) {
        if (entity instanceof EntityPlayer) {
            entity.lastTickPosX = entity.posX;
            entity.lastTickPosY = entity.posY;
            entity.lastTickPosZ = entity.posZ;
            entity.prevRotationYaw = entity.rotationYaw;
            entity.prevRotationPitch = entity.rotationPitch;
        } else {
            world.updateEntityWithOptionalForce(entity, forceUpdate);
        }
    }
}
