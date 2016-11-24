package nl.ramondevaan.visualization.point;

import com.sun.javafx.geom.Vec3d;
import nl.ramondevaan.visualization.mesh.Mesh;
import nl.ramondevaan.visualization.mesh.PropertyType;
import org.apache.commons.lang3.Validate;
import org.apache.commons.lang3.tuple.ImmutablePair;

import java.nio.*;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public class RandomInMeshPointGenerator extends PointSetGenerator {
    private final static double         EPSILON     = 0.000001d;
    private final static PropertyType   VALUE_TYPE  = PropertyType.DOUBLE;

    private int numberOfPoints;
    
    private Mesh mesh;
    private boolean meshChanged;
    private double[] min;
    private double[] max;
    private Vec3d[][] triangles;

    public RandomInMeshPointGenerator() {
        meshChanged     = true;
        numberOfPoints  = 1;
    }

    public final void setNumberOfPoints(int numberOfPoints) {
        if(numberOfPoints != this.numberOfPoints) {
            if(numberOfPoints <= 0) {
                throw new IllegalArgumentException("Number of points must be larger than 1");
            }
            this.numberOfPoints = numberOfPoints;
            changed();
        }
    }

    public final int getNumberOfPoints() {
        return numberOfPoints;
    }

    public final void setMesh(Mesh mesh) {
        this.mesh = mesh;
        meshChanged = true;
        changed();
    }

    @Override
    protected final ImmutablePair<PropertyType, List<ByteBuffer>> generatePoints() {
        checkValidity();

        List<ByteBuffer> coords = new ArrayList<>();
        for(int i = 0; i < mesh.dimensionality; i++) {
            coords.add(ByteBuffer.allocate(VALUE_TYPE.numberOfBytes * numberOfPoints));
        }

        for(int i = 0; i < numberOfPoints; i++) {
            generatePoint(coords);
        }

        return new ImmutablePair<>(VALUE_TYPE, coords);
    }

    protected final void checkValidity() {
        if(meshChanged) {
            Validate.notNull(mesh);
            if(mesh.dimensionality != 3) {
                throw new IllegalArgumentException("Only 3d meshes are (currently) supported");
            }

            triangles = new Vec3d[mesh.numberOfFaces][3];

            List<DoubleBuffer> coordinates = new ArrayList<>();
            for(int i = 0; i < mesh.dimensionality; i++) {
                coordinates.add(mesh.coordinates.get(i).type.parseDoubleBuffer(
                        mesh.coordinates.get(i).getValues()));
            }

            IntBuffer numVert   = mesh.getNumVerticesInFaces();
            IntBuffer vert      = mesh.getVerticesInFaces();

            int i;
            for(int f = 0; numVert.hasRemaining(); f++) {
                final int num = numVert.get();
                if(num != 3) {
                    throw new IllegalArgumentException(
                            "Only triangular faces are currently supported");
                }
                for(i = 0; i < num; i++) {
                    final int vertexIndex = vert.get();
                    triangles[f][i] = new Vec3d(
                            coordinates.get(0).get(vertexIndex),
                            coordinates.get(1).get(vertexIndex),
                            coordinates.get(2).get(vertexIndex)
                    );
                }
            }
            computeBounds(coordinates);

            meshChanged = false;
        }
    }

    protected final void generatePoint(List<ByteBuffer> coords) {
        double[] buffer = new double[coords.size()];
        do {
            random(buffer);
        }
        while(!contains(buffer));

        for(int i = 0; i < coords.size(); i++) {
            coords.get(i).putDouble(buffer[i]);
        }
    }

    private boolean contains(double[] pos) {
        Vec3d origin = new Vec3d(pos[0], pos[1], pos[2]);
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

    private void computeBounds(List<DoubleBuffer> coordinates) {
        coordinates.forEach(Buffer::rewind);

        min = new double[coordinates.size()];
        max = new double[coordinates.size()];
        
        if(coordinates.get(0).limit() == 0) {
            Arrays.fill(min, Double.NaN);
            Arrays.fill(max, Double.NaN);
            return;
        }

        for(int i = 0; i < coordinates.size(); i++) {
            min[i] = coordinates.get(i).get();
        }
        System.arraycopy(min, 0, max, 0, coordinates.size());

        double val;
        while(coordinates.get(0).hasRemaining()) {
            for(int i = 0; i < coordinates.size(); i++) {
                val = coordinates.get(i).get();
                min[i] = Math.min(min[i], val);
                max[i] = Math.max(max[i], val);
            }
        }
    }

    private void random(double[] buffer) {
        for(int i = 0; i < buffer.length; i++) {
            buffer[i] = Math.random() * (max[i] - min[i]) + min[i];
        }
    }
}
