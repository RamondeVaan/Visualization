package nl.ramondevaan.visualization.statistics;

import nl.ramondevaan.visualization.core.Filter;
import nl.ramondevaan.visualization.core.Source;
import nl.ramondevaan.visualization.mesh.Mesh;
import org.apache.commons.lang3.Validate;

import java.nio.DoubleBuffer;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.IntStream;

public class Hausdorff extends Filter<Mesh, Double> {
    public Hausdorff() {
        super(2);
    }

    public final void setMesh1(Source<Mesh> mesh) {
        setInput(0, mesh);
    }

    public final void setMesh2(Source<Mesh> mesh) {
        setInput(1, mesh);
    }

    @Override
    protected Double updateImpl() throws Exception {
        Mesh mesh1 = getInput(0);
        Mesh mesh2 = getInput(1);

        Validate.notNull(mesh1);
        Validate.notNull(mesh2);

        final int dimensionality = mesh1.dimensionality;
        if(dimensionality != mesh2.dimensionality) {
            throw new IllegalArgumentException("Meshes did were not equal in dimensionality");
        }

        List<DoubleBuffer> mesh1Coords = new ArrayList<>();
        List<DoubleBuffer> mesh2Coords = new ArrayList<>();
        for(int i = 0; i < dimensionality; i++) {
            mesh1Coords.add(mesh1.coordinates.get(i).type.parseDoubleBuffer(
                    mesh1.coordinates.get(i).getValues()));
            mesh2Coords.add(mesh2.coordinates.get(i).type.parseDoubleBuffer(
                    mesh2.coordinates.get(i).getValues()));
        }

        return Math.max(
                hausdorff(mesh1Coords, mesh2Coords,
                        mesh1.numberOfVertices, mesh2.numberOfVertices),
                hausdorff(mesh2Coords, mesh1Coords,
                        mesh2.numberOfVertices, mesh1.numberOfVertices)
        );
    }

    private static double hausdorff(List<DoubleBuffer> mesh1Coords,
                                    List<DoubleBuffer> mesh2Coords,
                                    int len1, int len2) {
        return IntStream.range(0, len1).parallel().mapToDouble(i1 ->
                IntStream.range(0, len2).sequential().mapToDouble(i2 ->
                        euclideanDistance(mesh1Coords, mesh2Coords, i1, i2)
                ).min().getAsDouble()
        ).max().orElseThrow(() ->
                new IllegalArgumentException("Error computing Hausdorff distance"));
    }

    private static double euclideanDistance(List<DoubleBuffer> mesh1Coords,
                                          List<DoubleBuffer> mesh2Coords,
                                          int i1, int i2) {
        double sumsq = 0d;

        for(int i = 0; i < mesh1Coords.size(); i++) {
            sumsq += Math.pow(mesh1Coords.get(i).get(i1) -
                    mesh2Coords.get(i).get(i2), 2);
        }

        return Math.sqrt(sumsq);
    }
}
