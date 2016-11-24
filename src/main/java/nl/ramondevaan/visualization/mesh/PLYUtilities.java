package nl.ramondevaan.visualization.mesh;

import java.util.Arrays;
import java.util.Collections;
import java.util.List;

public class PLYUtilities {
    public final static String FORMAT_BIG_ENDIAN    = "format binary_big_endian 1.0";
    public final static String FORMAT_LITTLE_ENDIAN = "format binary_little_endian 1.0";
    public final static String FORMAT_ASCII         = "format ascii 1.0";

    public final static String          VERTEX_NAME         = "vertex";
    public final static String          FACE_NAME           = "face";
    public final static String          INDICES_NAME        = "vertex_indices";
    public final static List<String>    COORD_DIM_NAMES     = Collections.unmodifiableList(Arrays.asList(
            "x", "y", "z"
    ));
    public final static List<String>    NORMAL_DIM_NAMES    = Collections.unmodifiableList(Arrays.asList(
            "nx", "ny", "nz"
    ));
}
