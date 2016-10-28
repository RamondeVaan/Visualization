package nl.ramondevaan.visualization.image;

import nl.ramondevaan.visualization.core.Filter;
import nl.ramondevaan.visualization.core.Source;
import nl.ramondevaan.visualization.data.DataType;
import nl.ramondevaan.visualization.utilities.DataUtils;
import org.apache.commons.lang3.ArrayUtils;
import org.apache.commons.lang3.Validate;

import java.nio.ByteBuffer;
import java.nio.DoubleBuffer;
import java.nio.IntBuffer;
import java.nio.LongBuffer;
import java.util.Arrays;

public class ImageExtend extends Filter<Image, Image> {
    private LongBuffer extension;
    private ByteBuffer fillValue;
    
    public ImageExtend() {
        super(1);
    }
    
    public final void setExtension(long[] extension) {
        Validate.notNull(extension);
        LongBuffer extensionTemp = LongBuffer.allocate(extension.length);
        extensionTemp.put(extension);

        extensionTemp.rewind();
        this.extension.rewind();

        if(!extensionTemp.equals(this.extension)) {
            this.extension = extensionTemp;
            changed();
        }
    }

    public final void setExtension(LongBuffer extension) {
        extension.rewind();
        this.extension.rewind();
        if(!extension.equals(this.extension)) {
            this.extension = DataUtils.clone(extension);
            changed();
        }
    }
    
    public final void setFillValue(byte[] fillValue) {
        if(fillValue == null) {
            this.fillValue = null;
            return;
        }
        ByteBuffer fillValueTemp = ByteBuffer.allocate(fillValue.length);
        fillValueTemp.put(fillValue);

        fillValueTemp.rewind();
        this.fillValue.rewind();

        if(!fillValueTemp.equals(this.fillValue)) {
            this.fillValue = fillValueTemp;
            changed();
        }
    }

    public final void setFillValue(ByteBuffer fillValue) {
        if(fillValue == null) {
            this.fillValue = null;
            return;
        }
        fillValue.rewind();
        this.fillValue.rewind();
        if(!fillValue.equals(this.fillValue)) {
            this.fillValue = DataUtils.clone(fillValue);
            changed();
        }
    }
    
    public final void setInput(Source<Image> input) {
        setInput(0, input);
    }
    
    @Override
    protected Image updateImpl() throws Exception {
        Image input = getInput(0);
        Validate.notNull(input);

        if(extension == null || DataUtils.allZero(extension)) {
            return input.copy();
        }

        ByteBuffer fillValueUsed;

        if(fillValue == null) {
            fillValueUsed = DataUtils.clone(input.dataType.zero);
        } else if(fillValue.limit() != input.dataType.numBytes) {
            throw new IllegalArgumentException("Fill byte length was incorrect");
        }

        LongBuffer extensionUsed = LongBuffer.allocate(input.dimensionality);
        extension.limit(input.dimensionality);
        extension.rewind();
        extensionUsed.put(extension);
        extension.limit(extension.capacity());
        
        Image output = constructOutput(input, extensionUsed);
        setZeros(input, output);
        setValues(input, output);
        
        return output;
    }
    
    private Image constructOutput(Image input, LongBuffer extensionUsed) {
        int dimensionality = input.dimensionality;

        input.dimensions.rewind();
        LongBuffer dimSizes = LongBuffer.allocate(dimensionality);
        while(dimSizes.hasRemaining()) {
            dimSizes.put(input.dimensions.get() + Math.abs(extension.get()));
        }
        int numValues = 1;
        dimSizes.rewind();
        while(dimSizes.hasRemaining()) {
            numValues *= dimSizes.get();
        }
        numValues *= input.dataType.numBytes * input.dataDimensionality;

        DoubleBuffer origin = DataUtils.clone(input.origin);
        ByteBuffer values   = ByteBuffer.allocate(numValues);
        DataType dataType = input.dataType.copy();

        origin.rewind();
        input.origin.rewind();
        input.spacing.rewind();
        extensionUsed.rewind();
        while(extensionUsed.hasRemaining()) {
            origin.put(input.origin.get() +
                    extensionUsed.get() * input.spacing.get());
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
            if(extension[i] == 0) {
                continue;
            }
            a1 = 2 * i;
            a2 = a1 + 1;
            if(extension[i] < 0) {
                region[a1] = 0;
                region[a2] = extension[i] - 1;
                other[0] = extension[i];
                other[1] = dimSizes[i] - 1;
            } else {
                region[a1] = dimSizes[i] - extension[i];
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
        int[] outRegion = new int[input.extent.length];
        int a1, a2;
        for(int i = 0; i < dimensionality; i++) {
            a1 = 2 * i;
            a2 = a1 + 1;
            inRegion[a2] = input.dimensions[i] - 1;
            if(extension[i] < 0) {
                outRegion[a1] = Math.abs(extension[i]);
                outRegion[a2] = inRegion[a2] + extension[i];
            }else {
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
