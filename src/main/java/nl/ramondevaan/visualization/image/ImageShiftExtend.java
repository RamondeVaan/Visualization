package nl.ramondevaan.visualization.image;

import nl.ramondevaan.visualization.core.Filter;
import nl.ramondevaan.visualization.core.Source;
import nl.ramondevaan.visualization.data.DataType;
import org.apache.commons.lang3.ArrayUtils;
import org.apache.commons.lang3.Validate;

import java.nio.ByteBuffer;
import java.util.Arrays;

public class ImageShiftExtend extends Filter<Image, Image> {
    private int[] shift;
    private byte[] fillValue;
    
    private int[] dimSizes;
    private int dimensionality;
    private int numValues;
    
    public ImageShiftExtend() {
        super(1);
    }
    
    public final void setInput(Source<Image> input) {
        setInput(0, input);
    }
    
    public final void setShift(int[] shift) {
        if(!Arrays.equals(this.shift, shift)) {
            this.shift = ArrayUtils.clone(shift);
            changed();
        }
    }
    
    public final void setFillValue(byte[] fillValue) {
        if(!Arrays.equals(this.fillValue, fillValue)) {
            this.fillValue = ArrayUtils.clone(fillValue);
            changed();
        }
    }
    
    @Override
    protected Image updateImpl() throws Exception {
        Image input = getInput(0);
    
        if(ArrayUtils.isEmpty(shift) ||
                Arrays.equals(new int[shift.length], shift)) {
            return input.copy();
        }
        if(fillValue == null) {
            fillValue = new byte[input.dataType.numBytes];
            input.dataType.zero.rewind();
            input.dataType.zero.get(fillValue);
        } else if(fillValue.length != input.dataType.numBytes) {
            throw new IllegalArgumentException("Fill byte length was incorrect");
        }
    
        Image output = constructOutput(input);
        setZeros(input, output);
        setValues(input, output);
    
        return output;
    }
    
    private Image constructOutput(Image input) {
        dimensionality = input.dimensionality;
        if(shift.length < dimensionality) {
            int[] t = new int[dimensionality];
            System.arraycopy(shift, 0, t, 0, shift.length);
            this.shift = t;
        }
        dimSizes = new int[input.dimensionality];
        for(int i = 0; i < input.dimensionality; i++) {
            dimSizes[i] = input.dimensions[i] + Math.abs(shift[i]);
        }
        numValues = 1;
        for(int i = 0; i < dimensionality; i++) {
            numValues *= dimSizes[i];
        }
        numValues *= input.dataType.numBytes;
        
        DataType dataType = input.dataType.copy();
        double[] spacing = new double[dimensionality];
        double[] size = new double[dimensionality];
        double[] offset = new double[dimensionality];
        double[] transform = new double[input.transformMatrix.length];
        byte[] values = new byte[numValues];
        
        System.arraycopy(input.spacing, 0, spacing, 0, dimensionality);
        System.arraycopy(input.size, 0, size, 0, dimensionality);
        System.arraycopy(input.offset, 0, offset, 0, dimensionality);
        System.arraycopy(input.transformMatrix, 0, transform, 0, transform.length);
        
        for(int i = 0; i < dimensionality; i++) {
            if(shift[i] < 0) {
                offset[i] += shift[i] * spacing[i];
            }
        }
    
        int[] extent = new int[input.extent.length];
        double[] bounds = new double[extent.length];
        int a1, a2;
        for(int i = 0; i < dimensionality; i++) {
            a1 = 2 * i;
            a2 = a1 + 1;
            bounds[a1] = offset[i];
            extent[a2] = dimSizes[i] - 1;
            bounds[a2] = offset[i] + dimSizes[i] * spacing[i];
        }
        
        return new Image(dataType, dimensionality, dimSizes, spacing,
                size, offset, transform, ByteBuffer.wrap(values), extent,
                bounds, input.extraProperties);
    }
    
    private void setZeros(Image input, Image output) {
        int[] region = new int[input.extent.length];
        int a1, a2;
        for(int i = 0; i < dimensionality; i++) {
            a1 = 2 * i;
            a2 = a1 + 1;
            region[a1] = 0;
            region[a2] = input.dimensions[i] - 1;
        }
        int[] other = new int[2];
        for(int i = 0; i < dimensionality; i++) {
            if(shift[i] == 0) {
                continue;
            }
            a1 = 2 * i;
            a2 = a1 + 1;
            if(shift[i] > 0) {
                region[a1] = 0;
                region[a2] = shift[i] - 1;
                other[0] = shift[i];
                other[1] = dimSizes[i] - 1;
            } else {
                region[a1] = dimSizes[i] - shift[i];
                region[a2] = dimSizes[i] - 1;
                other[0] = 0;
                other[1] = region[a1] - 1;
            }
    
            ImageRegionIterator it = new ImageRegionIterator(output, region);
            while(it.hasNext()) {
                it.set(fillValue);
                it.next();
            }
            
            region[a1] = other[0];
            region[a2] = other[1];
        }
    }
    
    private void setValues(Image input, Image output) {
        int[] inRegion = new int[input.extent.length];
        int[] outRegion = new int[output.extent.length];
        
        int a1, a2;
        for(int i = 0; i < dimensionality; i++) {
            a1 = 2 * i;
            a2 = a1 + 1;
            inRegion[a2] = input.dimensions[i] - 1;
            if(shift[i] > 0) {
                outRegion[a1] = shift[i];
                outRegion[a2] = output.dimensions[i] - 1;
            } else {
                outRegion[a2] = inRegion[a2];
            }
        }
        ImageRegionIterator in = new ImageRegionIterator(input, inRegion);
        ImageRegionIterator out = new ImageRegionIterator(output, outRegion);
        
        while(in.hasNext()) {
            out.set(in.get());
            in.next();
            out.next();
        }
    }
}
