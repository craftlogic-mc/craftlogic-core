package ru.craftlogic.api.entity;

import net.minecraft.entity.IRangedAttackMob;
import net.minecraft.util.ResourceLocation;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;

import static ru.craftlogic.api.CraftAPI.MOD_ID;
import static ru.craftlogic.api.CraftAPI.wrapWithModId;

public interface Zombie extends IRangedAttackMob {
    @SideOnly(Side.CLIENT)
    boolean isSwingingArms();

    default ZombieVariant getVariant() {
        return ZombieVariant.NORMAL;
    }

    enum ZombieVariant {
        NORMAL("normal", 9),
        PLAGUE("plague", 18),
        HUNGRY("hungry", 18);

        private final ResourceLocation texture;
        private final int bowRarity;

        ZombieVariant(String texture, int bowRarity) {
            ResourceLocation tx = wrapWithModId(texture, MOD_ID);
            this.texture = new ResourceLocation(tx.getResourceDomain(), "textures/entity/zombie/" + tx.getResourcePath() + ".png");
            this.bowRarity = bowRarity;
        }

        public ResourceLocation getTexture() {
            return texture;
        }

        public int getBowRarity() {
            return bowRarity;
        }
    }
}
