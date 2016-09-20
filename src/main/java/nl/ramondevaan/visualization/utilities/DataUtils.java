package nl.ramondevaan.visualization.utilities;

import java.nio.ByteBuffer;

public class DataUtils {
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
