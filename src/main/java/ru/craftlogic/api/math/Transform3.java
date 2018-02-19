//------------------------------------------------------------------------------------------------
//
//   Greg's Mod Base for 1.10 - 3D Transformation
//
//------------------------------------------------------------------------------------------------

package ru.craftlogic.api.math;

import net.minecraft.entity.Entity;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.math.AxisAlignedBB;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Vec3d;
import net.minecraft.util.math.Vec3i;

import java.util.List;

import static java.lang.Math.round;

public class Transform3 {

    public static final Transform3 IDENTITY = new Transform3(Vector3.ZERO);
    public static final Transform3 BLOCK_CENTER = new Transform3(Vector3.BLOCK_CENTER);
    
    public static final Transform3 SIDE_TURN_ROTATIONS[][] = new Transform3[6][4];
    static {
        for (int side = 0; side < 6; side++) {
            for (int turn = 0; turn < 4; turn++) {
                SIDE_TURN_ROTATIONS[side][turn] = new Transform3(Vector3.ZERO, Matrix3.SIDE_TURN_ROTATIONS[side][turn]);
            }
        }
    }
    
    public static Transform3 blockCenter(BlockPos pos) {
        return new Transform3(Vector3.blockCenter(pos));
    }
    
    public static Transform3 sideTurn(int side, int turn) {
        return SIDE_TURN_ROTATIONS[side][turn];
    }
    
    public static Transform3 sideTurn(double x, double y, double z, int side, int turn) {
        return sideTurn(new Vector3(x, y, z), side, turn);
    }
    
    public static Transform3 blockCenterSideTurn(int side, int turn) {
        return sideTurn(Vector3.BLOCK_CENTER, side, turn);
    }

    public static Transform3 sideTurn(Vector3 v, int side, int turn) {
        return new Transform3(v, Matrix3.SIDE_TURN_ROTATIONS[side][turn]);
    }
    
    public Vector3 offset;
    public Matrix3 rotation;
    public double scaling;
    
    public Transform3(Vector3 v) {
        this(v, Matrix3.IDENTITY);
    }
    
    public Transform3(Vector3 v, Matrix3 m) {
        this(v, m, 1.0);
    }

    public Transform3(Vector3 v, Matrix3 m, double s) {
        offset = v;
        rotation = m;
        scaling = s;
    }
    
    public Transform3(double dx, double dy, double dz) {
        this(new Vector3(dx, dy, dz), Matrix3.IDENTITY, 1.0);
    }
    
    public Transform3(BlockPos pos) {
        this(pos.getX() + 0.5, pos.getY() + 0.5, pos.getZ() + 0.5);
    }
    
    public Transform3 translate(Vector3 v) {
        if (v == Vector3.ZERO)
            return this;
        else
            return translate(v.x, v.y, v.z);
    }
    
    public Transform3 translate(double dx, double dy, double dz) {
        return new Transform3(
            offset.add(rotation.multiply(dx * scaling, dy * scaling, dz * scaling)),
            rotation,
            scaling);
    }
    
    public Transform3 rotate(Matrix3 m) {
        return new Transform3(offset, rotation.multiply(m), scaling);
    }
    
    public Transform3 rotateX(double deg) {
        return rotate(Matrix3.rotateX(deg));
    }
    
    public Transform3 rotateY(double deg) {
        return rotate(Matrix3.rotateY(deg));
    }
    
    public Transform3 rotateZ(double deg) {
        return rotate(Matrix3.rotateZ(deg));
    }
    
    public Transform3 scale(double s) {
        return new Transform3(offset, rotation, scaling * s);
    }
    
    public Transform3 side(EnumFacing dir) {
        return side(dir.ordinal());
    }
    
    public Transform3 side(int i) {
        return rotate(Matrix3.SIDE_ROTATIONS[i]);
    }
    
    public Transform3 turn(int i) {
        return rotate(Matrix3.TURN_ROTATIONS[i]);
    }

    public Transform3 t(Transform3 t) {
        return new Transform3(
            offset.add(rotation.multiply(t.offset).mul(scaling)),
            rotation.multiply(t.rotation),
            scaling * t.scaling);
    }
    
    public Vector3 p(double x, double y, double z) {
        return p(new Vector3(x, y, z));
    }
    
    public Vector3 p(Vector3 u) {
        return offset.add(rotation.multiply(u.mul(scaling)));
    }
    
    public Vector3 ip(double x, double y, double z) {
        return ip(new Vector3(x, y, z));
    }
    
    public Vector3 ip(Vector3 u) {
        return rotation.inverseMultiply(u.sub(offset)).mul(1.0/scaling);
    }
    
    public Vector3 v(double x, double y, double z) {
        return v(new Vector3(x, y, z));
    }
    
    public Vector3 iv(double x, double y, double z) {
        return iv(new Vector3(x, y, z));
    }
    
    public Vector3 v(Vec3i u) {
        return v(u.getX(), u.getY(), u.getZ());
    }
    
    public Vector3 iv(Vec3i u) {
        return iv(u.getX(), u.getY(), u.getZ());
    }
    
    public Vector3 v(Vector3 u) {
        return rotation.multiply(u.mul(scaling));
    }
    
    public Vector3 v(EnumFacing f) {
        return v(Vector3.getDirectionVec(f));
    }

    public Vector3 iv(EnumFacing f) {
        return iv(Vector3.getDirectionVec(f));
    }

    public Vector3 iv(Vector3 u) {
        return rotation.inverseMultiply(u).mul(1.0/scaling);
    }
    
    public Vector3 iv(Vec3d u) {
        return iv(u.x, u.y, u.z);
    }
    
    public AxisAlignedBB t(AxisAlignedBB box) {
        return boxEnclosing(p(box.minX, box.minY, box.minZ), p(box.maxX, box.maxY, box.maxZ));
    }
    
    public AxisAlignedBB box(Vector3 p0, Vector3 p1) {
        return boxEnclosing(p(p0), p(p1));
    }

    public static int turnFor(Entity e, int side) {
        if (side > 1)
            return 0;
        int rot = (int)round(e.rotationYaw / 90);
        if (side == 0)
            rot = 2 - rot;
        else
            rot = 2 + rot;
        return rot & 0x3;
    }
    
    public static AxisAlignedBB boxEnclosing(Vector3 p, Vector3 q) {
        return new AxisAlignedBB(p.x, p.y, p.z, q.x, q.y, q.z);
    }

    public EnumFacing t(EnumFacing f) {
        return v(f).facing();
    }
    
    public EnumFacing it(EnumFacing f) {
        return iv(f).facing();
    }
    
    public void addBox(Vector3 p0, Vector3 p1, List<AxisAlignedBB> boxes) {
        addBox(p0.x, p0.y, p0.z, p1.x, p1.y, p1.z, boxes);
    }
    
    public void addBox(double x0, double y0, double z0, double x1, double y1, double z1, List<AxisAlignedBB> boxes) {
        AxisAlignedBB box = boxEnclosing(p(x0, y0, z0), p(x1, y1, z1));
        boxes.add(box);
    }

}
