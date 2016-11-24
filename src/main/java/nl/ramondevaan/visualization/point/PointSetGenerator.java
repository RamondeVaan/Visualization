package nl.ramondevaan.visualization.point;

import nl.ramondevaan.visualization.core.Source;
import nl.ramondevaan.visualization.mesh.*;
import org.apache.commons.lang3.tuple.ImmutablePair;

import java.nio.ByteBuffer;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.stream.IntStream;

public abstract class PointSetGenerator extends Source<Mesh> {
    private final static List<String> AXIS_NAMES = Collections.unmodifiableList(Arrays.asList(
            "x",
            "y",
            "z"
    ));
    private final static String VERTEX_NAME     = "vertex";
    private final static String FACE_NAME       = "face";
    private final static String INDICES_NAME    = "vertex_indices";

    @Override
    protected Mesh updateImpl() throws Exception {
        ImmutablePair<PropertyType, List<ByteBuffer>> ret = generatePoints();

        PropertyType        valueType   = ret.getLeft();
        List<ByteBuffer>    temp        = ret.getRight();
        List<Property>      properties  = new ArrayList<>();

        if(temp == null || temp.size() == 0) {
            throw new IllegalArgumentException("An empty set of points was generated");
        }
        if(!temp.parallelStream().allMatch(p -> p.capacity() == temp.get(0).capacity())) {
            throw new IllegalArgumentException("Not all bytebuffers were of equal size");
        }
        if(temp.get(0).capacity() % valueType.numberOfBytes != 0) {
            throw new IllegalArgumentException("Bytebuffer lengths were not divisible by property type byte length");
        }
        if(temp.size() > AXIS_NAMES.size()) {
            throw new UnsupportedOperationException("PointSetGenerator can (currently) " +
                    "only handle pointsets of dimensions up to " + AXIS_NAMES.size());
        }

        IntStream.range(0, temp.size()).forEachOrdered(i -> properties.add(
                new Property(AXIS_NAMES.get(i), valueType, temp.get(i))
        ));

        int         numberOfEntries = temp.get(0).capacity() / valueType.numberOfBytes;
        Element     vertex          = new Element(VERTEX_NAME, numberOfEntries, properties);

        Property indices = new ListProperty(INDICES_NAME, PropertyType.INT,
                ByteBuffer.allocate(0), PropertyType.INT, ByteBuffer.allocate(0));
        Element face = new Element(FACE_NAME, 0, Collections.singletonList(indices));

        return (new Mesh.Builder())
                .setElements(Arrays.asList(vertex, face))
                .setVertexId(0)
                .setFaceId(1)
                .setCoordIds(new int[] {0, 1, 2})
                .setIndicesId(0)
                .build();
    }

    protected abstract ImmutablePair<PropertyType, List<ByteBuffer>> generatePoints();
}
