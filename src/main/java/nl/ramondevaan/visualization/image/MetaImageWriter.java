package nl.ramondevaan.visualization.image;

import javafx.util.Pair;
import nl.ramondevaan.visualization.data.DataTypeFactory;
import nl.ramondevaan.visualization.utilities.DataUtils;
import static nl.ramondevaan.visualization.utilities.MetaImageUtilities.*;
import org.apache.commons.io.FilenameUtils;
import org.apache.commons.lang3.tuple.ImmutablePair;

import java.io.*;
import java.nio.*;
import java.util.Iterator;

public class MetaImageWriter extends ImageWriter {
    private OutputStream stream;
    private String rawPath;
    private boolean local;
    private boolean hideElementDataFile;
    private boolean skipBinary;
    
    public final void setLocal(boolean local) {
        this.local = local;
        changed();
    }
    
    public final void setHideElementDataFile(boolean hideElementDataFile) {
        this.hideElementDataFile = hideElementDataFile;
        changed();
    }
    
    public final void setRawPath(String rawPath) {
        this.rawPath = rawPath;
        changed();
    }
    
    public final void setSkipBinary(boolean skipBinary) {
        this.skipBinary = skipBinary;
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
        
        printProperty(OBJECT_TYPE,                  IMAGE);
        printProperty(N_DIMS,                       image.dimensionality);
        printProperty(BINARY_DATA,                  true);
        printProperty(BINARYDATA_BYTEORDER_MSB,     image.byteOrder == ByteOrder.BIG_ENDIAN);
        printProperty(COMPRESSED_DATA,              false);
        printProperty(TRANSFORM_MATRIX,             image.getTransformMatrix());
        printProperty(ORIGIN,                       image.getOrigin());
        for(ImmutablePair<String, String> p : image.extraProperties) {
            printProperty(p);
        }
        printProperty(ELEMENT_SPACING,              image.getSpacing());
        printProperty(ELEMENT_SIZE,                 image.getPixelSize());
        printProperty(DIM_SIZE,                     image.getDimensions());
        printProperty(ELEMENT_TYPE,                 image.componentType.name());
        printProperty(ELEMENT_NUMBER_OF_CHANNELS,   image.dataDimensionality);
        
        if(!local) {
            if(!hideElementDataFile) {
                printProperty(ELEMENT_DATAFILE, tmpRawPath);
            }
            stream.close();
            
            if(skipBinary) {
                return;
            }
    
            stream = new BufferedOutputStream(new FileOutputStream(
                    FilenameUtils.concat(FilenameUtils.getFullPath(path), tmpRawPath)));
        } else {
            printProperty(ELEMENT_DATAFILE, LOCAL);
        }
        stream.write(image.values.array());
        stream.close();
    }
    
    private static String toString(double[] values) {
        return String.join(" ", () -> new Iterator<CharSequence>() {
            int i = 0;
            
            @Override
            public boolean hasNext() {
                return i < values.length;
            }
        
            @Override
            public CharSequence next() {
                return DataUtils.NUMBER_FORMAT.format(values[i++]);
            }
        });
    }
    
    private static String toString(float[] values) {
        return String.join(" ", () -> new Iterator<CharSequence>() {
            int i = 0;
        
            @Override
            public boolean hasNext() {
                return i < values.length;
            }
        
            @Override
            public CharSequence next() {
                return DataUtils.NUMBER_FORMAT.format(values[i++]);
            }
        });
    }
    
    private static String toString(short[] values) {
        return String.join(" ", () -> new Iterator<CharSequence>() {
            int i = 0;
            
            @Override
            public boolean hasNext() {
                return i < values.length;
            }
            
            @Override
            public CharSequence next() {
                return String.valueOf(values[i++]);
            }
        });
    }
    
    private static String toString(int[] values) {
        return String.join(" ", () -> new Iterator<CharSequence>() {
            int i = 0;
        
            @Override
            public boolean hasNext() {
                return i < values.length;
            }
        
            @Override
            public CharSequence next() {
                return String.valueOf(values[i++]);
            }
        });
    }
    
    private static String toString(long[] values) {
        return String.join(" ", () -> new Iterator<CharSequence>() {
            int i = 0;
            
            @Override
            public boolean hasNext() {
                return i < values.length;
            }
            
            @Override
            public CharSequence next() {
                return String.valueOf(values[i++]);
            }
        });
    }
    
    private static String toString(DoubleBuffer buffer) {
        return String.join(" ", () -> new Iterator<CharSequence>() {
            @Override
            public boolean hasNext() {
                return buffer.hasRemaining();
            }

            @Override
            public CharSequence next() {
                return DataUtils.NUMBER_FORMAT.format(buffer.get());
            }
        });
    }
    
    private static String toString(FloatBuffer buffer) {
        return String.join(" ", () -> new Iterator<CharSequence>() {
            @Override
            public boolean hasNext() {
                return buffer.hasRemaining();
            }
        
            @Override
            public CharSequence next() {
                return DataUtils.NUMBER_FORMAT.format(buffer.get());
            }
        });
    }
    
    private static String toString(ShortBuffer buffer) {
        return String.join(" ", () -> new Iterator<CharSequence>() {
            @Override
            public boolean hasNext() {
                return buffer.hasRemaining();
            }
            
            @Override
            public CharSequence next() {
                return String.valueOf(buffer.get());
            }
        });
    }
    
    private static String toString(IntBuffer buffer) {
        return String.join(" ", () -> new Iterator<CharSequence>() {
            @Override
            public boolean hasNext() {
                return buffer.hasRemaining();
            }
        
            @Override
            public CharSequence next() {
                return String.valueOf(buffer.get());
            }
        });
    }
    
    private static String toString(LongBuffer buffer) {
        return String.join(" ", () -> new Iterator<CharSequence>() {
            @Override
            public boolean hasNext() {
                return buffer.hasRemaining();
            }
            
            @Override
            public CharSequence next() {
                return String.valueOf(buffer.get());
            }
        });
    }
    
    private void printProperty(ImmutablePair<String, String> p) throws IOException {
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
    
    private void printProperty(String key, DoubleBuffer buffer) throws IOException {
        printProperty(key, toString(buffer));
    }
    
    private void printProperty(String key, FloatBuffer buffer) throws IOException {
        printProperty(key, toString(buffer));
    }
    
    private void printProperty(String key, ShortBuffer buffer) throws IOException {
        printProperty(key, toString(buffer));
    }
    
    private void printProperty(String key, IntBuffer buffer) throws IOException {
        printProperty(key, toString(buffer));
    }
    
    private void printProperty(String key, LongBuffer buffer) throws IOException {
        printProperty(key, toString(buffer));
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
