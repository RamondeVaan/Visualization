package nl.ramondevaan.visualization.image;

import nl.ramondevaan.visualization.utilities.DataUtils;
import org.apache.commons.io.FilenameUtils;
import org.apache.commons.lang3.Validate;
import org.apache.commons.lang3.tuple.ImmutablePair;

import java.io.FileOutputStream;
import java.io.IOException;
import java.nio.*;
import java.nio.channels.FileLock;
import java.util.Iterator;

import static nl.ramondevaan.visualization.utilities.MetaImageUtilities.*;

public class MetaImageWriter extends ImageWriter {
    private FileOutputStream    stream;
    private String              rawPath;
    private boolean             local;
    private boolean             hideElementDataFile;
    private boolean             skipBinary;
    private ByteOrder           byteOrder;

    public MetaImageWriter() {
        this.byteOrder = ByteOrder.BIG_ENDIAN;
    }

    public final String getRawPath() {
        return rawPath;
    }

    public final boolean isLocal() {
        return local;
    }

    public final boolean isHideElementDataFile() {
        return hideElementDataFile;
    }

    public final boolean isSkipBinary() {
        return skipBinary;
    }

    public final ByteOrder getByteOrder() {
        return byteOrder;
    }

    public final void setLocal(boolean local) {
        if(local != this.local) {
            this.local = local;
            changed();
        }
    }
    
    public final void setHideElementDataFile(boolean hideElementDataFile) {
        if(hideElementDataFile != this.hideElementDataFile) {
            this.hideElementDataFile = hideElementDataFile;
            changed();
        }
    }
    
    public final void setRawPath(String rawPath) {
        if(!FilenameUtils.equalsNormalizedOnSystem(this.rawPath, rawPath)) {
            this.rawPath = rawPath;
            changed();
        }
    }
    
    public final void setSkipBinary(boolean skipBinary) {
        if(skipBinary != this.skipBinary) {
            this.skipBinary = skipBinary;
            changed();
        }
    }

    public final void setByteOrder(ByteOrder byteOrder) {
        Validate.notNull(byteOrder);
        if(byteOrder != this.byteOrder) {
            this.byteOrder = byteOrder;
            changed();
        }
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
        stream = new FileOutputStream(file);

        FileLock lock = stream.getChannel().tryLock();
        if(lock == null) {
            throw new IOException("Could not lock \"" + path + "\".");
        }
        
        printProperty(OBJECT_TYPE,                  IMAGE);
        printProperty(N_DIMS,                       image.dimensionality);
        printProperty(BINARY_DATA,                  true);
        printProperty(BINARYDATA_BYTEORDER_MSB,     byteOrder == ByteOrder.BIG_ENDIAN);
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
    
            stream = new FileOutputStream(FilenameUtils.concat(
                    FilenameUtils.getFullPath(path), tmpRawPath));

            lock = stream.getChannel().tryLock();
            if(lock == null) {
                throw new IOException("Could not lock \"" + path + "\".");
            }
        } else {
            printProperty(ELEMENT_DATAFILE, LOCAL);
        }

        ByteBuffer buffer = DataUtils.clone(image.getValues());
        buffer.rewind();
        image.componentType.setByteOrder(buffer, byteOrder);

        stream.getChannel().write(buffer);
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
