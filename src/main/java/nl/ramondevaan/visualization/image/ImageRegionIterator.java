package nl.ramondevaan.visualization.image;

import java.nio.ByteBuffer;
import java.util.Arrays;

public class ImageRegionIterator {
    private final int dimensionality;
    private final int dmin1;
    private final Image image;
    private final int[] min;
    private final int[] max;
    private final int[] cur;
    private final int[] skip;
    private final ByteBuffer values;
    private int curDim;
    
    public ImageRegionIterator(Image image, int[] region) {
        this.dimensionality = image.dimensionality;
        this.dmin1 = this.dimensionality - 1;
        this.image = image;
        if(region.length != image.extent.length) {
            throw new IllegalArgumentException("Region dimensionality did not match image dimensionality");
        }
        this.min = new int[dimensionality];
        this.max = new int[dimensionality];
        this.cur = new int[dimensionality];
        this.skip = new int[dimensionality];
        int a1, a2;
        for(int i = 0; i < dimensionality; i++) {
            a1 = 2 * i;
            a2 = a1 + 1;
            if(region[a1] > region[a2]) {
                min[i] = region[a2];
                max[i] = region[a1];
            } else {
                min[i] = region[a1];
                max[i] = region[a2];
            }
            if(min[i] < 0 || max[i] >= image.dimensions[i]) {
                throw new IllegalArgumentException("Region was out of bounds");
            }
        }
        System.arraycopy(min, 0, cur, 0, dimensionality);
        int num;
        skip[0] = image.dataType.numBytes;
        for(int i = 0; i < dmin1; i++) {
            num = 1;
            for(int j = 0; j < i; j++) {
                num *= image.dimensions[j];
            }
            System.out.println((min[i] + image.dimensions[i] - max[i] - 1));
            skip[i + 1] = (min[i] + image.dimensions[i] - max[i] - 1) * num * image.dataType.numBytes;
        }
        System.out.println("MIN: " + Arrays.toString(min));
        System.out.println("MAX: " + Arrays.toString(max));
        System.out.println("PRESKIP: " + Arrays.toString(skip));
        for(int i = 1; i < skip.length; i++) {
            skip[i] += skip[i - 1];
        }
        values = image.values;
        values.position(locAtPos(min));
        curDim = 0;
        System.out.println("DIME: " + Arrays.toString(image.dimensions));
        System.out.println("REGI: " + Arrays.toString(region));
        System.out.println("SKIP: " + Arrays.toString(skip));
    }
    
    private int locAtPos(int[] pos) {
        int ret = 0;
        int num = 1;
        for(int i = 0; i < dimensionality; i++) {
            num *= image.dimensions[i];
            ret += num * pos[i];
        }
        ret *= image.dataType.numBytes;
        return ret;
    }
    
    public final int[] getIndex() {
        int[] ret = new int[dimensionality];
        System.arraycopy(cur, 0, ret, 0, dimensionality);
        return ret;
    }
    
    public final ByteBuffer get() {
        return (ByteBuffer) values.slice()
                .asReadOnlyBuffer()
                .limit(image.dataType.numBytes);
    }
    
    public final void set(ByteBuffer buffer) {
        values.mark();
        values.put((ByteBuffer) buffer.rewind().limit(image.dataType.numBytes));
        values.reset();
    }
    
    public final boolean hasNext() {
        return curDim < dimensionality;
    }
    
    public final void next() {
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
                return;
            }
        }

        System.out.println(curLoc);
        values.position(curLoc);
    }
}
