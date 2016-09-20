package nl.ramondevaan.visualization.data;

import java.nio.ByteBuffer;

public class DataShort extends DataType {
    public DataShort() {
        super(ByteBuffer.allocate(2).putShort((short) 0));
    }
    
    @Override
    public DataType copy() {
        return new DataShort();
    }
}
