package nl.ramondevaan.visualization.data;

import java.nio.*;

public enum ComponentType {
    MET_CHAR        (ByteBuffer.allocate(1).put((byte) 0)),
    MET_UCHAR       (ByteBuffer.allocate(1).put((byte) 0)),
    MET_SHORT       (ByteBuffer.allocate(2).putShort((short) 0)),
    MET_INT         (ByteBuffer.allocate(4).putInt(0)),
    MET_LONG_LONG   (ByteBuffer.allocate(8).putLong(0)),
    MET_FLOAT       (ByteBuffer.allocate(4).putFloat(0)),
    MET_DOUBLE      (ByteBuffer.allocate(8).putDouble(0));
    
    public final ByteBuffer zero;
    public final int        numberOfBytes;
    
    ComponentType(ByteBuffer zero) {
        this.numberOfBytes  = zero.capacity();
        this.zero           = zero.asReadOnlyBuffer();
        
        this.zero.rewind();
    }
    
    public final ByteBuffer getZero() {
        return zero.duplicate();
    }

    public final void setByteOrder(ByteBuffer buffer, ByteOrder order) {
        if(buffer.order() == order) {
            return;
        }

        switch(this) {
            case MET_CHAR:
            case MET_UCHAR: {
                ByteBuffer orig = buffer.duplicate().order(buffer.order());
                buffer.order(order);
                orig.put(buffer.duplicate().order(order));
                break;
            }
            case MET_SHORT: {
                ShortBuffer orig = buffer.asShortBuffer();
                buffer.order(order);
                orig.put(buffer.asShortBuffer());
                break;
            }
            case MET_INT: {
                IntBuffer orig = buffer.asIntBuffer();
                buffer.order(order);
                orig.put(buffer.asIntBuffer());
                break;
            }
            case MET_LONG_LONG: {
                LongBuffer orig = buffer.asLongBuffer();
                buffer.order(order);
                orig.put(buffer.asLongBuffer());
                break;
            }
            case MET_FLOAT: {
                FloatBuffer orig = buffer.asFloatBuffer();
                buffer.order(order);
                orig.put(buffer.asFloatBuffer());
                break;
            }
            case MET_DOUBLE: {
                DoubleBuffer orig = buffer.asDoubleBuffer();
                buffer.order(order);
                orig.put(buffer.asDoubleBuffer());
                break;
            }
        }
    }
}
