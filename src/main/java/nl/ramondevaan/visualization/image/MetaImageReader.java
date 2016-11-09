package nl.ramondevaan.visualization.image;

import nl.ramondevaan.visualization.data.ComponentType;
import nl.ramondevaan.visualization.data.PixelType;
import nl.ramondevaan.visualization.utilities.DataUtils;
import nl.ramondevaan.visualization.utilities.MetaImageUtilities;
import org.apache.commons.io.FilenameUtils;
import org.apache.commons.lang3.tuple.ImmutablePair;

import java.io.*;
import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.nio.DoubleBuffer;
import java.nio.IntBuffer;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public class MetaImageReader extends ImageReader {
    private final static char LF = 10;
    private final static char CR = 13;
    
    private int                                 dimensionality;
    private int                                 numberOfChannels;
    private IntBuffer                           dimensions;
    private DoubleBuffer                        spacing;
    private DoubleBuffer                        size;
    private DoubleBuffer                        origin;
    private DoubleBuffer                        transformMatrix;
    private ComponentType                       componentType;
    private PixelType                           pixelType;
    private ByteOrder                           byteOrder;
    private ByteBuffer                          bytes;
    private boolean                             local;
    private int                                 headerSize;
    private String                              dataFile;
    private List<ImmutablePair<String, String>> otherProperties;
    
    private InputStream stream;
    private String line;
    private String key;
    private String value;
    private int curChar;
    
    public MetaImageReader() {
        otherProperties = new ArrayList<>();
    }
    
    @Override
    protected Image read() throws IOException {
        resetProperties();
        do {
            readLine();
        } while(parseProperty());
        
        if(dimensionality <= 0) {
           throw new IllegalArgumentException("No dimensionality was provided");
        }
        if(dimensions == null) {
            throw new IllegalArgumentException("Dimension sizes were missing");
        }
        if(dimensions.capacity() != dimensionality) {
            throw new IllegalArgumentException("Number of dimension sizes was not equal to dimensionality");
        }
        if(spacing == null && size == null) {
            spacing = DoubleBuffer.allocate(dimensionality);
            
            while(spacing.hasRemaining()) {
                spacing.put(1d);
            }
            
            size = DataUtils.clone(spacing);
        } else if(spacing == null) {
            spacing = DataUtils.clone(size);
        } else if(size == null) {
            size = DataUtils.clone(spacing);
        }
    
        if(size.capacity() != dimensionality) {
            throw new IllegalArgumentException("Size dimensionality was not equal to provided dimensionality");
        }
        if(spacing.capacity() != dimensionality) {
            throw new IllegalArgumentException("Spacing dimensionality was not equal to provided dimensionality");
        }
        if(origin != null && origin.capacity() != dimensionality) {
            throw new IllegalArgumentException("Origin dimensionality was not equal to provided dimensionality");
        }
        if(transformMatrix != null && transformMatrix.capacity() != (dimensionality * dimensionality)) {
            throw new IllegalArgumentException("TransformMatrix had incorrect dimensionality");
        }
        
        if(componentType == null) {
            throw new IllegalArgumentException("No element type set");
        }
        if(byteOrder == null) {
            throw new IllegalArgumentException("No byte order set");
        }
        
        if(!local) {
            if(dataFile == null) {
                dataFile = FilenameUtils.removeExtension(path) + ".raw";
            }
            stream.close();
            stream = new BufferedInputStream(new FileInputStream(dataFile));
        }
        readBinary();
        stream.close();
    
        IntBuffer extent = IntBuffer.allocate(2 * dimensions.capacity());
        dimensions.rewind();
        while (dimensions.hasRemaining()) {
            extent.put(0);
            extent.put(dimensions.get());
        }
        DoubleBuffer bounds = DoubleBuffer.allocate(extent.capacity());
        
        origin.rewind();
        dimensions.rewind();
        spacing.rewind();
        
        double t;
        double s;
        while(bounds.hasRemaining()) {
            s = spacing.get();
            t = origin.get() - s;
            bounds.put(t);
            bounds.put(t + dimensions.get() * s);
        }
        
        pixelType = numberOfChannels == 1 ?
                PixelType.SCALAR :
                PixelType.VECTOR;
    
        extent          .rewind();
        bounds          .rewind();
        dimensions      .rewind();
        spacing         .rewind();
        size            .rewind();
        origin          .rewind();
        transformMatrix .rewind();
        bytes           .rewind();
        
        return new Image(
                componentType,
                pixelType,
                byteOrder,
                numberOfChannels,
                dimensions,
                spacing,
                size,
                origin,
                transformMatrix,
                bytes,
                extent,
                bounds,
                otherProperties
        );
    }
    
    private void resetProperties() throws FileNotFoundException {
        otherProperties.clear();
        
        dimensionality      = -1;
        numberOfChannels    = 1;
        headerSize          = 0;
        dimensions          = null;
        spacing             = null;
        size                = null;
        origin              = null;
        transformMatrix     = null;
        dataFile            = null;
        componentType       = null;
        bytes               = null;
        local               = false;
        pixelType           = PixelType.SCALAR;
        
        line = null;
        key = null;
        value = null;
        stream = new BufferedInputStream(new FileInputStream(file));
    }

    private void readLine() throws IOException {
        line = "";
        curChar = stream.read();
        while(curChar != -1 && curChar != LF && curChar != CR) {
            line += ((char) curChar);
            curChar = stream.read();
        }
        if(curChar == CR) {
            stream.mark(1);
            curChar = stream.read();
            if(curChar != LF) {
                stream.reset();
            }
        }

        line = line.trim();
        if(line.isEmpty()) {
            key     = null;
            value   = null;
            return;
        }
        String[] split = line.split("=");
        if(split.length != 2) {
            throw new IOException("Could not parse property from:"
                    + System.lineSeparator() + line);
        }
        key = split[0].trim();
        value = split[1].trim();
    }

    private boolean parseProperty() {
        if(key != null) {
            switch (key) {
                case MetaImageUtilities.OBJECT_TYPE:
                    if (!value.equalsIgnoreCase(MetaImageUtilities.IMAGE)) {
                        throw new IllegalArgumentException("MHD did not contain an image");
                    }
                    break;
                case MetaImageUtilities.N_DIMS:
                    dimensionality = Integer.parseInt(value);
                    if (dimensionality <= 0) {
                        throw new IllegalArgumentException("Number of dimensions may not be smaller than 1");
                    }
                    break;
                case MetaImageUtilities.DIM_SIZE:
                    parseDimSize(value);
                    break;
                case MetaImageUtilities.ELEMENT_SIZE:
                    parseElementSize(value);
                    break;
                case MetaImageUtilities.ELEMENT_SPACING:
                    parseElementSpacing(value);
                    break;
                case MetaImageUtilities.BINARY_DATA:
                    if (!Boolean.parseBoolean(value)) {
                        throw new UnsupportedOperationException("Currently, only binary data is supported");
                    }
                    break;
                case MetaImageUtilities.COMPRESSED_DATA:
                    if (Boolean.parseBoolean(value)) {
                        throw new UnsupportedOperationException("Compressed data is not (yet) supported");
                    }
                    break;
                case MetaImageUtilities.ORIGIN:
                    parseOrigin(value);
                    break;
                case MetaImageUtilities.ELEMENT_TYPE:
                    componentType = ComponentType.valueOf(value);
                    break;
                case MetaImageUtilities.TRANSFORM_MATRIX:
                    parseTransformMatrix(value);
                    break;
                case MetaImageUtilities.ELEMENT_NUMBER_OF_CHANNELS:
                    int i = Integer.parseInt(value);
                    if (i < 1) {
                        throw new IllegalArgumentException("ElementNumberOfChannels " +
                                "must be greater than or equal to 1");
                    }
                    numberOfChannels = i;
                    break;
                case MetaImageUtilities.HEADER_SIZE:
                    headerSize = Integer.parseInt(value);
                    break;
                case MetaImageUtilities.ELEMENT_DATAFILE:
                    parseDataFile(value);
                    return false;
                case MetaImageUtilities.BINARYDATA_BYTEORDER_MSB:
                case MetaImageUtilities.ELEMENT_BYTEORDER_MSB:
                    byteOrder = Boolean.parseBoolean(value) ? ByteOrder.BIG_ENDIAN : ByteOrder.LITTLE_ENDIAN;
                    break;
                case MetaImageUtilities.CENTER_OF_ROTATION:
                case MetaImageUtilities.ANATOMICAL_ORIENTATION:
                    addOtherProperty(key, value);
                    break;
                default:
                    throw new UnsupportedOperationException("Unexpected key: \"" + key + "\"");
            }
        }

        return curChar != -1;
    }
    
    private void parseDimSize(String value) {
        String[] dims = value.split("\\s+");
        dimensions = IntBuffer.allocate(dims.length);
        
        Arrays.stream(dims)
                .mapToInt(Integer::parseInt)
                .forEachOrdered(i -> {
                    if(i <= 0) {
                        throw new IllegalArgumentException(
                                "Dimension size cannot be smaller than or equal to 0");
                    }
                    dimensions.put(i);
                });
    }
    
    private void parseElementSize(String value) {
        String[] splSize = value.split("\\s+");
        size = DoubleBuffer.allocate(splSize.length);
    
        Arrays.stream(splSize)
                .mapToDouble(Double::parseDouble)
                .forEachOrdered(d -> {
                    if(d < Double.MIN_VALUE) {
                        throw new IllegalArgumentException(
                                "Size cannot be smaller than or equal to 0");
                    }
                    size.put(d);
                });
    }
    
    private void parseElementSpacing(String value) {
        String[] splSpacing = value.split("\\s+");
        spacing = DoubleBuffer.allocate(splSpacing.length);
    
        Arrays.stream(splSpacing)
                .mapToDouble(Double::parseDouble)
                .forEachOrdered(d -> {
                    if(d < Double.MIN_VALUE) {
                        throw new IllegalArgumentException(
                                "Spacing cannot be smaller than or equal to 0");
                    }
                    spacing.put(d);
                });
    }
    
    private void parseOrigin(String value) {
        String[] split = value.split("\\s+");
        origin = DoubleBuffer.allocate(split.length);
        
        Arrays.stream(split)
                .mapToDouble(Double::parseDouble)
                .forEachOrdered(origin::put);
    }
    
    private void parseTransformMatrix(String value) {
        String[] split = value.split("\\s+");
        transformMatrix = DoubleBuffer.allocate(split.length);
        
        Arrays.stream(split)
                .mapToDouble(Double::parseDouble)
                .forEachOrdered(transformMatrix::put);
    }
    
    private void parseDataFile(String value) {
        if(value.matches("LIST[\\s+\\d+D]")) {
            throw new UnsupportedOperationException("Lists of filenames is currently not supported");
        } else if(value.matches(MetaImageUtilities.LOCAL)) {
            local = true;
            return;
        }
        String ext = FilenameUtils.getExtension(value);
        if(!ext.equalsIgnoreCase("raw")) {
            throw new UnsupportedOperationException("Only files with .raw extension are currently supported");
        }
        dataFile = FilenameUtils.concat(
                FilenameUtils.getFullPath(path), value);
    }
    
    private void addOtherProperty(String key, String value) {
        otherProperties.add(new ImmutablePair<>(key, value));
    }
    
    private void readBinary() throws IOException {
        int num = 1;
        dimensions.rewind();
        while (dimensions.hasRemaining()){
            num *= dimensions.get();
        }
        num *= componentType.numberOfBytes * numberOfChannels;
        
        bytes = ByteBuffer.allocate(num).order(byteOrder);
        byte[] internal = bytes.array();
        if(headerSize > 0) {
            if(headerSize != stream.skip(headerSize)) {
                throw new IOException("Error skipping over required number of bytes");
            }
        }
        int k = stream.read(internal, 0, num);
        if(k < num) {
            throw new IOException("Could not read required number of bytes");
        }
    }
}
