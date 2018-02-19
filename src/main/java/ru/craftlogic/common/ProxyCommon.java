package ru.craftlogic.common;

import net.minecraft.block.Block;
import net.minecraft.block.BlockGlass;
import net.minecraft.block.BlockPane;
import net.minecraft.block.material.Material;
import net.minecraft.block.state.IBlockState;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.init.Items;
import net.minecraft.inventory.Container;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.RayTraceResult;
import net.minecraft.world.World;
import net.minecraftforge.fml.common.network.IGuiHandler;
import ru.craftlogic.api.block.holders.ScreenHolder;
import ru.craftlogic.api.entity.EntityThrownItem;
import ru.craftlogic.api.world.Location;
import ru.craftlogic.api.world.TileEntities;

import javax.annotation.Nullable;

import static ru.craftlogic.CraftLogic.registerEntity;

public class ProxyCommon implements IGuiHandler {
    public void preInit() {}

    public void init() {
        registerEntity(EntityThrownItem.class, "thrown_item", 64, 10, true);
    }

    public void postInit() {
        EntityThrownItem.registerThrowable(Items.BRICK, 0.5F, (item, target) -> {
            if (target.typeOfHit == RayTraceResult.Type.BLOCK) {
                Location loc = new Location(item.world, target.getBlockPos());
                IBlockState state = loc.getBlockState();
                Block block = state.getBlock();
                if (block instanceof BlockGlass || block instanceof BlockPane && state.getMaterial() == Material.GLASS) {
                    loc.setToAir();
                    loc.playEvent(2001, Block.getStateId(state));
                }
            }
        });
    }

    @Nullable
    @Override
    public Container getServerGuiElement(int subId, EntityPlayer player, World world, int x, int y, int z) {
        BlockPos pos = new BlockPos(x, y, z);
        ScreenHolder screenHolder = TileEntities.getTileEntity(world, pos, ScreenHolder.class);
        return screenHolder != null ? screenHolder.createContainer(player, subId) : null;
    }

    @Nullable
    @Override
    public Object getClientGuiElement(int id, EntityPlayer player, World world, int x, int y, int z) {
        return null;
    }
}
