package nl.ramondevaan.visualization.point;

import nl.ramondevaan.visualization.core.Filter;
import nl.ramondevaan.visualization.core.Source;
import nl.ramondevaan.visualization.mesh.Mesh;

import java.nio.FloatBuffer;

public class PointsetToMeshFilter extends Filter<FloatBuffer, Mesh> {
    public PointsetToMeshFilter() {
        super(1);
    }

    public final void setInput(Source<FloatBuffer> source) {
        setInput(0, source);
    }

    @Override
    protected Mesh updateImpl() throws Exception {
        FloatBuffer f = getInput(0);
        f.limit(f.capacity());
        if(f.capacity() % 3 != 0) {
            throw new IllegalArgumentException("Number of values in buffer was incorrect");
        }
        return new Mesh(f.capacity() / 3, f, new int[0][]);
    }
}
