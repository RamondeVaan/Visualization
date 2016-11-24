package nl.ramondevaan.visualization.mesh;

import nl.ramondevaan.visualization.core.Filter;
import nl.ramondevaan.visualization.core.Source;
import nl.ramondevaan.visualization.utilities.DataUtils;
import org.apache.commons.lang3.ArrayUtils;

import java.nio.ByteBuffer;
import java.nio.DoubleBuffer;
import java.nio.FloatBuffer;
import java.nio.IntBuffer;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.stream.IntStream;

public class MeshShift extends Filter<Mesh, Mesh> {
    private final static PropertyType VALUE_TYPE = PropertyType.DOUBLE;

    private DoubleBuffer shift;
    
    public MeshShift() {
        super(1);

        shift = DoubleBuffer.allocate(3);

        while(shift.hasRemaining()) {
            shift.put(0d);
        }

        shift.rewind();
    }
    
    public final void setInput(Source<Mesh> mesh) {
        setInput(0, mesh);
    }

    public final void setShift(float[] shift) {
        setShift(FloatBuffer.wrap(shift));
    }

    public final void setShift(double[] shift) {
        setShift(DoubleBuffer.wrap(shift));
    }

    public final void setShift(FloatBuffer shift) {
        DoubleBuffer d = DoubleBuffer.allocate(shift.remaining());
        FloatBuffer dupe = shift.duplicate();

        while(dupe.hasRemaining()) {
            d.put(dupe.get());
        }
        d.rewind();
        setShift(d);
    }

    public final void setShift(DoubleBuffer shift) {
        if(!this.shift.equals(shift)) {
            this.shift = DoubleBuffer.allocate(shift.remaining());
            this.shift.put(shift.duplicate());
            this.shift.rewind();
            changed();
        }
    }
    
    @Override
    protected Mesh updateImpl() throws Exception {
        Mesh input = getInput(0);

        if(shift.remaining() == 0 || allZero(shift)) {
            return input;
        }

        if(shift.remaining() > input.dimensionality) {
            throw new IllegalArgumentException("Shift dimensionality was larger than mesh dimensionality");
        }

        IntBuffer coordPropInd = input.getCoordinatePropertyIndices();

        List<DoubleBuffer>  coords      = new ArrayList<>();
        List<ByteBuffer>    newCoords   = new ArrayList<>();
        for(int i = 0; i < shift.remaining(); i++) {
            coords.add(input.coordinates.get(i).type.parseDoubleBuffer(
                    input.coordinates.get(i).getValues()));
            newCoords.add(ByteBuffer.allocate(VALUE_TYPE.numberOfBytes * input.numberOfVertices));
        }

        IntStream.range(0, input.numberOfVertices)
                .parallel()
                .forEach(i -> {
                    for(int j = 0; j < coords.size(); j++) {
                        newCoords.get(j).putDouble(i * VALUE_TYPE.numberOfBytes,
                                coords.get(j).get(i) + shift.get(j));
                    }
                });

        List<Property> vertexProperties = new ArrayList<>();
        coordPropInd.rewind();

        vertexProperties.addAll(input.vertexElement.properties);
        for(int i = 0; coordPropInd.hasRemaining(); i++) {
            int k = coordPropInd.get();
            vertexProperties.set(k, new Property(
                    vertexProperties.get(k).name,
                    newCoords.get(i), VALUE_TYPE));
        }

        List<Element> elements = new ArrayList<>();
        elements.addAll(input.elements);
        elements.set(input.vertexElementIndex, new Element(
                input.vertexElement.name, vertexProperties,
                input.numberOfVertices));

        return (new Mesh.Builder())
                .setElements(elements)
                .setVertexId(input.vertexElementIndex)
                .setFaceId(input.faceElementIndex)
                .setIndicesId(input.vertexIndicesIndex)
                .setCoordIds(input.getCoordinatePropertyIndices())
                .setNormalIds(input.getNormalPropertyIndices(), false)
                .build();
    }

    private static boolean allZero(DoubleBuffer f) {
        DoubleBuffer t = f.duplicate();

        while(t.hasRemaining()) {
            if(Math.abs(t.get()) > 0.000000001d) {
                return false;
            }
        }

        return true;
    }
}
