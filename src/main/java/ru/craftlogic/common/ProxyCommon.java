package ru.craftlogic.common;

import net.minecraft.block.Block;
import net.minecraft.block.BlockSlab;
import net.minecraft.block.BlockSlab.EnumBlockHalf;
import net.minecraft.block.properties.IProperty;
import net.minecraft.block.state.IBlockState;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.entity.monster.EntityZombie;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.util.EntityDamageSource;
import net.minecraft.util.IThreadListener;
import net.minecraft.util.math.RayTraceResult;
import net.minecraft.util.math.Vec3d;
import net.minecraftforge.event.entity.living.LivingDeathEvent;
import net.minecraftforge.event.entity.living.LivingDestroyBlockEvent;
import net.minecraftforge.fml.client.FMLClientHandler;
import net.minecraftforge.fml.common.FMLCommonHandler;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;
import net.minecraftforge.fml.common.network.simpleimpl.MessageContext;
import net.minecraftforge.fml.relauncher.Side;
import ru.craftlogic.api.CraftAPI;
import ru.craftlogic.api.network.AdvancedMessage;
import ru.craftlogic.api.proxy.Proxy;
import ru.craftlogic.api.Server;
import ru.craftlogic.api.world.Location;
import ru.craftlogic.api.world.Player;
import ru.craftlogic.common.economy.EconomyManager;
import ru.craftlogic.common.script.ScriptManager;
import ru.craftlogic.common.script.impl.ScriptContainerFile;
import ru.craftlogic.network.message.*;

import java.util.Random;

import static ru.craftlogic.api.CraftNetwork.registerMessage;

public class ProxyCommon extends Proxy {
    public void preInit() {
        CraftAPI.init(FMLCommonHandler.instance().getSide());
    }

    public void init() {
        registerMessage(this::handleShowScreen, MessageShowScreen.class, Side.CLIENT);
        registerMessage(this::handleShowScriptScreen, MessageShowScriptScreen.class, Side.CLIENT);
        registerMessage(this::handleBalance, MessageBalance.class, Side.CLIENT);
        registerMessage(this::handleClearChat, MessageClearChat.class, Side.CLIENT);
        registerMessage(this::handleClientCustom, MessageCustom.class, Side.CLIENT);
        registerMessage(this::handleServerCustom, MessageCustom.class, Side.SERVER);
        registerMessage(this::handleToast, MessageToast.class, Side.CLIENT);
        registerMessage(this::handleCountdown, MessageCountdown.class, Side.CLIENT);
    }

    public void postInit() {

    }

    @SubscribeEvent
    public void onBlockBroken(LivingDestroyBlockEvent event) {
        IBlockState state = event.getState();
        Block block = state.getBlock();
        if (block instanceof BlockSlab && ((BlockSlab) block).isDouble()) {
            EntityLivingBase living = event.getEntityLiving();
            Vec3d eye = living.getPositionEyes(1F);
            Vec3d look = living.getLook(1F);
            double distance = 4;
            Vec3d end = eye.addVector(look.x * distance, look.y * distance, look.z * distance);
            RayTraceResult target = living.world.rayTraceBlocks(eye, end, false, false, true);
            if (target != null && target.typeOfHit == RayTraceResult.Type.BLOCK && event.getPos().equals(target.getBlockPos())) {
                Location location = new Location(living.world, target.getBlockPos());
                BlockSlab slab = (BlockSlab)block;
                IProperty<?> vprop = slab.getVariantProperty();
                Comparable<?> variant = state.getValue(vprop);
                EnumBlockHalf half = target.hitVec.y > 0.5 ? EnumBlockHalf.TOP : EnumBlockHalf.BOTTOM;
                ///TODO
                System.out.println("Broken half: " + half);
            }
        }
    }

    @SubscribeEvent
    public void onZombieKilled(LivingDeathEvent event) {
        EntityLivingBase living = event.getEntityLiving();
        if (!living.world.isRemote && living instanceof EntityZombie && event.getSource() instanceof EntityDamageSource) {
            EntityDamageSource source = (EntityDamageSource) event.getSource();
            Random rand = new Random();
            if (source.damageType.equals("player") && rand.nextInt(5) == 0) {
                EntityPlayer killer = (EntityPlayer) source.getTrueSource();
                if (killer != null) {
                    Server server = CraftAPI.getServer();
                    Player player = server.getPlayer(killer.getGameProfile());
                    if (player != null) {
                        EconomyManager economyManager = server.getEconomyManager();
                        if (economyManager.isEnabled()) {
                            float money = economyManager.roundUpToFormat(rand.nextFloat() * 0.9F + 0.1F);
                            economyManager.setBalance(player, economyManager.getBalance(player) + money);
                        }
                    }
                }
            }
        }
    }

    protected EntityPlayer getPlayer(MessageContext context) {
        return context.getServerHandler().player;
    }

    protected void syncTask(MessageContext context, Runnable task) {
        IThreadListener listener = context.side.isClient()
                ? FMLClientHandler.instance().getClient()
                : FMLCommonHandler.instance().getMinecraftServerInstance();
        listener.addScheduledTask(task);
    }

    protected AdvancedMessage handleShowScreen(MessageShowScreen message, MessageContext context) {
        return null;
    }

    protected AdvancedMessage handleShowScriptScreen(MessageShowScriptScreen message, MessageContext context) {
        return null;
    }

    protected AdvancedMessage handleBalance(MessageBalance message, MessageContext context) {
        return null;
    }

    protected AdvancedMessage handleClearChat(MessageClearChat messageClearChat, MessageContext context) {
        return null;
    }

    protected AdvancedMessage handleClientCustom(MessageCustom message, MessageContext context) {
        return null;
    }

    protected AdvancedMessage handleServerCustom(MessageCustom message, MessageContext context) {
        Server server = CraftAPI.getServer();
        ScriptManager scriptManager = server.getScriptManager();
        if (scriptManager.isEnabled()) {
            String channel = message.getChannel();
            for (ScriptContainerFile script : scriptManager.getAllLoadedScripts()) {
                NBTTagCompound response = script.handlePayload(channel, message.getData());
                if (response != null) {
                    return new MessageCustom(channel, response);
                }
            }
        }
        return null;
    }

    protected AdvancedMessage handleToast(MessageToast message, MessageContext context) {
        return null;
    }

    protected AdvancedMessage handleCountdown(MessageCountdown message, MessageContext context) {
        return null;
    }
}
