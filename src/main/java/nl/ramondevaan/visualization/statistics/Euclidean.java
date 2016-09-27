package nl.ramondevaan.visualization.statistics;

import nl.ramondevaan.visualization.core.Filter;
import nl.ramondevaan.visualization.core.Source;
import nl.ramondevaan.visualization.mesh.Mesh;
import org.apache.commons.lang3.Validate;

import java.io.IOException;
import java.nio.DoubleBuffer;
import java.nio.FloatBuffer;

public class Euclidean extends Filter<Mesh, DoubleBuffer> {
    public Euclidean() {
        super(2);
    }

    public final void setMesh1(Source<Mesh> mesh) {
        setInput(0, mesh);
    }
    
    public final void setMesh2(Source<Mesh> mesh) {
        setInput(1, mesh);
    }

    @Override
    protected DoubleBuffer updateImpl() throws Exception {
        Mesh mesh1 = getInput(0);
        Mesh mesh2 = getInput(1);

        Validate.notNull(mesh1);
        Validate.notNull(mesh2);

        if(mesh1.numberOfCoordinates != mesh2.numberOfCoordinates) {
            throw new IllegalArgumentException("Meshes differed in number of points");
        }

        FloatBuffer coords1 = mesh1.getCoordinates();
        FloatBuffer coords2 = mesh2.getCoordinates();
        coords1.rewind();
        coords2.rewind();
    
        DoubleBuffer ret = DoubleBuffer.allocate(mesh1.numberOfCoordinates);
        double sumsq;
    
        while(coords1.hasRemaining()) {
            sumsq = 0d;
        
            for(int j = 0; j < 3; j++) {
                sumsq += Math.pow(coords1.get() - coords2.get(), 2);
            }
        
            ret.put(Math.sqrt(sumsq));
        }
    
        return ret.asReadOnlyBuffer();
    }
}
