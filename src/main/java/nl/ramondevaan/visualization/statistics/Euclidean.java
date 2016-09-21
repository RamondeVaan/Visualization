package nl.ramondevaan.visualization.statistics;

import nl.ramondevaan.visualization.mesh.Mesh;
import org.apache.commons.lang3.Validate;

import java.io.IOException;

public class Euclidean extends ValueSource {
    private Mesh mesh1;
    private Mesh mesh2;
    
    public Euclidean() {
        super("Euclidean", 0);
    }
    
    public final void setMesh1(Mesh mesh1) {
        this.mesh1 = mesh1;
        changed();
    }
    
    public final void setMesh2(Mesh mesh2) {
        this.mesh2 = mesh2;
        changed();
    }
    
    @Override
    protected double[] computeValues() throws IOException {
        Validate.notNull(mesh1);
        Validate.notNull(mesh2);
        
        int dimensionality = mesh1.getDimensionality();
        if(dimensionality != mesh2.getDimensionality()) {
            throw new IllegalArgumentException("Meshes differed in dimensionality");
        }
        float[][] coords1 = mesh1.getCoordinates();
        float[][] coords2 = mesh2.getCoordinates();
        if(coords1.length != coords2.length) {
            throw new IllegalArgumentException("Meshes differed in number of points");
        }
    
        double[] ret = new double[coords1.length];
        double sumsq;
    
        for(int i = 0; i < coords1.length; i++) {
            sumsq = 0d;
        
            for(int j = 0; j < dimensionality; j++) {
                sumsq += Math.pow(coords1[i][j] - coords2[i][j], 2d);
            }
        
            ret[i] = Math.sqrt(sumsq);
        }
    
        return ret;
    }
}
