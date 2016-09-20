package nl.ramondevaan.visualization.image;

public class ImageRegionIterator {
    private final int dimensionality;
    private final int dmin1;
    private final Image image;
    private final int[] min;
    private final int[] max;
    private final int[] cur;
    private final int[] skip;
    private final byte[] values;
    private int curLoc;
    private int curDim;
    
    public ImageRegionIterator(Image image, int[] region) {
        this.dimensionality = image.dimensionality;
        this.dmin1 = this.dimensionality - 1;
        this.image = image;
        if(region.length != dimensionality * 2) {
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
        int k;
        for(int i = 0; i < dmin1; i++) {
            num = 1;
            k = i + 1;
            for(int j = i + 2; j < dimensionality; j++) {
                num *= image.dimensions[j];
            }
            skip[i] = (min[k] + image.dimensions[k] - max[k] - 1) * num * image.dataType.numBytes;
        }
        skip[dmin1] = image.dataType.numBytes;
        curLoc = locAtPos(min);
        curDim = dmin1;
        values = image.values.array();
    }
    
    private int locAtPos(int[] pos) {
        int ret = 0;
        int num;
        for(int i = 0; i < dimensionality; i++) {
            if(pos[i] == 0) {
                continue;
            }
            num = 1;
            for(int j = i + 1; j < dimensionality; j++) {
                num *= image.dimensions[j];
            }
            ret += num * pos[i];
        }
        ret *= image.dataType.numBytes;
        return ret;
    }
    
    public int[] getIndex() {
        int[] ret = new int[dimensionality];
        System.arraycopy(cur, 0, ret, 0, dimensionality);
        return ret;
    }
    
    public byte[] get() {
        byte[] ret = new byte[image.dataType.numBytes];
        System.arraycopy(values, curLoc, ret, 0, ret.length);
        return ret;
    }
    
    public void set(byte[] b) {
        System.arraycopy(b, 0, values, curLoc, image.dataType.numBytes);
    }
    
    public boolean hasNext() {
        return curDim >= 0;
    }
    
    public void next() {
        while(curDim >= 0) {
            if (cur[curDim] < max[curDim]) {
                cur[curDim]++;
                for(;curDim < dmin1; curDim++) {
                    curLoc += skip[curDim];
                }
                curLoc += skip[curDim];
                break;
            } else {
                cur[curDim] = min[curDim];
                curDim--;
            }
        }
    }
}
