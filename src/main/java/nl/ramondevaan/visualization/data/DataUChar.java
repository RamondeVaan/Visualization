package nl.ramondevaan.visualization.data;

import java.nio.ByteBuffer;

public class DataUChar extends DataScalar {
    public DataUChar() {
        super(ByteBuffer.allocate(1).put((byte) (0)));
    }
    
    @Override
    public DataUChar copy() {
        return new DataUChar();
    }
}
