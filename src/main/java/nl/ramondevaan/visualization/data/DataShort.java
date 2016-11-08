package nl.ramondevaan.visualization.data;

import java.nio.ByteBuffer;
import java.nio.ByteOrder;

public class DataShort extends DataScalar {
    public DataShort(ByteOrder byteOrder) {
        super(ByteBuffer.allocate(2).order(byteOrder).putShort((short) 0));
    }
    
    @Override
    public DataShort copy() {
        return new DataShort(getZero().order());
    }
}
