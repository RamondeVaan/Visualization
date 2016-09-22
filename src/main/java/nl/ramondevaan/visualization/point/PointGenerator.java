package nl.ramondevaan.visualization.point;

public abstract class PointGenerator {
    public final float[] generatePoint() {
        checkValidity();
        return generatePointImpl();
    }
    
    public float[][] generatePoints(int numberOfPoints) {
        checkValidity();
        if(numberOfPoints <= 0) {
            return new float[0][];
        }
        
        float[][] ret = new float[numberOfPoints][];
        
        for(int i = 0; i < numberOfPoints; i++) {
            ret[i] = generatePointImpl();
        }
        
        return ret;
    }
    
    protected abstract float[] generatePointImpl();
    
    protected void checkValidity() {
        //Do nothing
    }
}
