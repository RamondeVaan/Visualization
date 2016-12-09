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

public class ImageExtend extends Filter<Image, Image> {
    private IntBuffer   extension;
    private ByteBuffer  fillValue;
    
    public ImageExtend() {
        super(1);
    }
    
    public final void setExtension(int[] extension) {
        Validate.notNull(extension);
        if(this.extension == null) {
            this.extension = IntBuffer.allocate(extension.length);
            this.extension.put(extension);
            return;
        }
        IntBuffer extensionTemp = IntBuffer.allocate(extension.length);
        extensionTemp.put(extension);

        extensionTemp.rewind();
        this.extension.rewind();

        if(!extensionTemp.equals(this.extension)) {
            this.extension = extensionTemp;
            changed();
        }
    }

    public final void setExtension(IntBuffer extension) {
        if(this.extension == null) {
            this.extension = DataUtils.clone(extension);
            this.extension.rewind();
            return;
        }
        extension.rewind();
        this.extension.rewind();
        if(!extension.equals(this.extension)) {
            this.extension = DataUtils.clone(extension);
            this.extension.rewind();
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
    
    public final void setInput(Source<Image> input) {
        setInput(0, input);
    }
    
    @Override
    protected Image updateImpl() throws Exception {
        Image input = getInput(0);
        Validate.notNull(input);

        if(extension == null || DataUtils.allZero(extension)) {
            return input;
        }
    
        //Compute the extension used (based on dimensionality)
        IntBuffer extensionUsed = IntBuffer.allocate(input.dimensionality);
        extension.limit(Math.min(input.dimensionality, extension.capacity()));
        extension.rewind();
        extensionUsed.put(extension);
        while(extensionUsed.hasRemaining()) {
            extensionUsed.put(0);
        }
        extension.limit(extension.capacity());

        ImmutablePair<Image, ByteBuffer> pair = constructOutput(input, extensionUsed);
        Image       output = pair.left;
        ByteBuffer  buffer = pair.right;
        setZeros(input, output, buffer, extensionUsed);
        setValues(input, output, buffer, extensionUsed);
        
        return output;
    }
    
    private ImmutablePair<Image, ByteBuffer> constructOutput(Image input, IntBuffer extensionUsed) {
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
        extensionUsed.rewind();
        inputDimensions.rewind();
        while(dimensions.hasRemaining()) {
            dimensions.put(inputDimensions.get() + Math.abs(extensionUsed.get()));
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
        extensionUsed.rewind();
        while(extensionUsed.hasRemaining()) {
            ext = extensionUsed.get();
            origin.put(inputOrigin.get() +
                    (ext < 0 ? ext : 0) * inputSpacing.get());
        }
    
        //Compute new extent and bounds
        extensionUsed   .rewind();
        inputSpacing    .rewind();
        dimensions      .rewind();
        origin          .rewind();
        while(newExtent.hasRemaining()) {
            ext = extensionUsed.get();
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
    
    private void setZeros(Image input, Image output, ByteBuffer buffer, IntBuffer extensionUsed) {
        final int dataLen = input.componentType.numberOfBytes * input.dataDimensionality;
    
        IntBuffer inputDimensions   = input.getDimensions();
        IntBuffer outputDimensions  = output.getDimensions();
        ByteBuffer fillValueUsed    = ByteBuffer.allocate(dataLen);
        
        int ext;
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
        
        //Set zero values to non-overlapping region
        int[] other = new int[2];
        extensionUsed.rewind();
        for(int i = 0; i < input.dimensionality; i++) {
            ext = extensionUsed.get();
            outDim = outputDimensions.get();
            if(ext == 0) {
                continue;
            }
            a1 = 2 * i;
            a2 = a1 + 1;
            if(ext < 0) {
                region[a1] = 0;
                region[a2] = -ext - 1;
                other[0] = -ext;
                other[1] = outDim - 1;
            } else {
                region[a1] = outDim - ext;
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
    
    private void setValues(Image input, Image output, ByteBuffer buffer, IntBuffer extensionUsed) {
        int[] inRegion  = new int[input.dimensionality * 2];
        int[] outRegion = new int[input.dimensionality * 2];
    
        IntBuffer dimensions = input.getDimensions();
        int ext;
        
        int a1, a2;
        extensionUsed.rewind();
        for(int i = 0; i < input.dimensionality; i++) {
            ext = extensionUsed.get();
            a1 = 2 * i;
            a2 = a1 + 1;
            inRegion[a2] = dimensions.get() - 1;
            if(ext < 0) {
                outRegion[a1] = -ext;
                outRegion[a2] = inRegion[a2] - ext;
            } else {
                outRegion[a2] = inRegion[a2];
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
