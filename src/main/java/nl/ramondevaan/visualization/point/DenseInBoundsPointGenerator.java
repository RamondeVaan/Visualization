package nl.ramondevaan.visualization.point;

import org.apache.commons.lang3.Validate;

import java.nio.FloatBuffer;
import java.util.Arrays;

public class DenseInBoundsPointGenerator extends PointGenerator {
    private float[] spacing;
    private float[] origin;
    private int[]   size;

    public DenseInBoundsPointGenerator() {
        spacing = new float[] {1f, 1f, 1f};
        origin  = new float[] {0f, 0f, 0f};
        size    = new int[] {1, 1, 1};
    }

    public final void setSpacing(float[] spacing) {
        Validate.notNull(spacing);
        if(spacing.length != 3) {
            throw new IllegalArgumentException("Array length has to be equal to 3");
        }
        if(!Arrays.equals(spacing, this.spacing)) {
            System.arraycopy(spacing, 0, this.spacing, 0, 3);
            changed();
        }
    }

    public final void setOrigin(float[] origin) {
        Validate.notNull(origin);
        if(origin.length != 3) {
            throw new IllegalArgumentException("Array length has to be equal to 3");
        }
        if(!Arrays.equals(origin, this.origin)) {
            System.arraycopy(origin, 0, this.origin, 0, 3);
            changed();
        }
    }

    public final void setSize(int[] size) {
        Validate.notNull(size);
        if(size.length != 3) {
            throw new IllegalArgumentException("Array length has to be equal to 3");
        }
        if(!Arrays.equals(size, this.size)) {
            for(int i = 0; i < 3; i++) {
                if(size[i] < 1) {
                    throw new IllegalArgumentException("Size must be larger than 0");
                }
            }
            System.arraycopy(size, 0, this.size, 0, 3);
            changed();
        }
    }

    @Override
    FloatBuffer generatePoints() {
        return generatePointsImpl();
    }

    @Override
    protected FloatBuffer generatePointsImpl() {
        int numCoords = 1;

        for(int i = 0; i < size.length; i++) {
            numCoords *= size[i];
        }

        FloatBuffer f = FloatBuffer.allocate(numCoords * 3);

        float[] pos = new float[3];
        System.arraycopy(origin, 0, pos, 0, 3);
        for(int x = 0; x < size[0]; x++) {
            for(int y = 0; y < size[1]; y++) {
                for(int z = 0; z < size[2]; z++) {
                    f.put(pos[0]);
                    f.put(pos[1]);
                    f.put(pos[2]);

                    pos[2] += spacing[2];
                }
                pos[1] += spacing[1];
                pos[2] = origin[2];
            }
            pos[0] += spacing[0];
            pos[1] = origin[1];
        }

        return f;
    }

    @Override
    protected final void checkValidity() {
        //Do nothing
    }

}
