package nl.ramondevaan.visualization.data;

import org.apache.commons.lang3.Validate;

import java.nio.ByteBuffer;

public class DataVector extends DataType  {
    private final DataScalar    scalarType;
    private final int           dimension;
    
    public DataVector(DataScalar scalarType, int dimension) {
        super(getZero(scalarType, dimension));
        this.scalarType = scalarType;
        this.dimension = dimension;
    }
    
    private static ByteBuffer getZero(DataScalar scalarType, int dimension) {
        Validate.notNull(scalarType);
        Validate.isTrue(dimension > 0);
        
        ByteBuffer zero = ByteBuffer.allocate(scalarType.numBytes * dimension);
        for(int i = 0; i < dimension; i++) {
            zero.put(scalarType.getZero());
        }
        return zero;
    }
    
    @Override
    public DataType copy() {
        return new DataVector(scalarType.copy(), dimension);
    }
}
