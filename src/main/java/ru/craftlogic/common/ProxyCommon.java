package ru.craftlogic.common;

import net.minecraft.block.Block;
import net.minecraft.block.BlockSlab;
import net.minecraft.block.BlockSlab.EnumBlockHalf;
import net.minecraft.block.properties.IProperty;
import net.minecraft.block.state.IBlockState;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.init.Items;
import net.minecraft.item.ItemStack;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.math.RayTraceResult;
import net.minecraft.util.math.Vec3d;
import net.minecraftforge.event.entity.living.LivingDestroyBlockEvent;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;
import net.minecraftforge.fml.relauncher.Side;
import ru.craftlogic.api.network.message.MessageShowScreen;
import ru.craftlogic.api.proxy.Proxy;
import ru.craftlogic.api.recipe.OreStack;
import ru.craftlogic.api.recipe.Recipes;
import ru.craftlogic.api.world.Location;
import ru.craftlogic.common.entity.EntityThrownItem;
import ru.craftlogic.common.recipe.RecipeAlloying;
import ru.craftlogic.common.recipe.RecipeGridAlloying;

import static ru.craftlogic.CraftLogic.*;

public class ProxyCommon extends Proxy {
    public void preInit() {
        CraftSounds.init();
        CraftBlocks.init();
        CraftItems.init();
        CraftPlants.init();

        Recipes.registerRecipe(RecipeGridAlloying.class, new RecipeAlloying(
            new ResourceLocation(MODID, "iron_nugget"),
            new ItemStack(Items.IRON_NUGGET, 8),
            1F,
            1538,
            300,
            new OreStack("oreIron")
        ));
        Recipes.registerRecipe(RecipeGridAlloying.class, new RecipeAlloying(
            new ResourceLocation(MODID, "iron_ingot"),
            new ItemStack(Items.IRON_INGOT),
            1F,
            1538,
            150,
            new OreStack("nuggetIron", 9)
        ));
    }

    public void init() {
        registerMessage(new MessageShowScreen.Handler(), MessageShowScreen.class, Side.CLIENT);
    }

    public void postInit() {
        registerEntity(EntityThrownItem.class, "thrown_item", 64, 10, true);
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
}
