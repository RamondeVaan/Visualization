package nl.ramondevaan.visualization.point;

import com.sun.javafx.geom.Vec3d;
import nl.ramondevaan.visualization.mesh.Mesh;
import org.apache.commons.lang3.Validate;

import java.nio.FloatBuffer;
import java.nio.IntBuffer;
import java.util.Arrays;

public class MeshInternalPointGenerator extends PointGenerator {
    private final static double EPSILON = 0.000001d;
    
    private Mesh mesh;
    private boolean meshChanged;
    private float[] min;
    private float[] max;
    private Vec3d[][] triangles;

    public MeshInternalPointGenerator() {
        meshChanged = true;
    }

    public final void setMesh(Mesh mesh) {
        this.mesh = mesh;
        meshChanged = true;
        changed();
    }
    
    @Override
    protected void checkValidity() {
        if(meshChanged) {
            Validate.notNull(mesh);
            triangles = new Vec3d[mesh.numberOfFaces][3];

            FloatBuffer coordinates = mesh.getCoordinates();
            IntBuffer faces = mesh.getFaces();

            int i;
            for(int f = 0; faces.hasRemaining(); f++) {
                if(faces.get() != 3) {
                    throw new IllegalArgumentException(
                            "Only triangular faces are currently supported");
                }
                for(i = 0; i < 3; i++) {
                    coordinates.position(faces.get() * 3);
                    triangles[f][i] = new Vec3d(
                            coordinates.get(),
                            coordinates.get(),
                            coordinates.get()
                    );
                }
            }
            computeBounds(coordinates);

            meshChanged = false;
        }
    }
    
    @Override
    protected void generatePointImpl(FloatBuffer buffer) {
        while(!contains(buffer)) {
            random(buffer);
        }
    }
    
    private boolean contains(FloatBuffer buffer) {
        buffer.rewind();
        Vec3d origin = new Vec3d(buffer.get(),
                buffer.get(), buffer.get());
        Vec3d dir = new Vec3d(0, 0, 1);
        
        int count = 0;
        for(Vec3d[] triangle : triangles) {
            if(intersects(origin, dir, triangle)) {
                count++;
            }
        }
        
        return count % 2 == 1;
    }
    
    private static boolean intersects(Vec3d o, Vec3d d, Vec3d[] p) {
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

    private void computeBounds(FloatBuffer coordinates) {
        coordinates.rewind();

        min = new float[3];
        max = new float[3];
        
        if(coordinates.limit() == 0) {
            Arrays.fill(min, Float.NaN);
            Arrays.fill(max, Float.NaN);
            return;
        }

        min[0] = coordinates.get();
        min[1] = coordinates.get();
        min[2] = coordinates.get();
        System.arraycopy(min, 0, max, 0, 3);
        float x, y, z;
        while(coordinates.hasRemaining()) {
            x = coordinates.get();
            y = coordinates.get();
            z = coordinates.get();

            min[0] = Math.min(min[0], x);
            min[1] = Math.min(min[1], y);
            min[2] = Math.min(min[2], z);

            max[0] = Math.max(max[0], x);
            max[1] = Math.max(max[1], y);
            max[2] = Math.max(max[2], z);
        }
    }
    
    private void random(FloatBuffer buffer) {
        buffer.rewind();
        for(int i = 0; buffer.hasRemaining(); i++) {
            buffer.put((float) (Math.random() * (max[i] - min[i]) + min[i]));
        }
    }
}
