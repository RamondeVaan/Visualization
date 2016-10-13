package nl.ramondevaan.visualization.point;

import nl.ramondevaan.visualization.core.Source;
import nl.ramondevaan.visualization.utilities.DataUtils;

import java.nio.FloatBuffer;

public abstract class PointGenerator extends Source<FloatBuffer> {
    @Override
    protected final FloatBuffer updateImpl() throws Exception {
        checkValidity();
        return generatePoints();
    }

    FloatBuffer generatePoints() {
        return DataUtils.clone(generatePointsImpl());
    }

    protected abstract FloatBuffer generatePointsImpl();
    
    protected void checkValidity() {
        //Do nothing
    }
}
