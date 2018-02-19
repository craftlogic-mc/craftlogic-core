package ru.craftlogic.api.math;

public final class Matrix3 {

    public static final Matrix3 IDENTITY = new Matrix3();

    public static final Matrix3[] TURN_ROTATIONS = {
        rotateY(0), rotateY(90), rotateY(180), rotateY(270)
    };

    public static final Matrix3[] SIDE_ROTATIONS = {
        /*0, -Y, DOWN */ IDENTITY,
        /*1, +Y, UP   */ rotateX(180),
        /*2, -Z, NORTH*/ rotateX(90),
        /*3, +Z, SOUTH*/ rotateX(-90).multiply(rotateY(180)),
        /*4, -X, WEST */ rotateZ(-90).multiply(rotateY(90)),
        /*5, +X, EAST */ rotateZ(90).multiply(rotateY(-90))
    };
    
    public static final Matrix3[][] SIDE_TURN_ROTATIONS = new Matrix3[6][4];
    static {
        for (int side = 0; side < 6; side++) {
            for (int turn = 0; turn < 4; turn++) {
                SIDE_TURN_ROTATIONS[side][turn] = SIDE_ROTATIONS[side].multiply(TURN_ROTATIONS[turn]);
            }
        }
    }
    
    private double m[][] = new double[][] {
        {1, 0, 0},
        {0, 1, 0},
        {0, 0, 1}
    };
        
    public static Matrix3 rotateX(double deg) {
        return rotate(deg, 1, 2);
    }
    
    public static Matrix3 rotateY(double deg) {
        return rotate(deg, 2, 0);
    }
    
    public static Matrix3 rotateZ(double deg) {
        return rotate(deg, 0, 1);
    }
    
    static Matrix3 rotate(double deg, int i, int j) {
        double a = Math.toRadians(deg);
        double s = Math.sin(a);
        double c = Math.cos(a);
        Matrix3 r = new Matrix3();
        r.m[i][i] = c;
        r.m[i][j] = -s;
        r.m[j][i] = s;
        r.m[j][j] = c;
        return r;
    }
    
    public Matrix3 multiply(Matrix3 n) {
        Matrix3 r = new Matrix3();
        for (int i = 0; i < 3; i++)
            for (int j = 0; j < 3; j++)
                r.m[i][j] = m[i][0] * n.m[0][j] + m[i][1] * n.m[1][j] + m[i][2] * n.m[2][j];
        return r;
    }

//  public Matrix3 inverseMultiply(Matrix3 n) {
//      Matrix3 r = new Matrix3();
//      for (int i = 0; i < 3; i++)
//          for (int j = 0; j < 3; j++)
//              r.m[i][j] = m[0][i] * n.m[j][0] + m[1][i] * n.m[j][1] + m[2][i] * n.m[j][2];
//      return r;
//  }

    public Vector3 multiply(double x, double y, double z) {
        return new Vector3(
            x * m[0][0] + y * m[0][1] + z * m[0][2],
            x * m[1][0] + y * m[1][1] + z * m[1][2],
            x * m[2][0] + y * m[2][1] + z * m[2][2]
        );
    }
    
    public Vector3 inverseMultiply(double x, double y, double z) {
        //  Multiply by inverse, assuming an orthonormal matrix
        return new Vector3(
            x * m[0][0] + y * m[1][0] + z * m[2][0],
            x * m[0][1] + y * m[1][1] + z * m[2][1],
            x * m[0][2] + y * m[1][2] + z * m[2][2]
        );
    }

    public Vector3 multiply(Vector3 v) {
        return multiply(v.x, v.y, v.z);
    }
    
    public Vector3 inverseMultiply(Vector3 v) {
        return inverseMultiply(v.x, v.y, v.z);
    }
    
    public void dump() {
        for (int i = 0; i < 3; i++) {
            System.out.printf("[%6.3f %6.3f %6.3f]\n", m[i][0], m[i][1], m[i][2]);
        }
    }

}
