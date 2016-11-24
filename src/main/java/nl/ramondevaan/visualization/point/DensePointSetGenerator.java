package nl.ramondevaan.visualization.point;

import nl.ramondevaan.visualization.mesh.PropertyType;
import org.apache.commons.lang3.tuple.ImmutablePair;

import java.nio.ByteBuffer;
import java.nio.DoubleBuffer;
import java.nio.FloatBuffer;
import java.nio.IntBuffer;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.IntStream;

public class DensePointSetGenerator extends PointSetGenerator {
    private final static PropertyType VALUE_TYPE = PropertyType.DOUBLE;

    private DoubleBuffer    spacing;
    private DoubleBuffer    origin;
    private IntBuffer       size;

    public DensePointSetGenerator() {
        spacing = DoubleBuffer  .allocate(3);
        origin  = DoubleBuffer  .allocate(3);
        size    = IntBuffer     .allocate(3);

        while (spacing.hasRemaining()) {
            spacing .put(1.0d);
            origin  .put(0.0d);
            size    .put(1);
        }

        spacing .rewind();
        origin  .rewind();
        size    .rewind();
    }

    public final void setSpacing(float[] spacing) {
        setSpacing(FloatBuffer.wrap(spacing));
    }

    public final void setSpacing(double[] spacing) {
        setSpacing(DoubleBuffer.wrap(spacing));
    }

    public final void setSpacing(FloatBuffer spacing) {
        DoubleBuffer d = DoubleBuffer.allocate(spacing.remaining());
        FloatBuffer dupe = spacing.duplicate();

        while(dupe.hasRemaining()) {
            d.put(dupe.get());
        }
        d.rewind();
        setSpacing(d);
    }

    public final void setSpacing(DoubleBuffer spacing) {
        if(!this.spacing.equals(spacing)) {
            this.spacing = DoubleBuffer.allocate(spacing.remaining());
            this.spacing.put(spacing.duplicate());
            this.spacing.rewind();
            changed();
        }
    }

    public final void setOrigin(float[] origin) {
        setOrigin(FloatBuffer.wrap(origin));
    }

    public final void setOrigin(double[] origin) {
        setOrigin(DoubleBuffer.wrap(origin));
    }

    public final void setOrigin(FloatBuffer origin) {
        DoubleBuffer d = DoubleBuffer.allocate(origin.remaining());
        FloatBuffer dupe = origin.duplicate();

        while(dupe.hasRemaining()) {
            d.put(dupe.get());
        }
        d.rewind();
        setOrigin(d);
    }

    public final void setOrigin(DoubleBuffer origin) {
        if(!this.origin.equals(origin)) {
            this.origin = DoubleBuffer.allocate(origin.remaining());
            this.origin.put(origin.duplicate());
            this.origin.rewind();
            changed();
        }
    }

    public final void setSize(int[] size) {
        setSize(IntBuffer.wrap(size));
    }

    public final void setSize(IntBuffer size) {
        if(!this.size.equals(size)) {
            IntBuffer dupe = size.duplicate();
            while(dupe.hasRemaining()) {
                if(dupe.get() <= 0) {
                    throw new IllegalArgumentException("Size must be at least 1");
                }
            }

            this.size = IntBuffer.allocate(size.remaining());
            this.size.put(size.duplicate());
            this.size.rewind();
            changed();
        }
    }

    public final DoubleBuffer getSpacing() {
        return spacing.duplicate();
    }

    public final DoubleBuffer getOrigin() {
        return origin.duplicate();
    }

    public final IntBuffer getSize() {
        return size.duplicate();
    }

    @Override
    protected ImmutablePair<PropertyType, List<ByteBuffer>> generatePoints() {
        if(spacing.limit() < size.limit()) {
            throw new IllegalArgumentException("Spacing dimensionality was smaller than size dimensionality");
        }
        if(origin.limit() < size.limit()) {
            throw new IllegalArgumentException("Origin dimensionality was smaller than size dimensionality");
        }

        int numberOfValues = 1;

        while (size.hasRemaining()) {
            numberOfValues *= size.get();
        }

        size.rewind();

        List<ByteBuffer> ret = new ArrayList<>();

        for(int i = 0; i < size.limit(); i++) {
            ret.add(ByteBuffer.allocate(VALUE_TYPE.numberOfBytes * numberOfValues));
        }

        int curDim = 0;
        int[] cur = new int[size.limit()];
        int[] max = IntStream.range(0, size.capacity()).map(i -> size.get(i) - 1).toArray();

        outer: while(true) {
            IntStream.range(0, ret.size()).parallel().forEach(i ->
                    ret.get(i).putDouble(origin.get(i) + cur[i] * spacing.get(i))
            );
            while(true) {
                if(cur[curDim] < max[curDim]) {
                    cur[curDim]++;
                    curDim = 0;
                    break;
                } else {
                    cur[curDim] = 0;
                    curDim++;
                    if(curDim >= size.limit()) {
                        break outer;
                    }
                }
            }
        }

        return new ImmutablePair<>(VALUE_TYPE, ret);
    }
}
