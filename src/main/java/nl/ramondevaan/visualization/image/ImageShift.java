package nl.ramondevaan.visualization.image;

import nl.ramondevaan.visualization.core.Filter;
import nl.ramondevaan.visualization.core.Source;
import nl.ramondevaan.visualization.utilities.DataUtils;
import org.apache.commons.lang3.Validate;
import org.apache.commons.lang3.tuple.ImmutablePair;

import java.nio.ByteBuffer;
import java.nio.IntBuffer;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;

public class ImageShift extends Filter<Image, Image> {
    private IntBuffer   shift;
    private ByteBuffer  fillValue;
    
    public ImageShift() {
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
            return;
        }
        shift.rewind();
        this.shift.rewind();
        if(!shift.equals(this.shift)) {
            this.shift = DataUtils.clone(shift);
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
        shift.limit(input.dimensionality);
        shift.rewind();
        shiftUsed.put(shift);
        while(shiftUsed.hasRemaining()) {
            shiftUsed.put(0);
        }
        shift.limit(shift.capacity());
    
        Image output = constructOutput(input);
        setZeros(input, output, shiftUsed);
        setValues(input, output, shiftUsed);
        output.values = output.values.asReadOnlyBuffer();
    
        return output;
    }
        
    private Image constructOutput(Image input) {
        //Copy other properties
        List<ImmutablePair<String, String>> extraProperties =
                new ArrayList<>(input.extraProperties);
        
        return new Image(
                input.componentType,
                input.pixelType,
                input.byteOrder,
                input.dataDimensionality,
                input.getDimensions(),
                input.getSpacing(),
                input.getPixelSize(),
                input.getOrigin(),
                input.getTransformMatrix(),
                ByteBuffer.allocate(input.values.capacity()),
                input.getExtent(),
                input.getBounds(),
                Collections.unmodifiableList(extraProperties)
        );
    }
    
    private void setZeros(Image input, Image output, IntBuffer shiftUsed) {
        final int dataLen = input.componentType.numberOfBytes * input.dataDimensionality;
    
        IntBuffer inputDimensions   = input.getDimensions();
        ByteBuffer fillValueUsed    = ByteBuffer.allocate(dataLen);
    
        int shift;
        int inDim;
        
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
        int[] other = new int[2];
        shiftUsed       .rewind();
        inputDimensions .rewind();
        for(int i = 0; i < input.dimensionality; i++) {
            shift = shiftUsed.get();
            inDim = inputDimensions.get();
            if(shift == 0) {
                continue;
            }
            a1 = 2 * i;
            a2 = a1 + 1;
            if(shift > 0) {
                region[a1] = 0;
                region[a2] = shift - 1;
                other[0] = shift;
                other[1] = inDim - 1;
            } else {
                region[a1] = inDim + shift;
                region[a2] = inDim - 1;
                other[0] = 0;
                other[1] = region[a1] - 1;
            }
            
            ImageRegionIterator it = new ImageRegionIterator(output, region);
            while(it.hasNext()) {
                it.next().put(fillValueUsed);
                fillValueUsed.rewind();
            }
            
            region[a1] = other[0];
            region[a2] = other[1];
        }
    }
    
    private void setValues(Image input, Image output, IntBuffer shiftUsed) {
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
            if(shift > 0) {
                inRegion[a2] = inputDimensions.get() - 1 - shift;
                outRegion[a1] = shift;
                outRegion[a2] = outputDimensions.get() - 1;
            } else {
                outRegion[a2] = inputDimensions.get() - 1 + shift;
                inRegion[a1] = -shift;
                inRegion[a2] = outputDimensions.get() - 1;
            }
        }
        ImageRegionIterator in = new ImageRegionIterator(input, inRegion);
        ImageRegionIterator out = new ImageRegionIterator(output, outRegion);
        
        while(in.hasNext()) {
            out.next().put(in.next());
        }
    }
}
