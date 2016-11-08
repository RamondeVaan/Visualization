package nl.ramondevaan.visualization.data;

public enum ComponentType {
    MET_CHAR    (1),
    MET_UCHAR   (1),
    MET_SHORT   (2),
    MET_INT     (4),
    MET_LONG    (8),
    MET_FLOAT   (4),
    MET_DOUBLE  (8);
    
    public  final int       numberOfBytes;
    
    ComponentType(int numberOfBytes) {
        this.numberOfBytes = numberOfBytes;
    }
}
