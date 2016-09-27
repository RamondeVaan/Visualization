package nl.ramondevaan.visualization.point;

import nl.ramondevaan.visualization.core.Source;

import java.nio.FloatBuffer;

public abstract class PointGenerator extends Source<FloatBuffer> {

    private int numberOfPoints;

    public PointGenerator() {
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
    protected FloatBuffer updateImpl() throws Exception {
        checkValidity();
        FloatBuffer ret = FloatBuffer.allocate(numberOfPoints * 3);
        for(int i = 0; i < numberOfPoints; i++) {
            ret.position(i * 3);
            ret.limit(ret.position() + 3);
            generatePointImpl(ret.slice());
        }
        return ret.asReadOnlyBuffer();
    }
    
    protected abstract void generatePointImpl(FloatBuffer buffer);
    
    protected void checkValidity() {
        //Do nothing
    }
}
