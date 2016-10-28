package nl.ramondevaan.visualization.image;

import nl.ramondevaan.visualization.data.DataType;
import org.apache.commons.lang3.Validate;

import java.nio.ByteBuffer;

public class ImageRegionIterator {
    private final int dimensionality;
    private final int dmin1;
    private final int dataLength;
    private final int[] min;
    private final int[] max;
    private final int[] cur;
    private final int[] skip;
    private final ByteBuffer values;
    private int curDim;

    public ImageRegionIterator(DataType dataType, int dataDimensionality,
                               int[] dimensions, int[] region) {
        Validate.notNull(dataType);
        Validate.notNull(dimensions)
        Validate.notNull(region);
        if(dataDimensionality < 1) {
            throw new IllegalArgumentException("Data dimensionality must be 1 or larger");
        }
        if(dimensions.length < 1) {
            throw new IllegalArgumentException("Dimensionality must be 1 or larger");
        }
        if((dimensions.length * 2) != region.length) {
            throw new IllegalArgumentException("Region dimensions were not compatible with given dimensions");
        }
        this.dimensionality = dimensions.length;
        this.dmin1 = this.dimensionality - 1;
        this.dataLength = dataType.numBytes * dataDimensionality;
    }

    public ImageRegionIterator(Image image, int[] region) {
        this.dimensionality = image.dimensionality;
        this.dmin1 = this.dimensionality - 1;
        if(region.length != image.extent.capacity()) {
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
            if(min[i] < 0 || max[i] >= image.dimensions.get(i)) {
                throw new IllegalArgumentException("Region was out of bounds");
            }
        }
        System.arraycopy(min, 0, cur, 0, dimensionality);
        int num;
        skip[0] = image.dataType.numBytes;
        for(int i = 0; i < dmin1; i++) {
            num = 1;
            for(int j = 0; j < i; j++) {
                num *= image.dimensions.get(j);
            }
            skip[i + 1] = (min[i] + image.dimensions.get(i) - max[i] - 1) * num * image.dataType.numBytes;
        }
        for(int i = 1; i < skip.length; i++) {
            skip[i] += skip[i - 1];
        }
        values = image.values;
        values.position(locAtPos(min));
        curDim = 0;
    }
    
    private int locAtPos(int[] pos) {
        int ret = 0;
        int num = 1;
        for(int i = 0; i < dimensionality; i++) {
            ret += num * pos[i];
            num *= image.dimensions[i];
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
    
    public final void set(byte[] value) {
        if(value.length != image.dataType.numBytes) {
            throw new IllegalArgumentException("Byte length was incorrect");
        }
        values.mark();
        values.put(value);
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

        values.position(curLoc);
    }
}
