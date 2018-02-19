package ru.craftlogic.coremod;

import net.minecraftforge.fml.relauncher.IFMLLoadingPlugin;

import javax.annotation.Nullable;
import java.util.Map;

@IFMLLoadingPlugin.MCVersion("1.12.2")
@IFMLLoadingPlugin.Name("craftlogic-coremod")
@IFMLLoadingPlugin.SortingIndex(1001)
public class CraftLogicCoremod implements IFMLLoadingPlugin {

    @Override
    public String[] getASMTransformerClass() {
        return new String[] {
            "ru.craftlogic.coremod.asm.TransformerEntityPlayerMP",
            "ru.craftlogic.coremod.asm.TransformerTileEntity"
        };
    }

    @Override
    public String getModContainerClass() {
        return "ru.craftlogic.coremod.ModContainer";
    }

    @Nullable
    @Override
    public String getSetupClass() {
        return null;
    }

    @Override
    public void injectData(Map<String, Object> map) {

    }

    @Override
    public String getAccessTransformerClass() {
        return null;
    }
}
