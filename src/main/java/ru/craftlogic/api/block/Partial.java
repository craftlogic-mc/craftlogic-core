package ru.craftlogic.api.block;

import net.minecraft.block.state.IBlockState;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.ItemStack;
import net.minecraft.util.EnumHand;
import net.minecraft.util.math.AxisAlignedBB;
import net.minecraft.util.math.RayTraceResult;
import net.minecraft.util.math.Vec3d;
import ru.craftlogic.api.world.Location;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import java.util.Collections;
import java.util.List;

public interface Partial {
    @Nullable
    Part getPart(IBlockState state, String name);

    default List<Part> getAllParts(Location location) {
        Part root = getRootPart(location);
        if (root != null) {
            return Collections.singletonList(root.applyTransformation(location));
        }
        return Collections.emptyList();
    }

    @Nullable
    default Part getRootPart(Location location) {
        return new Part() {
            @Override
            public String getName() {
                return "root";
            }

            @Override
            public AxisAlignedBB getBounding() {
                return location.getBlockBounding();
            }
        };
    }

    @Nullable
    default Part getPart(Location location, RayTraceResult target) {
        Vec3d hitVec = target.hitVec;
        List<Part> parts = getAllParts(location);
        for (Part part : parts) {
            AxisAlignedBB bounding = part.getBounding();
            if (bounding.minX <= hitVec.x && bounding.maxX >= hitVec.x &&
                bounding.minY <= hitVec.y && bounding.maxY >= hitVec.y &&
                bounding.minZ <= hitVec.z && bounding.maxZ >= hitVec.z ){

                return part;
            }
        }
        return null;
    }

    interface Part {
        String getName();
        AxisAlignedBB getBounding();

        @Nonnull
        default Part applyTransformation(Location location) {
            return this;
        }

        default boolean onActivated(Location location, EntityPlayer player, EnumHand hand) {
            return false;
        }

        default boolean onBroken(Location location, EntityPlayer player, ItemStack item) {
            return true;
        }
    }
}
