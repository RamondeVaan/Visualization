package nl.ramondevaan.visualization.data;

import java.nio.ByteBuffer;
import java.nio.ByteOrder;

public class DataLong extends DataScalar {
    public DataLong(ByteOrder byteOrder) {
        super(ByteBuffer.allocate(8).order(byteOrder).putLong(0));
    }
    
    @Override
    public DataLong copy() {
        return new DataLong(getZero().order());
    }
}
