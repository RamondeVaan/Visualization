package nl.ramondevaan.visualization.image;

import nl.ramondevaan.visualization.core.Filter;
import nl.ramondevaan.visualization.core.Source;
import nl.ramondevaan.visualization.utilities.DataUtils;
import org.apache.commons.lang3.Validate;
import org.apache.commons.lang3.tuple.ImmutablePair;

import java.nio.ByteBuffer;
import java.nio.DoubleBuffer;
import java.nio.IntBuffer;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public class ImageShiftExtend extends Filter<Image, Image> {
    private IntBuffer shift;
    private ByteBuffer  fillValue;
    
    public ImageShiftExtend() {
        super(1);
    }
    
    public final void setInput(Source<Image> input) {
        setInput(0, input);
    }
    
    public final void setShift(int[] shift) {
        Validate.notNull(shift);
        if(this.shift == null) {
            this.shift = IntBuffer.allocate(shift.length);
            this.shift.put(shift);
            return;
        }
        IntBuffer shiftTemp = IntBuffer.allocate(shift.length);
        shiftTemp.put(shift);
        
        shiftTemp.rewind();
        this.shift.rewind();
        
        if(!shiftTemp.equals(this.shift)) {
            this.shift = shiftTemp;
            changed();
        }
    }
    
    public final void setShift(IntBuffer shift) {
        if(this.shift == null) {
            this.shift = DataUtils.clone(shift);
            this.shift.rewind();
            return;
        }
        shift.rewind();
        this.shift.rewind();
        if(!shift.equals(this.shift)) {
            this.shift = DataUtils.clone(shift);
            this.shift.rewind();
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
        if(!fillValue.equals(this.fillValue)) {
            this.fillValue = DataUtils.clone(fillValue);
            this.fillValue.rewind();
            changed();
        }
    }
    
    @Override
    protected Image updateImpl() throws Exception {
        Image input = getInput(0);
        Validate.notNull(input);
    
        if(shift == null || DataUtils.allZero(shift)) {
            return input;
        }
    
        //Compute the extension used (based on dimensionality)
        IntBuffer shiftUsed = IntBuffer.allocate(input.dimensionality);
        shift.limit(Math.min(input.dimensionality, shift.capacity()));
        shift.rewind();
        shiftUsed.put(shift);
        while(shiftUsed.hasRemaining()) {
            shiftUsed.put(0);
        }
        shift.limit(shift.capacity());

        ImmutablePair<Image, ByteBuffer> pair = constructOutput(input, shiftUsed);
        Image       output = pair.left;
        ByteBuffer  buffer = pair.right;
        setZeros(input, output, buffer, shiftUsed);
        setValues(input, output, buffer, shiftUsed);
    
        return output;
    }
    
    private ImmutablePair<Image, ByteBuffer> constructOutput(Image input, IntBuffer shiftUsed) {
        final int   dimensionality = input.dimensionality;
        double      orig;
        double      spacing;
        int         dim;
        int         ext;
    
        IntBuffer       inputDimensions = input         .getDimensions();
        DoubleBuffer    inputOrigin     = input         .getOrigin();
        DoubleBuffer    inputSpacing    = input         .getSpacing();
        DoubleBuffer    origin          = DoubleBuffer  .allocate(dimensionality);
        IntBuffer       dimensions      = IntBuffer     .allocate(dimensionality);
        IntBuffer       newExtent       = IntBuffer     .allocate(input.dimensionality * 2);
        DoubleBuffer    newBounds       = DoubleBuffer  .allocate(input.dimensionality * 2);
        ByteBuffer      values;
    
        //Compute new dimensions
        inputDimensions.rewind();
        shiftUsed.rewind();
        while(dimensions.hasRemaining()) {
            dimensions.put(inputDimensions.get() + Math.abs(shiftUsed.get()));
        }
    
        //Initialize the new data
        int numValues = 1;
        dimensions.rewind();
        while(dimensions.hasRemaining()) {
            numValues *= dimensions.get();
        }
        numValues *= input.componentType.numberOfBytes * input.dataDimensionality;
        values = ByteBuffer.allocate(numValues);
    
        //Compute new origin
        origin.rewind();
        shiftUsed.rewind();
        while(shiftUsed.hasRemaining()) {
            ext = shiftUsed.get();
            origin.put(inputOrigin.get() +
                    (ext < 0 ? ext : 0) * inputSpacing.get());
        }
    
        //Compute new extent and bounds
        shiftUsed   .rewind();
        inputSpacing.rewind();
        dimensions  .rewind();
        origin      .rewind();
        while(newExtent.hasRemaining()) {
            ext = shiftUsed.get();
            dim = dimensions.get();
            spacing = inputSpacing.get();
        
            newExtent.put(0);
            newExtent.put(dim + Math.abs(ext));
        
            orig = origin.get() - spacing / 2d;
            newBounds.put(orig);
            newBounds.put(orig + dim * spacing);
        }
    
        //Copy other properties
        List<ImmutablePair<String, String>> extraProperties =
                new ArrayList<>(input.extraProperties);
    
        //Rewind buffers
        dimensions  .rewind();
        origin      .rewind();
        newExtent   .rewind();
        newBounds   .rewind();
    
        //Return new image
        return new ImmutablePair<>(new Image(
                input       .componentType,
                input       .pixelType,
                input       .dataDimensionality,
                dimensions  .asReadOnlyBuffer(),
                input       .getSpacing(),
                input       .getPixelSize(),
                origin      .asReadOnlyBuffer(),
                input       .getTransformMatrix(),
                values.asReadOnlyBuffer(),
                newExtent   .asReadOnlyBuffer(),
                newBounds   .asReadOnlyBuffer(),
                Collections .unmodifiableList(extraProperties)
        ), values);
    }

    private void setZeros(Image input, Image output, ByteBuffer buffer, IntBuffer shiftUsed) {
        final int dataLen = input.componentType.numberOfBytes * input.dataDimensionality;
    
        IntBuffer inputDimensions   = input.getDimensions();
        IntBuffer outputDimensions  = output.getDimensions();
        ByteBuffer fillValueUsed    = ByteBuffer.allocate(dataLen);
    
        int shift;
        int outDim;
    
        //Compute the fillValue used
        //If none is set, the zero value for the component type is used.
        if(fillValue == null) {
            for(int i = 0; i < input.dataDimensionality; i++) {
                fillValueUsed.put(input.componentType.getZero());
            }
        } else if(fillValue.limit() != dataLen) {
            throw new IllegalArgumentException("Fill byte length was incorrect");
        } else {
            fillValueUsed.put(fillValue);
            fillValue.rewind();
        }
        fillValueUsed.rewind();
    
        //Copy the input extent
        int[] region = new int[input.dimensionality * 2];
        int a1, a2;
        for(int i = 0; i < input.dimensionality; i++) {
            a1 = 2 * i;
            a2 = a1 + 1;
            region[a1] = 0;
            region[a2] = inputDimensions.get() - 1;
        }
        
        //Set zero values to non-overlapping regions
        shiftUsed.rewind();
        int[] other = new int[2];
        for(int i = 0; i < input.dimensionality; i++) {
            shift   = shiftUsed         .get();
            outDim  = outputDimensions  .get();
            if(shift == 0) {
                continue;
            }
            a1 = 2 * i;
            a2 = a1 + 1;
            if(shift > 0) {
                region[a1] = 0;
                region[a2] = shift - 1;
                other[0] = shift;
                other[1] = outDim - 1;
            } else {
                region[a1] = outDim + shift;
                region[a2] = outDim - 1;
                other[0] = 0;
                other[1] = region[a1] - 1;
            }
    
            ImageRegionIterator it = new ImageRegionIterator(buffer, output.componentType,
                    output.getDimensions(), output.dataDimensionality, region);
            while(it.hasNext()) {
                it.next().put(fillValueUsed);
                fillValueUsed.rewind();
            }
            
            region[a1] = other[0];
            region[a2] = other[1];
        }
    }

    private void setValues(Image input, Image output, ByteBuffer buffer, IntBuffer shiftUsed) {
        int[] inRegion  = new int[input.dimensionality * 2];
        int[] outRegion = new int[output.dimensionality * 2];
    
        IntBuffer inputDimensions   = input .getDimensions();
        IntBuffer outputDimensions  = output.getDimensions();
        
        shiftUsed.rewind();
        int shift;
        
        int a1, a2;
        for(int i = 0; i < input.dimensionality; i++) {
            shift = shiftUsed.get();
            a1 = 2 * i;
            a2 = a1 + 1;
            inRegion[a2] = inputDimensions.get() - 1;
            if(shift > 0) {
                outRegion[a1] = shift;
                outRegion[a2] = outputDimensions.get() - 1;
            } else {
                outRegion[a2] = inRegion[a2];
                outputDimensions.get();
            }
        }
        ImageRegionIterator in = new ImageRegionIterator(input, inRegion);
        ImageRegionIterator out = new ImageRegionIterator(buffer, output.componentType,
                output.getDimensions(), output.dataDimensionality, outRegion);
        
        while(in.hasNext()) {
            out.next().put(in.next());
        }
    }
}
