package nl.ramondevaan.visualization.data;

import org.apache.commons.lang3.Validate;

import java.nio.ByteOrder;

public class DataTypeFactory {
    private final static String UCHAR   = "MET_UCHAR";
    private final static String SHORT   = "MET_SHORT";
    private final static String INT     = "MET_INT";
    private final static String FLOAT   = "MET_FLOAT";
    private final static String LONG    = "MET_LONG";
    private final static String DOUBLE  = "MET_DOUBLE";
    
    public final DataType parseDataType(String s) {
        return parseDataType(s, ByteOrder.BIG_ENDIAN);
    }
    
    public final DataType parseDataType(String s, ByteOrder byteOrder) {
        return parseDataType(s, byteOrder, 1);
    }
    
    public final DataType parseDataType(String s, int numberOfChannels) {
        return parseDataType(s, ByteOrder.BIG_ENDIAN, numberOfChannels);
    }
    
    public final DataType parseDataType(String s, ByteOrder byteOrder, int numberOfChannels) {
        Validate.notNull(s);
        Validate.isTrue(numberOfChannels >= 1);
    
        switch(s) {
            case UCHAR:
                return new DataUChar();
            case SHORT:
                return new DataShort(byteOrder);
            case INT:
                return new DataInt(byteOrder);
            case FLOAT:
                return new DataFloat(byteOrder);
            case LONG:
                return new DataLong(byteOrder);
            case DOUBLE:
                return new DataDouble(byteOrder);
        }
    
        DataScalar ret = parseDataTypeExt(s, byteOrder);
    
        if(ret == null) {
            throw new UnsupportedOperationException(
                    "Datatype \"" + s + "\" is not (yet) supported");
        }
        
        return numberOfChannels <= 1 ? ret :
                new DataVector(ret, numberOfChannels);
    }
    
    protected DataScalar parseDataTypeExt(String s, ByteOrder byteOrder) {
        return null;
    }
    
    public final String toTypeString(DataType dataType) {
        Validate.notNull(dataType);
        
        if(dataType instanceof DataUChar) {
            return UCHAR;
        } else if(dataType instanceof DataShort) {
            return SHORT;
        } else if(dataType instanceof DataInt) {
            return INT;
        } else if(dataType instanceof DataFloat) {
            return FLOAT;
        }
        
        String ret = toTypeStringExt(dataType);
        if(ret == null) {
            throw new UnsupportedOperationException("Could not find type string for given DataType");
        }
        
        return ret;
    }
    
    protected String toTypeStringExt(DataType dataType) {
        return null;
    }
}
