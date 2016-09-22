package nl.ramondevaan.visualization.mesh;

public class PointGenerator {
    private Mesh mesh;
    private float[] min;
    private float[] max;

    public PointGenerator() {

    }

    public final void setMesh(Mesh mesh) {
        this.mesh = mesh;
    }

    public final Vec3d[] generatePoints(int num) {
        meshCheck();
        if(num <= 0) {
            return new Vec3d[0];
        }

        Vec3d[] ret = new Vec3d[num];

        for(int i = 0; i < ret.length; i++) {
            ret[i] = generatePointImpl();
        }

        return ret;
    }

    public final double[] generatePoint() {
        meshCheck();

        return generatePointImpl();
    }

    private double[] generatePointImpl() {
        double[] p = random();
        while(!mesh.contains(p)) {
            p = random();
        }

        return p;
    }

    private void contains(double[] p) {

    }

    private double[] random() {
        return new double[]{
                Math.random() * (mesh.max.x - mesh.min.x) + mesh.min.x,
                Math.random() * (mesh.max.y - mesh.min.y) + mesh.min.y,
                Math.random() * (mesh.max.z - mesh.min.z) + mesh.min.z
        };
    }

    private void meshCheck() {
        if(mesh == null) {
            throw new IllegalArgumentException("No mesh given");
        }
    }
}
