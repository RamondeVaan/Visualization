package nl.ramondevaan.visualization.image;

import java.nio.ByteBuffer;
import java.nio.IntBuffer;
import java.util.Iterator;

public class ImageRegionIterator implements Iterator<ByteBuffer> {
    private final int           dimensionality;
    private final int           valueLength;
    private final int[]         min;
    private final int[]         max;
    private final int[]         cur;
    private final int[]         skip;
    private final IntBuffer     dimensions;
    private final ByteBuffer    values;
    
    private int                 curDim;
    
    public ImageRegionIterator(Image image, int[] region) {
        this.dimensionality = image.dimensionality;
        final int dmin1 = this.dimensionality - 1;
        this.dimensions     = image.getDimensions();
        
        if(region.length != dimensionality * 2) {
            throw new IllegalArgumentException("Region dimensionality did not match image dimensionality");
        }
        this.min    = new int[dimensionality];
        this.max    = new int[dimensionality];
        this.cur    = new int[dimensionality];
        this.skip   = new int[dimensionality];
        
        dimensions.rewind();
        for(int i = 0; i < dimensionality; i++) {
            if(region[2 * i] > region[2 * i + 1]) {
                min[i] = region[2 * i + 1];
                max[i] = region[2 * i];
            } else {
                min[i] = region[2 * i];
                max[i] = region[2 * i + 1];
            }
            if(min[i] < 0 || max[i] >= dimensions.get()) {
                throw new IllegalArgumentException("Region was out of bounds");
            }
        }
        
        System.arraycopy(min, 0, cur, 0, dimensionality);
        valueLength = image.componentType.numberOfBytes * image.dataDimensionality;
        int num;
        skip[0] = valueLength;
        for(int i = 0; i < dmin1; i++) {
            num = 1;
            for(int j = 0; j < i; j++) {
                num *= dimensions.get(j);
            }
            skip[i + 1] = (min[i] + dimensions.get(i) - max[i] - 1) * num * valueLength;
        }
        for(int i = 1; i < skip.length; i++) {
            skip[i] += skip[i - 1];
        }
        values = image.getValues();
        values.position(locAtPos(min));
        curDim = 0;
    }
    
    private int locAtPos(int[] pos) {
        int ret = 0;
        int num = 1;
        dimensions.rewind();
        for(int i = 0; i < dimensionality; i++) {
            ret += num * pos[i];
            num *= dimensions.get();
        }
        ret *= valueLength;
        return ret;
    }
    
    public final int[] getIndex() {
        int[] ret = new int[dimensionality];
        System.arraycopy(cur, 0, ret, 0, dimensionality);
        return ret;
    }
    
    public final boolean hasNext() {
        return curDim < dimensionality;
    }
    
    public final ByteBuffer next() {
        values.limit(values.position() + valueLength);
        ByteBuffer returnValue = values.slice();
        values.limit(values.capacity());
        
        int curLoc = values.position();
        while(true) {
            if (cur[curDim] < max[curDim]) {
                cur[curDim]++;
                curLoc += skip[curDim];
                curDim = 0;
                break;
            } else {
                cur[curDim] = min[curDim];
                curDim++;
            }
            if(curDim >= dimensionality) {
                return returnValue;
            }
        }

        values.position(curLoc);
        return returnValue;
    }
}
