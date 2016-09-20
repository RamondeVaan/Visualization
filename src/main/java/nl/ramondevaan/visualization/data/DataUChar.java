package nl.ramondevaan.visualization.data;

import java.nio.ByteBuffer;

public class DataUChar extends DataType {
    public DataUChar() {
        super(ByteBuffer.allocate(1).put((byte) (0)));
    }
    
    @Override
    public DataType copy() {
        return new DataUChar();
    }
}
