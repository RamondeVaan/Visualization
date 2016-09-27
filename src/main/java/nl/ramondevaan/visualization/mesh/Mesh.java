package nl.ramondevaan.visualization.mesh;

import nl.ramondevaan.visualization.utilities.DataUtils;
import org.apache.commons.lang3.ArrayUtils;
import org.apache.commons.lang3.Validate;

import java.nio.FloatBuffer;
import java.nio.IntBuffer;
import java.util.Arrays;

public class Mesh {
    public final int dimensionality = 3;
    public final int numberOfCoordinates;
    public final int numberOfFaces;
    FloatBuffer coordinates;
    FloatBuffer coordinatesRead;
    IntBuffer faces;
    IntBuffer facesRead;

    public Mesh(int numberOfCoordinates, FloatBuffer coordinates, int[][] faces) {
        Validate.notNull(coordinates);
        Validate.notNull(faces);
        if(numberOfCoordinates < 0) {
            throw new IllegalArgumentException("Number of coordinates must be 0 or greater");
        }
        if(coordinates.limit() != numberOfCoordinates * 3) {
            throw new IllegalArgumentException("Length of coordinates array was incorrect");
        }

        this.numberOfCoordinates = numberOfCoordinates;
        this.numberOfFaces = faces.length;

        this.coordinates = DataUtils.clone(coordinates);
        this.coordinatesRead = this.coordinates.asReadOnlyBuffer();

        this.faces = getFacesBuffer(faces);
        this.facesRead = this.faces.asReadOnlyBuffer();
    }

    public Mesh(float[] coordinates, int numberOfCoordinates, int[][] faces) {
        Validate.notNull(coordinates);
        Validate.notNull(faces);
        if(numberOfCoordinates < 0) {
            throw new IllegalArgumentException("Number of coordinates must be 0 or greater");
        }
        final int numTimes3 = numberOfCoordinates * 3;
        if(coordinates.length < numTimes3) {
            throw new IllegalArgumentException("Length of vertices array was insufficient");
        }
        
        this.numberOfCoordinates = numberOfCoordinates;
        this.numberOfFaces = faces.length;

        this.faces = getFacesBuffer(faces);
        this.facesRead = this.faces.asReadOnlyBuffer();
        
        float[] c = Arrays.copyOf(coordinates, numTimes3);
        this.coordinates = FloatBuffer.wrap(c);
        this.coordinatesRead = this.coordinates.asReadOnlyBuffer();
    }
    
    Mesh(FloatBuffer coordinates, int numberOfCoordinates, int[][] faces) {
        this.numberOfCoordinates = numberOfCoordinates;
        this.numberOfFaces = faces.length;
        this.faces = getFacesBuffer(faces);
        this.facesRead = this.faces.asReadOnlyBuffer();
        this.coordinates = coordinates;
        this.coordinatesRead = this.coordinates.asReadOnlyBuffer();
    }
    
    Mesh(FloatBuffer coordinates, int numberOfCoordinates, IntBuffer faces, int numberOfFaces) {
        this.numberOfCoordinates = numberOfCoordinates;
        this.numberOfFaces = numberOfFaces;
        this.coordinates = coordinates;
        this.coordinatesRead = this.coordinates.asReadOnlyBuffer();
        this.faces = faces;
        this.facesRead = this.faces.asReadOnlyBuffer();
    }
    
    public final int getNumberOfCoordinates() {
        return numberOfCoordinates;
    }
    
    public final int getNumberOfFaces() {
        return numberOfFaces;
    }
    
    public final FloatBuffer getCoordinates() {
        return coordinatesRead;
    }
    
    public final IntBuffer getFaces() {
        return facesRead;
    }
    
    public final Mesh copy() {
        return new Mesh(
                DataUtils.clone(coordinates),
                this.numberOfCoordinates,
                DataUtils.clone(faces),
                this.numberOfFaces
        );
    }

    private static IntBuffer getFacesBuffer(int[][] faces) {
        int n = 0;
        for(int i = 0; i < faces.length; i++) {
            n += faces[i].length + 1;
        }
        IntBuffer ret = IntBuffer.allocate(n);
        for(int i = 0; i < faces.length; i++) {
            ret.put(faces[i].length);
            for(int j = 0; j < faces[i].length; j++) {
                ret.put(faces[i][j]);
            }
        }
        return ret;
    }
}