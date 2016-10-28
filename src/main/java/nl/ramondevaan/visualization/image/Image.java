package nl.ramondevaan.visualization.image;

import nl.ramondevaan.visualization.data.DataType;
import org.apache.commons.lang3.tuple.ImmutablePair;

import java.nio.ByteBuffer;
import java.nio.DoubleBuffer;
import java.nio.LongBuffer;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;

public class Image {
    final DataType                              dataType;
    final int                                   dataDimensionality;
    final int                                   dimensionality;
    final LongBuffer                            dimensions;
    final DoubleBuffer                          spacing;
    final DoubleBuffer                          pixelSize;
    final DoubleBuffer                          origin;
    final DoubleBuffer                          transformMatrix;
    final ByteBuffer                            values;
    final LongBuffer                            extent;
    final DoubleBuffer                          bounds;
    final List<ImmutablePair<String, String>>   extraProperties;

    public Image(DataType dataType, int dataDimensionality,     int dimensionality,
                 int[] dimensions,  double[] spacing,           double[] pixelSize,
                 double[] origin,   double[] transformMatrix,   byte[] values) {
        this(dataType, dataDimensionality, dimensionality, dimensions, spacing,
                pixelSize, origin, transformMatrix, values, new ArrayList<>());
    }

    public Image(DataType dataType, int dataDimensionality,     int dimensionality,
                 int[] dimensions,  double[] spacing,           double[] pixelSize,
                 double[] origin,   double[] transformMatrix,   byte[] values,
                 List<ImmutablePair<String, String>> extraProperties) {
        if(dimensionality <= 0) {
            throw new IllegalArgumentException("Dimensionality must be at least 1");
        }
        if(dataDimensionality <= 0) {
            throw new IllegalArgumentException("Data dimensionality must be at least 1");
        }
        if(dimensions.length != dimensionality) {
            throw new IllegalArgumentException("Given dimensions had incorrect dimensionality");
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
        if(values.length != dataType.numBytes * num * dataDimensionality) {
            throw new IllegalArgumentException("Values were of incorrect length");
        }
        LongBuffer      extentTemp      = LongBuffer.allocate(2 * dimensionality);
        DoubleBuffer    boundsTemp      = DoubleBuffer.allocate(extentTemp.capacity());
        LongBuffer      dimensionsTemp  = LongBuffer.allocate(dimensionality);
        DoubleBuffer    spacingTemp     = DoubleBuffer.allocate(dimensionality);
        DoubleBuffer    sizeTemp        = DoubleBuffer.allocate(dimensionality);
        DoubleBuffer    originTemp      = DoubleBuffer.allocate(dimensionality);
        DoubleBuffer    tMatrixTemp     = DoubleBuffer.allocate(dimensionality * dataDimensionality);
        ByteBuffer      valuesTemp      = ByteBuffer.allocate(values.length);
        List<ImmutablePair<String, String>> extraProps = new ArrayList<>();

        for(int i = 0; i < dimensionality; i++) {
            boundsTemp.put(origin[i]);
            boundsTemp.put(origin[i] + dimensions[i] * spacing[i]);
            extentTemp.put(0);
            extentTemp.put(dimensions[i] - 1);

            dimensionsTemp  .put(dimensions[i]);
            spacingTemp     .put(spacing[i]);
            sizeTemp        .put(pixelSize[i]);
            originTemp      .put(origin[i]);
        }
        Arrays.stream(transformMatrix).forEach(tMatrixTemp::put);
        for(int i = 0; i < values.length; i++) {
            valuesTemp.put(values[i]);
        }
        extraProps.addAll(extraProperties);

        extent  = extentTemp.asReadOnlyBuffer();
        bounds  = boundsTemp.asReadOnlyBuffer();

        this.dimensions         = dimensionsTemp.asReadOnlyBuffer();
        this.spacing            = spacingTemp   .asReadOnlyBuffer();
        this.pixelSize          = sizeTemp      .asReadOnlyBuffer();
        this.origin             = originTemp    .asReadOnlyBuffer();
        this.transformMatrix    = tMatrixTemp   .asReadOnlyBuffer();
        this.values             = valuesTemp    .asReadOnlyBuffer();
        
        this.dataType           = dataType.copy();
        this.dataDimensionality = dataDimensionality;
        this.dimensionality     = dimensionality;
        this.extraProperties    = Collections.unmodifiableList(extraProps);
    }

    Image(DataType dataType, int dataDimensionality, int dimensionality,
          LongBuffer dimensions, DoubleBuffer spacing, DoubleBuffer pixelSize,
          DoubleBuffer origin, DoubleBuffer transformMatrix, ByteBuffer values,
          LongBuffer extent, DoubleBuffer bounds,
          List<ImmutablePair<String, String>> extraProps) {
        this.dataType           = dataType;
        this.dimensionality     = dimensionality;
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

    Image(DataType dataType, int dataDimensionality, int dimensionality,
          LongBuffer dimensions, DoubleBuffer spacing, DoubleBuffer pixelSize,
          DoubleBuffer origin, DoubleBuffer transformMatrix, ByteBuffer values,
          LongBuffer extent, DoubleBuffer bounds) {
        this.dataType           = dataType;
        this.dimensionality     = dimensionality;
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

    public final DataType getDataType() {
        return dataType;
    }

    public final int getDataDimensionality() {
        return dataDimensionality;
    }

    public final int getDimensionality() {
        return dimensionality;
    }

    public final LongBuffer getDimensions() {
        return dimensions;
    }

    public final DoubleBuffer getSpacing() {
        return spacing;
    }

    public final DoubleBuffer getPixelSize() {
        return pixelSize;
    }

    public final DoubleBuffer getOrigin() {
        return origin;
    }

    public final DoubleBuffer getTransformMatrix() {
        return transformMatrix;
    }

    public final ByteBuffer getValues() {
        return values;
    }

    public final LongBuffer getExtent() {
        return extent;
    }

    public final DoubleBuffer getBounds() {
        return bounds;
    }

    public final List<ImmutablePair<String, String>> getExtraProperties() {
        return extraProperties;
    }

    public final Image copy() {
        return new Image(dataType.copy(), dataDimensionality, dimensionality,
                dimensions.duplicate(), spacing.duplicate(), pixelSize.duplicate(),
                origin.duplicate(), transformMatrix.duplicate(), values.duplicate(),
                extent.duplicate(), bounds.duplicate(),
                Collections.unmodifiableList(extraProperties));
    }
}
