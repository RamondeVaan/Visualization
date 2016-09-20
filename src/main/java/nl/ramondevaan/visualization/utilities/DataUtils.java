package nl.ramondevaan.visualization.utilities;

import java.nio.ByteBuffer;
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
    
    public static ByteBuffer cloneByteBuffer(final ByteBuffer original) {
        final ByteBuffer clone = (original.isDirect()) ?
                ByteBuffer.allocateDirect(original.capacity()) :
                ByteBuffer.allocate(original.capacity());
        final ByteBuffer readOnlyCopy = original.asReadOnlyBuffer();
        
        readOnlyCopy.flip();
        clone.put(readOnlyCopy);
        clone.order(original.order());
        
        return clone;
    }
}
