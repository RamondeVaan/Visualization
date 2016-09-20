package nl.ramondevaan.visualization.data;

import java.nio.ByteBuffer;
import java.nio.ByteOrder;

public class DataInt extends DataType {
    public DataInt(ByteOrder byteOrder) {
        super(ByteBuffer.allocate(4).order(byteOrder).putInt(0));
    }
    
    @Override
    public DataType copy() {
        return new DataInt(zero.order());
    }
}
