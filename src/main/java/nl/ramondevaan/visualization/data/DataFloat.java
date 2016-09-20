package nl.ramondevaan.visualization.data;

import java.nio.ByteBuffer;

public class DataFloat extends DataType {
    public DataFloat() {
        super(ByteBuffer.allocate(4).putFloat(0f));
    }
    
    @Override
    public DataType copy() {
        return new DataFloat();
    }
}
