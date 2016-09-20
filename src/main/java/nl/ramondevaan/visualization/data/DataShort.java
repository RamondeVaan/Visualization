package nl.ramondevaan.visualization.data;

import java.nio.ByteBuffer;
import java.nio.ByteOrder;

public class DataShort extends DataType {
    public DataShort(ByteOrder byteOrder) {
        super(ByteBuffer.allocate(2).order(byteOrder).putShort((short) 0));
    }
    
    @Override
    public DataType copy() {
        return new DataShort(zero.order());
    }
}
