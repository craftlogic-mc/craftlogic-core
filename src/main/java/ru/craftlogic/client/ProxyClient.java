package ru.craftlogic.client;

import net.minecraft.block.Block;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiScreen;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.init.Items;
import net.minecraft.item.Item;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;
import net.minecraftforge.client.event.ModelRegistryEvent;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;
import ru.craftlogic.api.block.Colored;
import ru.craftlogic.api.block.holders.ScreenHolder;
import ru.craftlogic.api.item.ItemBlockBase;
import ru.craftlogic.api.world.TileEntities;
import ru.craftlogic.common.ProxyCommon;

import javax.annotation.Nullable;

public class ProxyClient extends ProxyCommon {
    private final Minecraft client = Minecraft.getMinecraft();
    private final ModelManager modelManager = new ModelManager();

    @Override
    public void preInit() {
        super.preInit();
    }

    @Override
    public void init() {
        super.init();
        for (Block block : Block.REGISTRY) {
            if (block instanceof Colored) {
                this.client.getBlockColors().registerBlockColorHandler(((Colored) block)::getBlockColor, block);
                Item item = Item.getItemFromBlock(block);
                if (item != Items.AIR && item instanceof ItemBlockBase) {
                    this.client.getItemColors().registerItemColorHandler(((Colored) block)::getItemColor, block);
                }
            }
        }
        for (Item item : Item.REGISTRY) {
            if (!(item instanceof ItemBlockBase) && item instanceof Colored) {
                this.client.getItemColors().registerItemColorHandler(((Colored)item)::getItemColor, item);
            }
        }
    }

    @Override
    public void postInit() {
        super.postInit();
    }

    @SubscribeEvent
    public void onModelsRegister(ModelRegistryEvent event) {
        this.modelManager.init();
    }

    @Nullable
    @Override
    public GuiScreen getClientGuiElement(int subId, EntityPlayer player, World world, int x, int y, int z) {
        BlockPos pos = new BlockPos(x, y, z);
        ScreenHolder screenHolder = TileEntities.getTileEntity(world, pos, ScreenHolder.class);
        return screenHolder != null ? screenHolder.createScreen(player, subId) : null;
    }
}
