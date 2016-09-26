package nl.ramondevaan.visualization.image;

import javafx.util.Pair;
import nl.ramondevaan.visualization.data.DataType;
import nl.ramondevaan.visualization.data.DataTypeFactory;
import nl.ramondevaan.visualization.utilities.MetaImageUtilities;
import org.apache.commons.io.FilenameUtils;

import java.io.*;
import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Properties;

public class MetaImageReader extends ImageReader {
    private final static DataTypeFactory DEFAULT_FACTORY = new DataTypeFactory();
    private final static char LF = 10;
    private final static char CR = 13;
    
    private int dimensionality;
    private int[] dimensions;
    private double[] spacing;
    private double[] size;
    private double[] offset;
    private double[] transformMatrix;
    private DataType dataType;
    private ByteOrder byteOrder;
    private ByteBuffer bytes;
    private boolean local;
    private boolean byteOrderSet;
    private int headerSize;
    private String dataFile;
    private List<Pair<String, String>> otherProperties;
    private String dataTypeString;
    
    private InputStream stream;
    private DataTypeFactory factory;
    String line;
    String key;
    String value;
    int curChar;
    
    public MetaImageReader() {
        otherProperties = new ArrayList<>();
        factory = DEFAULT_FACTORY;
    }
    
    public MetaImageReader(DataTypeFactory factory) {
        this.factory = factory == null ?
                DEFAULT_FACTORY : factory;
    }
    
    public final void setFactory(DataTypeFactory factory) {
        this.factory = factory == null ?
                DEFAULT_FACTORY : factory;
        changed();
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
        if(dimensions.length != dimensionality) {
            throw new IllegalArgumentException("Number of dimension sizes was not equal to dimensionality");
        }
        if(spacing == null && size == null) {
            size = new double[dimensionality];
            spacing = new double[dimensionality];
            Arrays.fill(size, 1);
            Arrays.fill(spacing, 1);
        } else if(spacing == null) {
            spacing = new double[size.length];
            System.arraycopy(size, 0, spacing, 0, size.length);
        } else if(size == null) {
            size = new double[spacing.length];
            System.arraycopy(spacing, 0, size, 0, spacing.length);
        }
    
        if(size.length != dimensionality) {
            throw new IllegalArgumentException("Size dimensionality was not equal to provided dimensionality");
        }
        if(spacing.length != dimensionality) {
            throw new IllegalArgumentException("Spacing dimensionality was not equal to provided dimensionality");
        }
        if(offset != null && offset.length != dimensionality) {
            throw new IllegalArgumentException("Offset dimensionality was not equal to provided dimensionality");
        }
        if(transformMatrix != null && transformMatrix.length != (dimensionality * dimensionality)) {
            throw new IllegalArgumentException("TransformMatrix had incorrect dimensionality");
        }
        
        if(dataTypeString == null) {
            throw new IllegalArgumentException("No element type set");
        }
        if(!byteOrderSet) {
            throw new IllegalArgumentException("No byte order set");
        }
        dataType = factory.parseDataType(dataTypeString, byteOrder);
        
        if(!local) {
            if(dataFile == null) {
                dataFile = FilenameUtils.removeExtension(path) + ".raw";
            }
            stream.close();
            stream = new BufferedInputStream(new FileInputStream(dataFile));
        }
        readBinary();
        stream.close();
    
        int[] extent = new int[2 * dimensions.length];
        for(int i = 0; i < dimensions.length; i++) {
            extent[2 * i + 1] = dimensions[i] - 1;
        }
        double[] bounds = new double[extent.length];
        int k;
        for(int i = 0; i < dimensions.length; i++) {
            k = 2 * i;
            bounds[k] = offset[i];
            bounds[k + 1] = offset[i] + dimensions[i] * spacing[i];
        }
        
        return new Image(dataType, dimensionality, dimensions, spacing, size,
                offset, transformMatrix, bytes, extent, bounds, otherProperties);
    }
    
    private void resetProperties() throws FileNotFoundException {
        dimensionality = -1;
        dimensions = null;
        spacing = null;
        size = null;
        offset = null;
        transformMatrix = null;
        otherProperties.clear();
        byteOrderSet = false;
        headerSize = 0;
        dataFile = null;
        local = false;
        dataTypeString = null;
        dataType = null;
        bytes = null;

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

        String[] split = line.split("=");
        if(split.length != 2) {
            throw new IOException("Could not parse property from:"
                    + System.lineSeparator() + line);
        }
        key = split[0].trim();
        value = split[1].trim();
    }

    private boolean parseProperty() {
        switch(key) {
            case MetaImageUtilities.OBJECT_TYPE:
                if(!value.equalsIgnoreCase(MetaImageUtilities.IMAGE)) {
                    throw new IllegalArgumentException("MHD did not contain an image");
                }
                break;
            case MetaImageUtilities.N_DIMS:
                dimensionality = Integer.parseInt(value);
                if(dimensionality <= 0) {
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
                if(!Boolean.parseBoolean(value)) {
                    throw new UnsupportedOperationException("Currently, only binary data is supported");
                }
                break;
            case MetaImageUtilities.COMPRESSED_DATA:
                if(Boolean.parseBoolean(value)) {
                    throw new UnsupportedOperationException("Compressed data is not (yet) supported");
                }
                break;
            case MetaImageUtilities.OFFSET:
                parseOffset(value);
                break;
            case MetaImageUtilities.ELEMENT_TYPE:
                dataTypeString = value;
                break;
            case MetaImageUtilities.TRANSFORM_MATRIX:
                parseTransformMatrix(value);
                break;
            case MetaImageUtilities.ELEMENT_NUMBER_OF_CHANNELS:
                int i = Integer.parseInt(value);
                if(i != 1) {
                    throw new IllegalArgumentException("Currently, only a value of 1 for ElementNumberOfChannels is supported");
                }
                addOtherProperty(key, value);
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
                byteOrderSet = true;
                break;
            case MetaImageUtilities.CENTER_OF_ROTATION:
            case MetaImageUtilities.ANATOMICAL_ORIENTATION:
                addOtherProperty(key, value);
                break;
            default:
                throw new UnsupportedOperationException("Unexpected key: \"" + key + "\"");
        }

        return curChar != -1;
    }
    
    private void parseDimSize(String value) {
        String[] dims = value.split("\\s+");
        dimensions = new int[dims.length];
        for(int i = 0; i < dims.length; i++) {
            dimensions[i] = Integer.parseInt(dims[i]);
            if(dimensions[i] <= 0) {
                throw new IllegalArgumentException("Dimension size cannot be smaller than or equal to 0");
            }
        }
    }
    
    private void parseElementSize(String value) {
        String[] splSize = value.split("\\s+");
        size = new double[splSize.length];
        for(int i = 0; i < splSize.length; i++) {
            size[i] = Double.parseDouble(splSize[i]);
            if(size[i] < Double.MIN_VALUE) {
                throw new IllegalArgumentException("Size cannot be smaller than or equal to 0");
            }
        }
    }
    
    private void parseElementSpacing(String value) {
        String[] splSpacing = value.split("\\s+");
        spacing = new double[splSpacing.length];
        for(int i = 0; i < splSpacing.length; i++) {
            spacing[i] = Double.parseDouble(splSpacing[i]);
            if(spacing[i] < Double.MIN_VALUE) {
                throw new IllegalArgumentException("Spacing cannot be smaller than or equal to 0");
            }
        }
    }
    
    private void parseOffset(String value) {
        String[] split = value.split("\\s+");
        offset = new double[split.length];
        for(int i = 0; i < offset.length; i++) {
            offset[i] = Double.parseDouble(split[i]);
        }
    }
    
    private void parseTransformMatrix(String value) {
        String[] split = value.split("\\s+");
        transformMatrix = new double[split.length];
        for(int i = 0; i < transformMatrix.length; i++) {
            transformMatrix[i] = Double.parseDouble(split[i]);
        }
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
        otherProperties.add(new Pair<>(key, value));
    }
    
    private void readBinary() throws IOException {
        int num = 1;
        for(int i = 0; i < dimensionality; i++) {
            num *= dimensions[i];
        }
        num *= dataType.numBytes;
        
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
