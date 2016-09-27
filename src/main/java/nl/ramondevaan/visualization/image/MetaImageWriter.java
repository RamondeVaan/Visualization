package nl.ramondevaan.visualization.image;

import javafx.util.Pair;
import nl.ramondevaan.visualization.data.DataTypeFactory;
import nl.ramondevaan.visualization.utilities.DataUtils;
import nl.ramondevaan.visualization.utilities.MetaImageUtilities;
import org.apache.commons.io.FilenameUtils;

import java.io.*;
import java.nio.ByteOrder;

public class MetaImageWriter extends ImageWriter {
    private final static DataTypeFactory DEFAULT_FACTORY = new DataTypeFactory();
    
    private OutputStream stream;
    private String rawPath;
    private DataTypeFactory factory;
    private boolean local;
    private boolean hideElementDataFile;
    
    public MetaImageWriter() {
        factory = DEFAULT_FACTORY;
    }
    
    public MetaImageWriter(DataTypeFactory factory) {
        this.factory = factory == null ?
                DEFAULT_FACTORY : factory;
    }
    
    public final void setLocal(boolean local) {
        this.local = local;
        changed();
    }
    
    public final void setHideElementDataFile(boolean hideElementDataFile) {
        this.hideElementDataFile = hideElementDataFile;
        changed();
    }
    
    public final void setFactory(DataTypeFactory factory) {
        this.factory = factory == null ?
                DEFAULT_FACTORY : factory;
        changed();
    }
    
    public final void setRawPath(String rawPath) {
        this.rawPath = rawPath;
        changed();
    }
    
    @Override
    protected void write(Image image) throws IOException {
        if(!FilenameUtils.getExtension(path).equalsIgnoreCase("mhd")) {
            throw new IllegalArgumentException("Can currently only write mhd files");
        }
        String tmpRawPath = rawPath;
        if(tmpRawPath == null) {
            tmpRawPath = FilenameUtils.getBaseName(path) + ".raw";
        }
        stream = new BufferedOutputStream(new FileOutputStream(file));
        
        printProperty(MetaImageUtilities.OBJECT_TYPE, MetaImageUtilities.IMAGE);
        printProperty(MetaImageUtilities.N_DIMS, image.dimensionality);
        printProperty(MetaImageUtilities.BINARY_DATA, true);
        printProperty(MetaImageUtilities.BINARYDATA_BYTEORDER_MSB,
                image.dataType.zero.order() == ByteOrder.BIG_ENDIAN);
        printProperty(MetaImageUtilities.COMPRESSED_DATA, false);
        printProperty(MetaImageUtilities.TRANSFORM_MATRIX, image.transformMatrix);
        printProperty(MetaImageUtilities.OFFSET, image.offset);
        for(Pair<String, String> p : image.extraProperties) {
            printProperty(p);
        }
        printProperty(MetaImageUtilities.ELEMENT_SPACING, image.spacing);
        printProperty(MetaImageUtilities.ELEMENT_SIZE, image.size);
        printProperty(MetaImageUtilities.DIM_SIZE, image.dimensions);
        printProperty(MetaImageUtilities.ELEMENT_TYPE,
                factory.toTypeString(image.dataType));
        
        if(!local) {
            if(!hideElementDataFile) {
                printProperty(MetaImageUtilities.ELEMENT_DATAFILE, tmpRawPath);
            }
            stream.close();
    
            stream = new BufferedOutputStream(new FileOutputStream(
                    FilenameUtils.concat(FilenameUtils.getFullPath(path), tmpRawPath)));
        } else {
            printProperty(MetaImageUtilities.ELEMENT_DATAFILE, MetaImageUtilities.LOCAL);
        }
        stream.write(image.values.array());
        stream.close();
    }
    
    private static String toString(double[] values) {
        String s  = "";
        for(double d : values) {
            s += DataUtils.NUMBER_FORMAT.format(d);
            s += " ";
        }
        return s.trim();
    }
    
    private static String toString(float[] values) {
        String s  = "";
        for(float f : values) {
            s += DataUtils.NUMBER_FORMAT.format(f);
            s += " ";
        }
        return s.trim();
    }
    
    private static String toString(int[] values) {
        String s  = "";
        for(int i : values) {
            s += String.valueOf(i);
            s += " ";
        }
        return s.trim();
    }
    
    private void printProperty(Pair<String, String> p) throws IOException {
        printProperty(p.getKey(), p.getValue());
    }
    
    private void printProperty(String key, double[] value) throws IOException {
        printProperty(key, toString(value));
    }
    
    private void printProperty(String key, float[] value) throws IOException {
        printProperty(key, toString(value));
    }
    
    private void printProperty(String key, int[] value) throws IOException {
        printProperty(key, toString(value));
    }
    
    private void printProperty(String key, int value) throws IOException {
        printProperty(key, String.valueOf(value));
    }
    
    private void printProperty(String key, boolean value) throws IOException {
        printProperty(key, String.valueOf(value));
    }
    
    private void printProperty(String key, String value) throws IOException {
        println(key + " = " + value);
    }
    
    private void println(String s) throws IOException {
        stream.write(s.getBytes());
        stream.write(System.lineSeparator().getBytes());
    }
}
