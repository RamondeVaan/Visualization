package nl.ramondevaan.visualization.point;

import nl.ramondevaan.visualization.utilities.DataUtils;

import java.nio.FloatBuffer;

public abstract class RandomPointGenerator extends PointGenerator {
    private int numberOfPoints;

    public RandomPointGenerator() {
        numberOfPoints = 1;
    }

    public final void setNumberOfPoints(int i) {
        if(i <= 0) {
            throw new IllegalArgumentException("Must generate at least 1 point");
        }
        this.numberOfPoints = i;
        changed();
    }

    @Override
    FloatBuffer generatePoints() {
        return generatePointsImpl();
    }

    @Override
    protected final FloatBuffer generatePointsImpl() {
        FloatBuffer ret = FloatBuffer.allocate(numberOfPoints * 3);
        for(int i = 0; i < numberOfPoints; i++) {
            ret.position(i * 3);
            ret.limit(ret.position() + 3);
            generatePoint(ret.slice());
        }
        return ret.asReadOnlyBuffer();
    }

    protected abstract void generatePoint(FloatBuffer buffer);
}
