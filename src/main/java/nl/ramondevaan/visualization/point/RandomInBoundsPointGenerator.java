package nl.ramondevaan.visualization.point;

import nl.ramondevaan.visualization.mesh.PropertyType;
import org.apache.commons.lang3.Validate;
import org.apache.commons.lang3.tuple.ImmutablePair;

import java.nio.ByteBuffer;
import java.util.ArrayList;
import java.util.List;

public class RandomInBoundsPointGenerator extends PointSetGenerator {
    private final static PropertyType VALUE_TYPE = PropertyType.DOUBLE;

    private int         numberOfPoints;
    private int         dimensionality;

    private double[]    min;
    private double[]    max;

    public RandomInBoundsPointGenerator() {
        numberOfPoints = 1;
    }

    public final void setNumberOfPoints(int numberOfPoints) {
        if(numberOfPoints != this.numberOfPoints) {
            if(numberOfPoints <= 0) {
                throw new IllegalArgumentException("Number of points must be larger than 1");
            }
            this.numberOfPoints = numberOfPoints;
            changed();
        }
    }

    public final int getNumberOfPoints() {
        return numberOfPoints;
    }

    public final void setBounds(double[] bounds) {
        if(bounds == null) {
            this.min = null;
            this.max = null;
            return;
        }
        int num = bounds.length;
        if(num % 2 != 0) {
            num -= 1;
        }
        dimensionality = num / 2;
        min = new double[dimensionality];
        max = new double[dimensionality];
        int a1, a2;
        for(int i = 0; i < dimensionality; i++) {
            a1 = 2 * i;
            a2 = a1 + 1;
            if(bounds[a1] > bounds[a2]) {
                int t = a1;
                a1 = a2;
                a2 = t;
            }
            min[i] = bounds[a1];
            max[i] = bounds[a2];
        }
        changed();
    }

    private final void generatePoint(List<ByteBuffer> coords) {
        for(int i = 0; i < coords.size(); i++) {
            coords.get(i).putDouble(Math.random() * (max[i] - min[i]) + min[i]);
        }
    }

    @Override
    protected ImmutablePair<PropertyType, List<ByteBuffer>> generatePoints() {
        Validate.notNull(min, "No bounds were set");

        List<ByteBuffer> coords = new ArrayList<>();
        for (int i = 0; i < dimensionality; i++) {
            coords.add(ByteBuffer.allocate(VALUE_TYPE.numberOfBytes * numberOfPoints));
        }

        for(int i = 0; i < numberOfPoints; i++) {
            generatePoint(coords);
        }

        return new ImmutablePair<>(VALUE_TYPE, coords);
    }
}
