package nl.ramondevaan.visualization.data;

import java.nio.ByteBuffer;
import java.nio.ByteOrder;

public class DataFloat extends DataScalar {
    public DataFloat(ByteOrder byteOrder) {
        super(ByteBuffer.allocate(4).order(byteOrder).putFloat(0f));
    }
    
    @Override
    public DataFloat copy() {
        return new DataFloat(getZero().order());
    }
}
