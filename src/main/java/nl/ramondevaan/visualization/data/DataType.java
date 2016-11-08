package nl.ramondevaan.visualization.data;

import nl.ramondevaan.visualization.utilities.DataUtils;

import java.nio.ByteBuffer;

public abstract class DataType {
    
    public final int numBytes;
    private final ByteBuffer zero;
    
    protected DataType(ByteBuffer zero) {
        if(zero.capacity() <= 0) {
            throw new IllegalArgumentException("Capacity must be at least 1");
        }
        this.numBytes = zero.capacity();
        this.zero = DataUtils.clone(zero).asReadOnlyBuffer();
    }
    
    public final ByteBuffer getZero() {
        return zero.duplicate();
    }
    
    public abstract DataType copy();
}