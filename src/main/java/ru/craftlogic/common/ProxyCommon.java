package ru.craftlogic.common;

import net.minecraft.block.Block;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraftforge.event.entity.player.PlayerEvent;
import net.minecraftforge.event.terraingen.DecorateBiomeEvent;
import net.minecraftforge.event.terraingen.OreGenEvent;
import net.minecraftforge.event.terraingen.PopulateChunkEvent;
import net.minecraftforge.fluids.FluidRegistry;
import net.minecraftforge.fml.common.FMLCommonHandler;
import net.minecraftforge.fml.common.Loader;
import net.minecraftforge.fml.common.eventhandler.Event;
import net.minecraftforge.fml.common.eventhandler.EventPriority;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;
import net.minecraftforge.fml.common.network.simpleimpl.MessageContext;
import net.minecraftforge.fml.relauncher.Side;
import ru.craftlogic.CraftConfig;
import ru.craftlogic.api.CraftAPI;
import ru.craftlogic.api.CraftFluids;
import ru.craftlogic.api.block.holders.TileEntityHolder;
import ru.craftlogic.api.entity.AdvancedPlayer;
import ru.craftlogic.api.integration.ModIntegration;
import ru.craftlogic.api.network.AdvancedMessage;
import ru.craftlogic.api.network.AdvancedMessageHandler;
import ru.craftlogic.api.world.Player;
import ru.craftlogic.common.integration.IntegrationCOFH;
import ru.craftlogic.network.message.*;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import static ru.craftlogic.api.CraftAPI.NETWORK;

public class ProxyCommon extends AdvancedMessageHandler {
    private List<ModIntegration> integrations = new ArrayList<>(Arrays.asList(
        new IntegrationCOFH()
    ));

    public void preInit() {
        CraftAPI.init(FMLCommonHandler.instance().getSide());

        for (ModIntegration integration : this.integrations) {
            if (Loader.isModLoaded(integration.getModId())) {
                integration.preInit();
            }
        }
    }

    public void init() {
        NETWORK.registerMessage(this::handleServerStop, MessageServerStop.class, Side.CLIENT);
        NETWORK.registerMessage(this::handleServerCrash, MessageServerCrash.class, Side.CLIENT);
        NETWORK.registerMessage(this::handleShowScreen, MessageShowScreen.class, Side.CLIENT);
        NETWORK.registerMessage(this::handleToast, MessageToast.class, Side.CLIENT);
        NETWORK.registerMessage(this::handleCountdown, MessageCountdown.class, Side.CLIENT);
        NETWORK.registerMessage(this::handleQuestion, MessageQuestion.class, Side.CLIENT);
        NETWORK.registerMessage(this::handleToastQuestion, MessageToastQuestion.class, Side.CLIENT);
        NETWORK.registerMessage(this::handleConfirmation, MessageConfirmation.class, Side.SERVER);
        NETWORK.registerMessage(this::handlePlayerInfo, MessagePlayerInfo.class, Side.CLIENT);
        NETWORK.registerMessage(this::handleTimedTeleportStart, MessageTimedTeleportStart.class, Side.CLIENT);
        NETWORK.registerMessage(this::handleTimedTeleportEnd, MessageTimedTeleportEnd.class, Side.CLIENT);

        for (ModIntegration integration : this.integrations) {
            if (Loader.isModLoaded(integration.getModId())) {
                integration.init();
            }
        }
    }

    public void postInit() {
        CraftAPI.postInit(FMLCommonHandler.instance().getSide());
        for (Block block : Block.REGISTRY) {
            if (block instanceof TileEntityHolder) {
                ((TileEntityHolder) block).registerTileEntity();
            }
        }
        for (ModIntegration integration : this.integrations) {
            if (Loader.isModLoaded(integration.getModId())) {
                integration.postInit();
            }
        }
    }

    @SubscribeEvent(priority = EventPriority.HIGH)
    public void registerFluid(FluidRegistry.FluidRegisterEvent event) {
        System.out.println("Registered fluid '" + event.getFluidName() + "' with id: " + event.getFluidID());
        CraftFluids.FLUID_NAME_TO_ID.put(event.getFluidName(), event.getFluidID());
    }

    @SubscribeEvent(priority = EventPriority.HIGH)
    public void onPlayerClone(PlayerEvent.Clone event) {
        EntityPlayer original = event.getOriginal();
        NBTTagCompound originalEntityData = original.getEntityData();
        EntityPlayer player = event.getEntityPlayer();
        NBTTagCompound entityData = player.getEntityData();
        entityData.setTag(Player.CMD_COOLDOWN_KEY, originalEntityData.getCompoundTag(Player.CMD_COOLDOWN_KEY));
    }

    @SubscribeEvent(priority = EventPriority.HIGHEST)
    public void generateOre(OreGenEvent.GenerateMinable event) {
        if (CraftConfig.tweaks.enableStoneUnification) {
            switch (event.getType()) {
                case GRANITE:
                case DIORITE:
                case ANDESITE:
                case GRAVEL:
                case DIRT:
                    event.setResult(Event.Result.DENY);
                    break;
            }
        }
    }

    @SubscribeEvent(priority = EventPriority.HIGHEST)
    public void generateWaterfall(DecorateBiomeEvent.Decorate event) {
        if (event.getType() == DecorateBiomeEvent.Decorate.EventType.LAKE_WATER) event.setResult(Event.Result.DENY);
    }

    @SubscribeEvent(priority = EventPriority.HIGHEST)
    public void generateLake(PopulateChunkEvent.Populate event) {
        if (event.getType() == PopulateChunkEvent.Populate.EventType.LAKE) event.setResult(Event.Result.DENY);
    }

    protected AdvancedMessage handleServerStop(MessageServerStop message, MessageContext context) {
        return null;
    }

    protected AdvancedMessage handleServerCrash(MessageServerCrash message, MessageContext context) {
        return null;
    }

    protected AdvancedMessage handleShowScreen(MessageShowScreen message, MessageContext context) {
        return null;
    }

    protected AdvancedMessage handleToast(MessageToast message, MessageContext context) {
        return null;
    }

    protected AdvancedMessage handleCountdown(MessageCountdown message, MessageContext context) {
        return null;
    }

    protected AdvancedMessage handleQuestion(MessageQuestion message, MessageContext context) {
        return null;
    }

    protected AdvancedMessage handleToastQuestion(MessageToastQuestion message, MessageContext context) {
        return null;
    }

    protected AdvancedMessage handleConfirmation(MessageConfirmation message, MessageContext context) {
        EntityPlayer entity = getPlayer(context);
        Player player = ((AdvancedPlayer)entity).wrapped();
        player.confirm(message.getId(), message.getChoice());
        return null;
    }

    protected AdvancedMessage handlePlayerInfo(MessagePlayerInfo message, MessageContext context) { ;
        return null;
    }

    protected AdvancedMessage handleTimedTeleportStart(MessageTimedTeleportStart message, MessageContext context) {
        return null;
    }

    protected AdvancedMessage handleTimedTeleportEnd(MessageTimedTeleportEnd message, MessageContext context) {
        return null;
    }
}
