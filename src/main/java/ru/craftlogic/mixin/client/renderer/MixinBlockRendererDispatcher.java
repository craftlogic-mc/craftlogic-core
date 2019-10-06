package ru.craftlogic.mixin.client.renderer;

import net.minecraft.block.*;
import net.minecraft.block.state.IBlockState;
import net.minecraft.client.renderer.BlockModelRenderer;
import net.minecraft.client.renderer.BlockRendererDispatcher;
import net.minecraft.client.renderer.BufferBuilder;
import net.minecraft.client.renderer.block.model.IBakedModel;
import net.minecraft.crash.CrashReport;
import net.minecraft.crash.CrashReportCategory;
import net.minecraft.init.Blocks;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.ReportedException;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.IBlockAccess;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;
import ru.craftlogic.CraftConfig;
import ru.craftlogic.common.block.BlockRock;

@Mixin(BlockRendererDispatcher.class)
public abstract class MixinBlockRendererDispatcher {
    @Shadow public abstract IBakedModel getModelForState(IBlockState state);

    @Shadow @Final private BlockModelRenderer blockModelRenderer;

    private boolean isStateApplicableForSnowLayer(IBlockState state) {
        return state.getBlock() instanceof BlockDoublePlant
            ? state.getValue(BlockDoublePlant.HALF) == BlockDoublePlant.EnumBlockHalf.LOWER
            : state.getBlock() instanceof BlockBush || state.getBlock() instanceof BlockRock;
    }

    private boolean isStateASnowyBlock(IBlockState state) {
        return state.getBlock() instanceof BlockSnow
            || state.getBlock() instanceof BlockSnowBlock
            || state.getBlock() instanceof BlockGrass && state.getValue(BlockGrass.SNOWY);
    }

    @Inject(method = "renderBlock", at = @At("HEAD"), cancellable = true)
    private void renderSnowLayerOverBlocks(IBlockState state, BlockPos pos, IBlockAccess blockAccessor, BufferBuilder bufferBuilder, CallbackInfoReturnable<Boolean> ci) {
        if (CraftConfig.tweaks.enableVisualSnowTweaks) {
            try {
                if (isStateApplicableForSnowLayer(state)) {
                    int snowCount = 0;
                    for (EnumFacing side : EnumFacing.values()) {
                        BlockPos p = pos.offset(side);
                        IBlockState s = blockAccessor.getBlockState(p).getActualState(blockAccessor, p);
                        if (isStateASnowyBlock(s)) {
                            snowCount++;
                        }
                    }
                    if (snowCount >= 2) {
                        IBlockState s = Blocks.SNOW_LAYER.getDefaultState();
                        IBakedModel snowLayer = getModelForState(s);
                        if (!blockModelRenderer.renderModel(blockAccessor, snowLayer, s, pos, bufferBuilder, false)) {
                            ci.setReturnValue(false);
                        }
                    }
                } else if (state.getBlock() == Blocks.GRASS && !state.getActualState(blockAccessor, pos).getValue(BlockGrass.SNOWY) && isStateApplicableForSnowLayer(blockAccessor.getBlockState(pos.up()))) {
                    int snowCount = 0;
                    for (EnumFacing side : EnumFacing.values()) {
                        BlockPos p = pos.offset(side);
                        IBlockState s = blockAccessor.getBlockState(p).getActualState(blockAccessor, p);
                        if (s.getBlock() instanceof BlockGrass && s.getValue(BlockGrass.SNOWY)) {
                            snowCount++;
                        }
                    }
                    if (snowCount >= 2) {
                        state = state.withProperty(BlockGrass.SNOWY, true);
                        IBakedModel model = getModelForState(state);
                        ci.setReturnValue(!blockModelRenderer.renderModel(blockAccessor, model, state, pos, bufferBuilder, false));
                    }
                }
            } catch (Throwable t) {
                CrashReport report = CrashReport.makeCrashReport(t, "Tesselating block in world");
                CrashReportCategory category = report.makeCategory("Block being tesselated");
                CrashReportCategory.addBlockInfo(category, pos, state.getBlock(), state.getBlock().getMetaFromState(state));
                throw new ReportedException(report);
            }
        }
    }
}
