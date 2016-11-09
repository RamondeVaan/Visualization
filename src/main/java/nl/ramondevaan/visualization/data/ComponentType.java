package nl.ramondevaan.visualization.data;

import java.nio.ByteBuffer;

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
}
