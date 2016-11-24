package nl.ramondevaan.visualization.statistics;

import nl.ramondevaan.visualization.core.Filter;
import nl.ramondevaan.visualization.core.Source;
import nl.ramondevaan.visualization.mesh.Mesh;
import org.apache.commons.lang3.Validate;

import java.io.IOException;
import java.nio.DoubleBuffer;
import java.nio.FloatBuffer;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.IntStream;

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

        if(mesh1.numberOfVertices != mesh2.numberOfVertices) {
            throw new IllegalArgumentException("Meshes differed in number of points");
        }
        if(mesh1.dimensionality != mesh2.dimensionality) {
            throw new IllegalArgumentException("Meshes differed in dimensionality");
        }

        List<DoubleBuffer> mesh1Coords = new ArrayList<>();
        List<DoubleBuffer> mesh2Coords = new ArrayList<>();
        for(int i = 0; i < mesh1.dimensionality; i++) {
            mesh1Coords.add(mesh1.coordinates.get(i).type.parseDoubleBuffer(
                    mesh1.coordinates.get(i).getValues()));
            mesh2Coords.add(mesh2.coordinates.get(i).type.parseDoubleBuffer(
                    mesh2.coordinates.get(i).getValues()));
        }
    
        DoubleBuffer ret = DoubleBuffer.allocate(mesh1.numberOfVertices);

        IntStream.range(0, mesh1.numberOfVertices)
                .parallel()
                .forEach(i -> {
                    double sumsq = 0d;

                    for(int j = 0; j < mesh1Coords.size(); j++) {
                        sumsq += Math.pow(mesh1Coords.get(j).get(i) -
                                mesh2Coords.get(j).get(i), 2);
                    }

                    ret.put(i, Math.sqrt(sumsq));
                });
    
        return ret.asReadOnlyBuffer();
    }
}
