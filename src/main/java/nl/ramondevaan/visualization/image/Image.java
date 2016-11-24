package nl.ramondevaan.visualization.image;

import nl.ramondevaan.visualization.data.ComponentType;
import nl.ramondevaan.visualization.data.PixelType;
import org.apache.commons.lang3.Validate;
import org.apache.commons.lang3.tuple.ImmutablePair;

import java.nio.*;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;

public class Image {
    public final ComponentType                          componentType;
    public final PixelType                              pixelType;
    public final List<ImmutablePair<String, String>>    extraProperties;
    public final int                                    dataDimensionality;
    public final int                                    dimensionality;
    public final ByteOrder                              byteOrder;
    final IntBuffer                                     dimensions;
    final DoubleBuffer                                  spacing;
    final DoubleBuffer                                  pixelSize;
    final DoubleBuffer                                  origin;
    final DoubleBuffer                                  transformMatrix;
    ByteBuffer                                          values;
    final IntBuffer                                     extent;
    final DoubleBuffer                                  bounds;

    public Image(ComponentType componentType,   PixelType pixelType,    ByteOrder byteOrder,
                 int dataDimensionality,        int[] dimensions,       double[] spacing,
                 double[] pixelSize,            double[] origin,        double[] transformMatrix,
                 byte[] values) {
        this(
                componentType,
                pixelType,
                byteOrder,
                dataDimensionality,
                dimensions,
                spacing,
                pixelSize,
                origin,
                transformMatrix,
                values,
                new ArrayList<>()
        );
    }

    public Image(ComponentType componentType, PixelType pixelType, ByteOrder byteOrder,
                 int dataDimensionality, int[] dimensions, double[] spacing,
                 double[] pixelSize, double[] origin, double[] transformMatrix,
                 byte[] values, List<ImmutablePair<String, String>> extraProperties) {
        Validate.notNull(componentType);
        Validate.notNull(pixelType);
        Validate.notNull(dimensions);
        Validate.notNull(spacing);
        Validate.notNull(pixelSize);
        Validate.notNull(origin);
        Validate.notNull(transformMatrix);
        Validate.notNull(values);
        
        this.componentType  = componentType;
        this.pixelType      = pixelType;
        this.byteOrder      = byteOrder;
        this.dimensionality = dimensions.length;
        if(dimensionality <= 0) {
            throw new IllegalArgumentException("Dimensionality must be at least 1");
        }
        if(dataDimensionality <= 0) {
            throw new IllegalArgumentException("Data dimensionality must be at least 1");
        }
        if(spacing.length != dimensionality) {
            throw new IllegalArgumentException("Given spacing had incorrect dimensionality");
        }
        if(pixelSize.length != dimensionality) {
            throw new IllegalArgumentException("Given pixel size had incorrect dimensionality");
        }
        if(origin.length != dimensionality) {
            throw new IllegalArgumentException("Given origin had incorrect dimensionality");
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
            if(pixelSize[i] <= Double.MIN_VALUE) {
                throw new IllegalArgumentException("Size must be larger than 0");
            }
            num *= dimensions[i];
        }
        if(values.length != componentType.numberOfBytes * num * dataDimensionality) {
            throw new IllegalArgumentException("Values were of incorrect length");
        }
        IntBuffer       extentTemp      = IntBuffer     .allocate(dimensionality * 2);
        DoubleBuffer    boundsTemp      = DoubleBuffer  .allocate(extentTemp.capacity());
        IntBuffer       dimensionsTemp  = IntBuffer     .allocate(dimensionality);
        DoubleBuffer    spacingTemp     = DoubleBuffer  .allocate(dimensionality);
        DoubleBuffer    sizeTemp        = DoubleBuffer  .allocate(dimensionality);
        DoubleBuffer    originTemp      = DoubleBuffer  .allocate(dimensionality);
        DoubleBuffer    tMatrixTemp     = DoubleBuffer  .allocate(dimensionality * dimensionality);
        ByteBuffer      valuesTemp      = ByteBuffer    .allocate(values.length);
        List<ImmutablePair<String, String>> extraProps = new ArrayList<>();

        double orig;
        for(int i = 0; i < dimensionality; i++) {
            orig = origin[i] - spacing[i] / 2d;
            boundsTemp.put(orig);
            boundsTemp.put(orig + dimensions[i] * spacing[i]);
            extentTemp.put(0);
            extentTemp.put(dimensions[i] - 1);

            dimensionsTemp  .put(dimensions[i]);
            spacingTemp     .put(spacing[i]);
            sizeTemp        .put(pixelSize[i]);
            originTemp      .put(origin[i]);
        }
        Arrays.stream(transformMatrix).forEach(tMatrixTemp::put);
        for (byte value : values) {
            valuesTemp.put(value);
        }
        extraProps.addAll(extraProperties);

        this.extent             = extentTemp    .asReadOnlyBuffer();
        this.bounds             = boundsTemp    .asReadOnlyBuffer();
        this.dimensions         = dimensionsTemp.asReadOnlyBuffer();
        this.spacing            = spacingTemp   .asReadOnlyBuffer();
        this.pixelSize          = sizeTemp      .asReadOnlyBuffer();
        this.origin             = originTemp    .asReadOnlyBuffer();
        this.transformMatrix    = tMatrixTemp   .asReadOnlyBuffer();
        this.values             = valuesTemp    .asReadOnlyBuffer();
    
        this.extent             .rewind();
        this.bounds             .rewind();
        this.dimensions         .rewind();
        this.spacing            .rewind();
        this.pixelSize          .rewind();
        this.origin             .rewind();
        this.transformMatrix    .rewind();
        this.values             .rewind();
    
        this.dataDimensionality = dataDimensionality;
        this.extraProperties    = Collections.unmodifiableList(extraProps);
    }

    Image(ComponentType componentType, PixelType pixelType, ByteOrder byteOrder,
          int dataDimensionality, IntBuffer dimensions, DoubleBuffer spacing,
          DoubleBuffer pixelSize, DoubleBuffer origin, DoubleBuffer transformMatrix,
          ByteBuffer values, IntBuffer extent, DoubleBuffer bounds,
          List<ImmutablePair<String, String>> extraProps) {
        this.componentType      = componentType;
        this.pixelType          = pixelType;
        this.byteOrder          = byteOrder;
        this.dimensionality     = dimensions.capacity();
        this.dataDimensionality = dataDimensionality;
        this.dimensions         = dimensions;
        this.spacing            = spacing;
        this.pixelSize          = pixelSize;
        this.origin             = origin;
        this.transformMatrix    = transformMatrix;
        this.values             = values;
        this.extent             = extent;
        this.bounds             = bounds;
        this.extraProperties    = extraProps;
    }

    Image(ComponentType componentType, PixelType pixelType, ByteOrder byteOrder,
          int dataDimensionality, IntBuffer dimensions, DoubleBuffer spacing,
          DoubleBuffer pixelSize, DoubleBuffer origin, DoubleBuffer transformMatrix,
          ByteBuffer values, IntBuffer extent, DoubleBuffer bounds) {
        this.componentType      = componentType;
        this.pixelType          = pixelType;
        this.byteOrder          = byteOrder;
        this.dimensionality     = dimensions.capacity();
        this.dataDimensionality = dataDimensionality;
        this.dimensions         = dimensions;
        this.spacing            = spacing;
        this.pixelSize          = pixelSize;
        this.origin             = origin;
        this.transformMatrix    = transformMatrix;
        this.values             = values;
        this.extent             = extent;
        this.bounds             = bounds;
        this.extraProperties    = Collections.emptyList();
    }

    public final IntBuffer getDimensions() {
        return dimensions.duplicate();
    }

    public final DoubleBuffer getSpacing() {
        return spacing.duplicate();
    }

    public final DoubleBuffer getPixelSize() {
        return pixelSize.duplicate();
    }

    public final DoubleBuffer getOrigin() {
        return origin.duplicate();
    }

    public final DoubleBuffer getTransformMatrix() {
        return transformMatrix.duplicate();
    }

    public final ByteBuffer getValues() {
        return values.duplicate();
    }

    public final IntBuffer getExtent() {
        return extent.duplicate();
    }

    public final DoubleBuffer getBounds() {
        return bounds.duplicate();
    }
}
