package nl.ramondevaan.visualization.data;

public class DataTypeFactory {
    public final DataType parseDataType(String s) {
        switch(s) {
            case "MET_UCHAR":
                return new DataUChar();
            case "MET_SHORT":
                return new DataShort();
            case "MET_INT":
                return new DataInt();
            case "MET_FLOAT":
                return new DataFloat();
        }
        
        DataType ret = parseDataTypeExt(s);
        
        if(ret == null) {
            throw new UnsupportedOperationException(
                    "Datatype \"" + s + "\" is not (yet) supported");
        }
        
        return ret;
    }
    
    protected DataType parseDataTypeExt(String s) {
        return null;
    }
}
