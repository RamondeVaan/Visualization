package nl.ramondevaan.visualization.data;

import java.nio.ByteBuffer;

public abstract class DataScalar extends DataType {
    public DataScalar(ByteBuffer zero) {
        super(zero);
    }
    
    public abstract DataScalar copy();
}
