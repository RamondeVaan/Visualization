package nl.ramondevaan.visualization.data;

import java.nio.ByteBuffer;
import java.nio.ByteOrder;

public class DataFloat extends DataType {
    public DataFloat(ByteOrder byteOrder) {
        super(ByteBuffer.allocate(4).order(byteOrder).putFloat(0f));
    }
    
    @Override
    public DataType copy() {
        return new DataFloat(zero.order());
    }
}
