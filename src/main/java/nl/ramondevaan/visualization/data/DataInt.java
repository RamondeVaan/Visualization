package nl.ramondevaan.visualization.data;

import java.nio.ByteBuffer;

public class DataInt extends DataType {
    public DataInt() {
        super(ByteBuffer.allocate(4).putInt(0));
    }
    
    @Override
    public DataType copy() {
        return new DataInt();
    }
}
