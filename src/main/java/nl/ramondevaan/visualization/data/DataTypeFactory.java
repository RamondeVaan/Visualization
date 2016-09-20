package nl.ramondevaan.visualization.data;

import org.apache.commons.lang3.Validate;

import java.nio.ByteOrder;

public class DataTypeFactory {
    private final static String UCHAR = "MET_UCHAR";
    private final static String SHORT = "MET_SHORT";
    private final static String INT = "MET_INT";
    private final static String FLOAT = "MET_FLOAT";
    
    public final DataType parseDataType(String s) {
        return parseDataType(s, ByteOrder.BIG_ENDIAN);
    }
    
    public final DataType parseDataType(String s, ByteOrder byteOrder) {
        Validate.notNull(s);
        
        switch(s) {
            case UCHAR:
                return new DataUChar();
            case SHORT:
                return new DataShort(byteOrder);
            case INT:
                return new DataInt(byteOrder);
            case FLOAT:
                return new DataFloat(byteOrder);
        }
        
        DataType ret = parseDataTypeExt(s, byteOrder);
        
        if(ret == null) {
            throw new UnsupportedOperationException(
                    "Datatype \"" + s + "\" is not (yet) supported");
        }
        
        return ret;
    }
    
    protected DataType parseDataTypeExt(String s, ByteOrder byteOrder) {
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
