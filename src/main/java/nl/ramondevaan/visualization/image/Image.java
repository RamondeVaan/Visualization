package nl.ramondevaan.visualization.image;

import javafx.util.Pair;
import nl.ramondevaan.visualization.data.DataType;
import nl.ramondevaan.visualization.utilities.DataUtils;
import org.apache.commons.lang3.Validate;

import java.nio.ByteBuffer;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public class Image {
    final DataType dataType;
    final int dimensionality;
    int[] dimensions;
    double[] spacing;
    double[] size;
    double[] offset;
    double[] transformMatrix;
    ByteBuffer values;
    final int[] extent;
    final double[] bounds;
    final List<Pair<String, String>> extraProperties;

    public Image(DataType dataType, int dimensionality, int[] dimensions,
                 double[] spacing, double[] size, double[] offset,
                 double[] transformMatrix, byte[] values) {
        this(dataType, dimensionality, dimensions, spacing, size,
                offset, transformMatrix, values, new ArrayList<>());
    }

    public Image(DataType dataType, int dimensionality, int[] dimensions,
                 double[] spacing, double[] size, double[] offset,
                 double[] transformMatrix, byte[] values, List<Pair<String, String>> extraProperties) {
        if(dimensionality <= 0) {
            throw new IllegalArgumentException("Dimensionality must be at least 1");
        }
        if(dimensions.length != dimensionality) {
            throw new IllegalArgumentException("Given dimensions had incorrect dimensionality");
        }
        if(spacing.length != dimensionality) {
            throw new IllegalArgumentException("Given spacing had incorrect dimensionality");
        }
        if(size.length != dimensionality) {
            throw new IllegalArgumentException("Given size had incorrect dimensionality");
        }
        if(offset.length != dimensionality) {
            throw new IllegalArgumentException("Given offset had incorrect dimensionality");
        }
        if(transformMatrix.length != dimensionality * dimensionality) {
            throw new IllegalArgumentException("Given transform matrix had incorrect dimensionality");
        }
        int num = 1;
        for(int i = 0; i < dimensionality; i++) {
            if(dimensions[i] <= 0) {
                throw new IllegalArgumentException("Dimensions can not be smaller than 1");
            }
            if(spacing[i] <= Double.MIN_VALUE) {
                throw new IllegalArgumentException("Spacing must be larger than 0");
            }
            if(size[i] <= Double.MIN_VALUE) {
                throw new IllegalArgumentException("Size must be larger than 0");
            }
            num *= dimensions[i];
        }
        if(values.length != dataType.numBytes * num) {
            throw new IllegalArgumentException("Values were of incorrect length");
        }
        extent = new int[2 * dimensions.length];
        for(int i = 0; i < dimensions.length; i++) {
            extent[2 * i + 1] = dimensions[i] - 1;
        }
        bounds = new double[extent.length];
        int k;
        for(int i = 0; i < dimensions.length; i++) {
            k = 2 * i;
            bounds[k] = offset[i];
            bounds[k + 1] = offset[i] + dimensions[i] * spacing[i];
        }
        
        this.dataType = dataType.copy();
        this.dimensionality = dimensionality;
        this.dimensions = new int[dimensionality];
        this.spacing = new double[dimensionality];
        this.size = new double[dimensionality];
        this.offset = new double[dimensionality];
        this.transformMatrix = new double[transformMatrix.length];
        byte[] copy = new byte[values.length];
        
        System.arraycopy(dimensions, 0, this.dimensions, 0, dimensions.length);
        System.arraycopy(spacing, 0, this.spacing, 0, spacing.length);
        System.arraycopy(size, 0, this.size, 0, size.length);
        System.arraycopy(offset, 0, this.offset, 0, offset.length);
        System.arraycopy(transformMatrix, 0, this.transformMatrix, 0, transformMatrix.length);
        System.arraycopy(values, 0, copy, 0, copy.length);
        
        this.values = ByteBuffer.wrap(copy);
        this.extraProperties = Collections.unmodifiableList(extraProperties);
    }

    Image(DataType dataType, int dimensionality, int[] dimensions,
          double[] spacing, double[] size, double[] offset,
          double[] transformMatrix, ByteBuffer values, int[] extent,
          double[] bounds, List<Pair<String, String>> extraProps) {
        this.dataType = dataType;
        this.dimensionality = dimensionality;
        this.dimensions = dimensions;
        this.spacing = spacing;
        this.size = size;
        this.offset = offset;
        this.transformMatrix = transformMatrix;
        this.values = values;
        this.extent = extent;
        this.bounds = bounds;
        this.extraProperties = Collections.unmodifiableList(extraProps);
    }

    Image(DataType dataType, int dimensionality, int[] dimensions,
          double[] spacing, double[] size, double[] offset,
          double[] transformMatrix, ByteBuffer values, int[] extent, double[] bounds) {
        this.dataType = dataType;
        this.dimensionality = dimensionality;
        this.dimensions = dimensions;
        this.spacing = spacing;
        this.size = size;
        this.offset = offset;
        this.transformMatrix = transformMatrix;
        this.values = values;
        this.extent = extent;
        this.bounds = bounds;
        this.extraProperties = Collections.emptyList();
    }
    
    public final DataType getDataType() {
        return dataType;
    }
    
    public final int getDimensionality() {
        return dimensionality;
    }
    
    public final int[] getDimensions() {
        return dimensions;
    }
    
    public final double[] getSpacing() {
        return spacing;
    }
    
    public final double[] getSize() {
        return size;
    }
    
    public final double[] getOffset() {
        return offset;
    }
    
    public final double[] getTransformMatrix() {
        return transformMatrix;
    }
    
    public final ByteBuffer getValues() {
        return values;
    }
    
    public final int[] getExtent() {
        return extent;
    }
    
    public final double[] getBounds() {
        return bounds;
    }

    public final List<Pair<String, String>> getExtraProperties() {
        return extraProperties;
    }

    public final Image copy() {
        DataType dataType = this.dataType.copy();
        int[] dimensions = new int[dimensionality];
        double[] spacing = new double[dimensionality];
        double[] size = new double[dimensionality];
        double[] offset = new double[dimensionality];
        double[] transformMatrix = new double[this.transformMatrix.length];
        ByteBuffer values = DataUtils.cloneByteBuffer(this.values);
        int[] extent = new int[this.extent.length];
        double[] bounds = new double[this.bounds.length];
        
        System.arraycopy(this.dimensions, 0, dimensions, 0, dimensionality);
        System.arraycopy(this.spacing, 0, spacing, 0, dimensionality);
        System.arraycopy(this.size, 0, size, 0, dimensionality);
        System.arraycopy(this.offset, 0, offset, 0, dimensionality);
        System.arraycopy(this.transformMatrix, 0, transformMatrix, 0, transformMatrix.length);
        System.arraycopy(this.extent, 0, extent, 0, extent.length);
        System.arraycopy(this.bounds, 0, bounds, 0, bounds.length);
        
        return new Image(dataType, dimensionality, dimensions, spacing,
                size, offset, transformMatrix, values, extent, bounds, extraProperties);
    }
}
