package nl.ramondevaan.visualization.point;

import com.sun.javafx.geom.Vec3d;
import nl.ramondevaan.visualization.mesh.Mesh;
import org.apache.commons.lang3.Validate;

import java.util.Arrays;

public class MeshInternalPointGenerator extends PointGenerator {
    private final static double EPSILON = 0.000001d;
    
    private Mesh mesh;
    private boolean meshChanged;
    private int dimensionality;
    private float[][] coordinates;
    private int[][] faces;
    private float[] min;
    private float[] max;
    private int numberOfPoints;

    public MeshInternalPointGenerator() {
        meshChanged = true;
    }

    public final void setMesh(Mesh mesh) {
        this.mesh = mesh;
        meshChanged = true;
    }
    
    @Override
    protected void checkValidity() {
        checkMesh();
    }
    
    private void checkMesh() {
        if(meshChanged) {
            Validate.notNull(mesh);
            this.dimensionality = mesh.getDimensionality();
            this.coordinates = mesh.getCoordinates();
            this.faces = mesh.getFaces();
            if(dimensionality != 3) {
                throw new UnsupportedOperationException(
                        "Dimensionality other than 3 is currently not supported");
            }
            for(int[] f : faces) {
                if(f.length != 3) {
                    throw new UnsupportedOperationException(
                            "Only triangular faces are currently supported");
                }
            }
            computeBounds();
        
            meshChanged = false;
        }
    }
    
    @Override
    protected float[] generatePointImpl() {
        float[] p = random();
        while(!contains(p)) {
            p = random();
        }
        
        return p;
    }
    
    private boolean contains(float[] p) {
        Vec3d origin = new Vec3d(p[0], p[1], p[2]);
        Vec3d dir = new Vec3d(0, 0, 1);
        
        int count = 0;
        Vec3d[] polygon;
        for(int[] face : faces) {
            polygon = new Vec3d[face.length];
            for(int i = 0; i < face.length; i++) {
                polygon[i] = new Vec3d(
                        coordinates[face[i]][0],
                        coordinates[face[i]][1],
                        coordinates[face[i]][2]
                );
            }
            if(intersects(origin, dir, polygon)) {
                count++;
            }
        }
        
        return count % 2 == 1;
    }
    
    private boolean intersects(Vec3d o, Vec3d d, Vec3d[] p) {
        //Find vectors for two edges sharing V1
        Vec3d e1 = new Vec3d(p[1]);
        e1.sub(p[0]);
        Vec3d e2 = new Vec3d(p[2]);
        e2.sub(p[0]);
        //Begin calculating determinant - also used to calculate u parameter
        Vec3d P = new Vec3d();
        P.cross(d, e2);
        //if determinant is near zero, ray lies in plane of triangle or ray is parallel to plane of triangle
        double det = e1.dot(P);
        //NOT CULLING
        if(det > -EPSILON && det < EPSILON) {
            return false;
        }
        double inv_det = 1.d / det;
    
        //calculate distance from V1 to ray origin
        Vec3d T = new Vec3d(o);
        T.sub(p[0]);
    
        //Calculate u parameter and test bound
        double u = T.dot(P) * inv_det;
        //The intersection lies outside of the triangle
        if(u < 0.f || u > 1.f) {
            return false;
        }
    
        //Prepare to test v parameter
        Vec3d Q = new Vec3d();
        Q.cross(T, e1);
    
        //Calculate V parameter and test bound
        double v = d.dot(Q) * inv_det;
        //The intersection lies outside of the triangle
        if(v < 0.f || u + v  > 1.f) {
            return false;
        }
    
        double t = e2.dot(Q) * inv_det;
    
        if(t > EPSILON) { //ray intersection
            return true;
        }
        // No hit, no win
        return false;
    }

    private void computeBounds() {
        min = new float[dimensionality];
        max = new float[dimensionality];
        
        if(coordinates.length == 0) {
            Arrays.fill(min, Float.NaN);
            Arrays.fill(max, Float.NaN);
            return;
        }
        
        System.arraycopy(coordinates[0], 0, min, 0, dimensionality);
        System.arraycopy(coordinates[0], 0, max, 0, dimensionality);
        for(int i = 0; i < coordinates.length; i++) {
            for(int j = 0; j < dimensionality; j++) {
                min[j] = Math.min(min[j], coordinates[i][j]);
                max[j] = Math.max(max[j], coordinates[i][j]);
            }
        }
    }
    
    private float[] random() {
        float[] ret = new float[dimensionality];
        for(int i = 0; i < dimensionality; i++) {
            ret[i] = (float) (Math.random() * (max[i] - min[i]) + min[i]);
        }
        return ret;
    }
}
