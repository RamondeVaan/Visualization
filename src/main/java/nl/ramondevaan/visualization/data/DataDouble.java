package nl.ramondevaan.visualization.data;

import java.nio.ByteBuffer;
import java.nio.ByteOrder;

public class DataDouble extends DataScalar {
    public DataDouble(ByteOrder byteOrder) {
        super(ByteBuffer.allocate(8).order(byteOrder).putDouble(0));
    }
    
    @Override
    public DataDouble copy() {
        return new DataDouble(getZero().order());
    }
}
