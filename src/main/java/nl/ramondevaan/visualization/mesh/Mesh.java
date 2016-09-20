package nl.ramondevaan.visualization.mesh;

public class Mesh {
    final int dimensionality;
    float[][] coordinates;
    int[][] faces;
    
    public Mesh(int dimensionality, float[][] coordinates, int[][] faces) {
        this.dimensionality = dimensionality;
        this.coordinates = coordinates;
        this.faces = faces;
    }
    
    public final int getDimensionality() {
        return dimensionality;
    }
    
    public final float[][] getCoordinates() {
        return coordinates;
    }
    
    public final int[][] getFaces() {
        return faces;
    }
}
