package ru.craftlogic.mixin.client.renderer.tileentity;

import net.minecraft.block.BlockChest;
import net.minecraft.block.state.IBlockState;
import net.minecraft.client.model.ModelChest;
import net.minecraft.client.renderer.GlStateManager;
import net.minecraft.client.renderer.tileentity.TileEntityChestRenderer;
import net.minecraft.client.renderer.tileentity.TileEntitySpecialRenderer;
import net.minecraft.tileentity.TileEntityChest;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.math.BlockPos;
import org.lwjgl.opengl.GL11;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Overwrite;
import org.spongepowered.asm.mixin.Shadow;
import ru.craftlogic.api.world.TileEntities;
import ru.craftlogic.common.block.ChestPart;

import static net.minecraft.block.BlockChest.FACING;
import static ru.craftlogic.common.block.ChestProperties.PART;

@Mixin(TileEntityChestRenderer.class)
public class MixinTileEntityChestRenderer extends TileEntitySpecialRenderer<TileEntityChest> {
    @Shadow @Final
    private static ResourceLocation TEXTURE_TRAPPED_DOUBLE, TEXTURE_CHRISTMAS_DOUBLE,
            TEXTURE_NORMAL_DOUBLE, TEXTURE_TRAPPED, TEXTURE_CHRISTMAS, TEXTURE_NORMAL;
    @Shadow @Final
    private ModelChest simpleChest, largeChest;
    @Shadow
    private boolean isChristmas;

    @Overwrite
    public void render(TileEntityChest chest, double x, double y, double z, float deltaTime, int destroyStage, float alpha) {
        GlStateManager.enableDepth();
        GlStateManager.depthFunc(GL11.GL_LEQUAL);
        GlStateManager.depthMask(true);
        ChestPart part = ChestPart.SINGLE;
        EnumFacing facing = EnumFacing.SOUTH;
        TileEntityChest secondPart = null;
        if (chest.hasWorld()) {
            IBlockState state = chest.getWorld().getBlockState(chest.getPos());
            if (!(state.getBlock() instanceof BlockChest)) {
                return;
            }
            part = state.getValue(PART);
            if (part == ChestPart.RIGHT) {
                return;
            }
            if (part == ChestPart.SINGLE) {
                ((BlockChest)state.getBlock()).checkForSurroundingChests(chest.getWorld(), chest.getPos(), state);
                state = chest.getWorld().getBlockState(chest.getPos());
            }
            chest.checkForAdjacentChests();

            part = state.getValue(PART);
            facing = state.getValue(FACING);

            if (part != ChestPart.SINGLE) {
                BlockPos offsetPos = chest.getPos().offset(part.rotate(facing));
                secondPart = TileEntities.getTileEntity(chest.getWorld(), offsetPos, TileEntityChest.class);
            }
        }

        ModelChest model = part == ChestPart.SINGLE ? this.simpleChest : this.largeChest;

        if (part == ChestPart.SINGLE) {
            if (destroyStage >= 0) {
                this.bindTexture(DESTROY_STAGES[destroyStage]);
                GlStateManager.matrixMode(GL11.GL_TEXTURE);
                GlStateManager.pushMatrix();
                GlStateManager.scale(4F, 4F, 1F);
                GlStateManager.translate(0.0625F, 0.0625F, 0.0625F);
                GlStateManager.matrixMode(GL11.GL_MODELVIEW);
            } else if (this.isChristmas) {
                this.bindTexture(TEXTURE_CHRISTMAS);
            } else if (chest.getChestType() == BlockChest.Type.TRAP) {
                this.bindTexture(TEXTURE_TRAPPED);
            } else {
                this.bindTexture(TEXTURE_NORMAL);
            }
        } else {
            if (destroyStage >= 0) {
                this.bindTexture(DESTROY_STAGES[destroyStage]);
                GlStateManager.matrixMode(GL11.GL_TEXTURE);
                GlStateManager.pushMatrix();
                GlStateManager.scale(8F, 4F, 1F);
                GlStateManager.translate(0.0625F, 0.0625F, 0.0625F);
                GlStateManager.matrixMode(GL11.GL_MODELVIEW);
            } else if (this.isChristmas) {
                this.bindTexture(TEXTURE_CHRISTMAS_DOUBLE);
            } else if (chest.getChestType() == BlockChest.Type.TRAP) {
                this.bindTexture(TEXTURE_TRAPPED_DOUBLE);
            } else {
                this.bindTexture(TEXTURE_NORMAL_DOUBLE);
            }
        }

        GlStateManager.pushMatrix();
        GlStateManager.enableRescaleNormal();
        if (destroyStage < 0) {
            GlStateManager.color(1F, 1F, 1F, alpha);
        }

        GlStateManager.translate((float)x, (float)y + 1F, (float)z + 1F);
        GlStateManager.scale(1F, -1F, -1F);
        GlStateManager.translate(0.5F, 0.5F, 0.5F);
        int rotation = 0;
        switch (facing) {
            case NORTH:
                rotation = 180;
                break;
            case WEST:
                rotation = 90;
                break;
            case EAST:
                rotation = -90;
                break;
        }

        GlStateManager.rotate((float)rotation, 0F, 1F, 0F);
        GlStateManager.translate(-0.5F, -0.5F, -0.5F);
        float lidAngle = chest.prevLidAngle + (chest.lidAngle - chest.prevLidAngle) * deltaTime;
        if (secondPart != null) {
            float otherLidAngle = secondPart.prevLidAngle + (secondPart.lidAngle - secondPart.prevLidAngle) * deltaTime;
            if (otherLidAngle > lidAngle) {
                lidAngle = otherLidAngle;
            }
        }

        lidAngle = 1F - lidAngle;
        lidAngle = 1F - lidAngle * lidAngle * lidAngle;
        model.chestLid.rotateAngleX = -(lidAngle * (float)(Math.PI / 2.0));
        model.renderAll();
        GlStateManager.disableRescaleNormal();
        GlStateManager.popMatrix();
        GlStateManager.color(1F, 1F, 1F, 1F);
        if (destroyStage >= 0) {
            GlStateManager.matrixMode(GL11.GL_TEXTURE);
            GlStateManager.popMatrix();
            GlStateManager.matrixMode(GL11.GL_MODELVIEW);
        }
    }
}
