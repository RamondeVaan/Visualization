package nl.ramondevaan.visualization.point;

import org.apache.commons.lang3.Validate;

public class RandomPointGenerator extends PointGenerator {
    
    private float[] min;
    private float[] max;
    
    public final void setBounds(float[] bounds) {
        if(bounds == null) {
            this.min = null;
            this.max = null;
            return;
        }
        int num = bounds.length;
        if(num % 2 != 0) {
            num -= 1;
        }
        final int dim = num / 2;
        min = new float[dim];
        max = new float[dim];
        int a1, a2;
        for(int i = 0; i < dim; i++) {
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
    }
    
    @Override
    protected float[] generatePointImpl() {
        float[] ret = new float[min.length];
        for(int i = 0; i < min.length; i++) {
            ret[i] = (float) (Math.random() * (max[i] - min[i]) + min[i]);
        }
        return ret;
    }
    
    @Override
    protected void checkValidity() {
        Validate.notNull(min, "No bounds were set");
    }
}
