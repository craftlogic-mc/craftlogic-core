package ru.craftlogic.common.item;

import net.minecraft.creativetab.CreativeTabs;
import net.minecraft.entity.Entity;
import net.minecraft.entity.item.EntityItemFrame;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.util.EnumActionResult;
import net.minecraft.util.EnumHand;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.RayTraceResult;
import net.minecraft.util.text.ITextComponent;
import net.minecraft.util.text.TextComponentTranslation;
import net.minecraft.world.World;
import ru.craftlogic.api.block.HeatAcceptor;
import ru.craftlogic.api.item.ItemBase;
import ru.craftlogic.api.world.Location;
import ru.craftlogic.api.world.TileEntities;

public class ItemThermometer extends ItemBase {
    public ItemThermometer() {
        super("thermometer", CreativeTabs.TOOLS);
        this.addPropertyOverride(new ResourceLocation("temperature"), (item, world, entity) -> {
            if (entity != null || item.isOnItemFrame()) {
                boolean handheld = entity != null;
                Entity holder = handheld ? entity : item.getItemFrame();
                if (holder instanceof EntityItemFrame){
                    Location location = new Location(holder).offset(holder.getAdjustedHorizontalFacing().getOpposite());
                    HeatAcceptor heatAcceptor = location.getTileEntity(HeatAcceptor.class);
                    if (heatAcceptor != null) {
                        float value = (float)heatAcceptor.getMaxTemperature() / heatAcceptor.getMaxTemperature();
                        return Math.min(1f, Math.max(0f, value));
                    }
                }
            }
            return 0F;
        });
    }

    @Override
    public EnumActionResult onItemUse(World world, BlockPos pos, RayTraceResult target, EntityPlayer player, EnumHand hand) {
        HeatAcceptor heatAcceptor = TileEntities.getTileEntity(world, pos, HeatAcceptor.class);
        if (heatAcceptor != null) {
            int temperature = heatAcceptor.getTemperature();
            int maxTemperature = heatAcceptor.getMaxTemperature();
            ITextComponent message = new TextComponentTranslation("tooltip.temperature", temperature, maxTemperature);
            player.sendStatusMessage(message, true);
            return EnumActionResult.SUCCESS;
        } else {
            return super.onItemUse(world, pos, target, player, hand);
        }
    }
}
