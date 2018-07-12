package ru.craftlogic.api.model;

import net.minecraft.block.Block;
import net.minecraft.block.state.IBlockState;
import net.minecraft.client.renderer.ItemMeshDefinition;
import net.minecraft.client.renderer.block.model.ModelResourceLocation;
import net.minecraft.client.renderer.block.statemap.IStateMapper;
import net.minecraft.client.renderer.block.statemap.StateMapperBase;
import net.minecraft.item.Item;
import net.minecraft.util.ResourceLocation;
import net.minecraftforge.client.model.ModelLoader;
import ru.craftlogic.CraftLogic;
import ru.craftlogic.api.item.ItemBlockBase;

import javax.annotation.Nonnull;
import java.util.function.BiFunction;

public class ModelManager {
    private String activeDomain;

    public void init() {
        for (Block block : Block.REGISTRY) {
            if (block instanceof ModelAutoReg) {
                this.activeDomain = block.getRegistryName().getResourceDomain();
                ((ModelAutoReg) block).registerModel(this);
            }
        }
        for (Item item : Item.REGISTRY) {
            if (!(item instanceof ItemBlockBase) && item instanceof ModelAutoReg) {
                this.activeDomain = item.getRegistryName().getResourceDomain();
                ((ModelAutoReg) item).registerModel(this);
            }
        }
        this.activeDomain = CraftLogic.MODID;
    }

    public void registerStateMapper(@Nonnull Block block, BiFunction<IBlockState, StateMapperBase, ModelResourceLocation> stateMapper) {
        this.registerStateMapper(block, new StateMapperBase() {
            @Override
            protected ModelResourceLocation getModelResourceLocation(IBlockState state) {
                return stateMapper.apply(state, this);
            }
        });
    }

    public void registerStateMapper(@Nonnull Block block, IStateMapper stateMapper) {
        ModelLoader.setCustomStateMapper(block, stateMapper);
    }

    public void registerItemModel(@Nonnull Item item) {
        this.registerItemModel(item, item.getRegistryName());
    }

    public void registerItemModel(@Nonnull Item item, String model) {
        this.registerItemModel(item, 0, model);
    }
    
    public void registerItemModel(@Nonnull Item item, @Nonnull ResourceLocation model) {
        this.registerItemModel(item, 0, new ModelResourceLocation(model, "#inventory"));
    }

    public void registerCustomMeshDefinition(@Nonnull Item item, @Nonnull ItemMeshDefinition meshDefinition) {
        ModelLoader.setCustomMeshDefinition(item, meshDefinition);
    }

    public void registerItemModel(@Nonnull Item item, int metadata, String model) {
        ResourceLocation id = wrapWithActiveDomain(model);
        this.registerItemModel(item, metadata, new ModelResourceLocation(id, "inventory"));
    }

    public void registerItemModel(@Nonnull Item item, int metadata, @Nonnull ModelResourceLocation model) {
        ModelLoader.setCustomModelResourceLocation(item, metadata, model);
    }

    public void registerItemVariants(@Nonnull Item item, String... variants) {
        ResourceLocation[] res = new ResourceLocation[variants.length];
        for (int i = 0; i < variants.length; i++) {
            res[i] = wrapWithActiveDomain(variants[i]);
        }
        ModelLoader.registerItemVariants(item, res);
    }

    private ResourceLocation wrapWithActiveDomain(String id) {
        return !id.contains(":") ? new ResourceLocation(this.activeDomain, id) : new ResourceLocation(id);
    }
}
