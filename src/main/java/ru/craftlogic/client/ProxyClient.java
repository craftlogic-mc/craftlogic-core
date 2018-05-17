package ru.craftlogic.client;

import net.minecraft.block.Block;
import net.minecraft.client.Minecraft;
import net.minecraft.entity.passive.EntityChicken;
import net.minecraft.init.Items;
import net.minecraft.item.Item;
import net.minecraftforge.client.event.ModelRegistryEvent;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;
import ru.craftlogic.api.block.Colored;
import ru.craftlogic.api.item.ItemBlockBase;
import ru.craftlogic.api.model.ModelManager;
import ru.craftlogic.client.render.RenderChicken;
import ru.craftlogic.client.render.RenderThrownItem;
import ru.craftlogic.common.ProxyCommon;
import ru.craftlogic.common.entity.EntityThrownItem;

import static ru.craftlogic.CraftLogic.registerEntityRenderer;

public class ProxyClient extends ProxyCommon {
    private final Minecraft client = Minecraft.getMinecraft();
    private final ModelManager modelManager = new ModelManager();

    @Override
    public void preInit() {
        super.preInit();
        registerEntityRenderer(EntityThrownItem.class, rm -> new RenderThrownItem(rm, client.getRenderItem()));
        registerEntityRenderer(EntityChicken.class, RenderChicken::new);
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
}
