package nl.ramondevaan.visualization.utilities;

import java.nio.*;
import java.text.DecimalFormat;
import java.text.DecimalFormatSymbols;
import java.util.Locale;

public class DataUtils {
    public final static DecimalFormat NUMBER_FORMAT;
    
    static {
        DecimalFormatSymbols s = new DecimalFormatSymbols(Locale.US);
        
        NUMBER_FORMAT = new DecimalFormat();
        NUMBER_FORMAT.setDecimalFormatSymbols(s);
        NUMBER_FORMAT.setGroupingUsed(false);
        NUMBER_FORMAT.setMaximumFractionDigits(340);
        NUMBER_FORMAT.setDecimalSeparatorAlwaysShown(false);
    }
    
    public static ByteBuffer clone(final ByteBuffer original) {
        final ByteBuffer clone = (original.isDirect()) ?
                ByteBuffer.allocateDirect(original.capacity()) :
                ByteBuffer.allocate(original.capacity());
        final ByteBuffer readOnlyCopy = original.asReadOnlyBuffer();
        readOnlyCopy.rewind();
        clone.put(readOnlyCopy);
        clone.order(original.order());
        
        return clone;
    }
    
    public static ShortBuffer clone(final ShortBuffer original) {
        final ShortBuffer clone = (original.isDirect()) ?
                ByteBuffer.allocateDirect(original.capacity() * 2)
                        .order(original.order()).asShortBuffer() :
                ByteBuffer.allocate(original.capacity() * 2)
                        .order(original.order()).asShortBuffer();
        final ShortBuffer readOnlyCopy = original.asReadOnlyBuffer();
        readOnlyCopy.rewind();
        clone.put(readOnlyCopy);
        
        return clone;
    }
    
    public static FloatBuffer clone(final FloatBuffer original) {
        final FloatBuffer clone = (original.isDirect()) ?
                ByteBuffer.allocateDirect(original.capacity() * 4)
                        .order(original.order()).asFloatBuffer() :
                ByteBuffer.allocate(original.capacity() * 4)
                        .order(original.order()).asFloatBuffer();
        final FloatBuffer readOnlyCopy = original.asReadOnlyBuffer();
        readOnlyCopy.rewind();
        clone.put(readOnlyCopy);
    
        return clone;
    }
    
    public static IntBuffer clone(final IntBuffer original) {
        final IntBuffer clone = (original.isDirect()) ?
                ByteBuffer.allocateDirect(original.capacity() * 4)
                        .order(original.order()).asIntBuffer() :
                ByteBuffer.allocate(original.capacity() * 4)
                        .order(original.order()).asIntBuffer();
        final IntBuffer readOnlyCopy = original.asReadOnlyBuffer();
        readOnlyCopy.rewind();
        clone.put(readOnlyCopy);
        
        return clone;
    }
    
    public static LongBuffer clone(final LongBuffer original) {
        final LongBuffer clone = (original.isDirect()) ?
                ByteBuffer.allocateDirect(original.capacity() * 8)
                        .order(original.order()).asLongBuffer() :
                ByteBuffer.allocate(original.capacity() * 8)
                        .order(original.order()).asLongBuffer();
        final LongBuffer readOnlyCopy = original.asReadOnlyBuffer();
        readOnlyCopy.rewind();
        clone.put(readOnlyCopy);
        
        return clone;
    }
    
    public static DoubleBuffer clone(final DoubleBuffer original) {
        final DoubleBuffer clone = (original.isDirect()) ?
                ByteBuffer.allocateDirect(original.capacity() * 8)
                        .order(original.order()).asDoubleBuffer() :
                ByteBuffer.allocate(original.capacity() * 8)
                        .order(original.order()).asDoubleBuffer();
        final DoubleBuffer readOnlyCopy = original.asReadOnlyBuffer();
        readOnlyCopy.rewind();
        clone.put(readOnlyCopy);
        
        return clone;
    }
}
